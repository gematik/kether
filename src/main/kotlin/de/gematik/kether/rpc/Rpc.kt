package de.gematik.kether.rpc

import de.gematik.kether.types.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Closeable

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class Rpc(val url: String = "http://localhost:8547", val wsUrl: String? = "ws://localhost:8546") : Closeable {
    private var id: Int = 0

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    protected val okClient by lazy { OkHttpClient() }

    private val _notifications = MutableSharedFlow<RpcNotification<*>>()
    internal val notifications = _notifications.asSharedFlow()

    private val _responses = MutableSharedFlow<RpcResponse<*>>()
    private val responses = _responses.asSharedFlow()

    private lateinit var ws: WebSocket

    init {
        wsUrl?.let {
            val request = Request.Builder().url(wsUrl).build()
            ws = okClient.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        runBlocking {
                            launch(Dispatchers.IO) {
                                logger.debug("ws->:${text.replace("\\s".toRegex(), "")}")
                                val message = deserialize(text)
                                when (message) {
                                    is RpcResponse<*> -> _responses.emit(message)
                                    is RpcNotification<*> -> _notifications.emit(message)
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    override fun close() {
        if (this::ws.isInitialized) {
            ws.close(1000, "socket closed regularly")
        }
    }

    suspend fun subscribe(type: SubscriptionTypes, filter: Filter = Filter()): RpcResponse<String> {
            when (type) {
                SubscriptionTypes.newHeads -> send(RpcRequest(RpcMethods.eth_subscribe, listOf(type.name)))
                SubscriptionTypes.logs -> send(RpcRequest(RpcMethods.eth_subscribe, listOf(type.name, filter)))
            }
            @Suppress("UNCHECKED_CAST")
            return responses.first() as RpcResponse<String>
    }

    suspend fun unsubscribe(subscriptionId: String): RpcResponse<Boolean> {
        send(RpcRequest(RpcMethods.eth_unsubscribe, listOf(subscriptionId)))
        @Suppress("UNCHECKED_CAST")
        return responses.first() as RpcResponse<Boolean>
    }


    fun call(request: RpcRequest): Response {
        request.id = id++
        val jsonString = json.encodeToString(request)
        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        logger.debug("$req\n$jsonString")
        return okClient.newCall(req).execute()
    }

    fun send(request: RpcRequest) {
        request.id = id++
        val json = json.encodeToString(request)
        logger.debug("ws<-: $json")
        ws.send(json)
    }

    private fun deserialize(jsonString: String): Any {
        return runCatching {
            when {
                jsonString.contains("number") -> json.decodeFromString<RpcNotification<Head>>(jsonString)
                jsonString.contains("logIndex") -> json.decodeFromString<RpcNotification<Log>>(jsonString)
                else -> json.decodeFromString<RpcResponse<AnyResult>>(jsonString)
            }

        }.onFailure { logger.debug(it.message) }.getOrThrow()
    }

}