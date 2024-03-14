/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.kether.rpc

import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.types.RpcNotification
import de.gematik.kether.rpc.types.RpcRequest
import de.gematik.kether.rpc.types.RpcResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Closeable

/**
 * Created by rk on 02.08.2022.
 */

private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class Rpc(val url: String = "http://localhost:8545", val wsUrl: String? = "ws://localhost:8546", val isSigner: Boolean = false) : Closeable {
    private var id: Int = 0

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    protected val okClient by lazy { OkHttpClient() }

    private val _notifications = MutableSharedFlow<RpcNotification>()
    internal val notifications = _notifications.asSharedFlow()

    private val _responses = MutableSharedFlow<RpcResponse>()
    internal val responses = _responses.asSharedFlow()

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
                                val message = deserializeMessage(text)
                                when (message) {
                                    is RpcResponse -> _responses.emit(message)
                                    is RpcNotification -> _notifications.emit(message)
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

    fun call(request: RpcRequest): RpcResponse {
        request.id = id++
        val jsonString = json.encodeToString(request)
        val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        logger.debug("$req\n$jsonString")
        val json = okClient.newCall(req).execute().body!!.string()
        logger.debug(json.replace("\\s".toRegex(), ""))
        return Json.decodeFromString(json)
    }

    fun send(request: RpcRequest) {
        request.id = id++
        val json = json.encodeToString(request)
        logger.debug("ws<-: $json")
        ws.send(json)
    }

    private fun deserializeMessage(jsonString: String): Any {
        return runCatching {
                if(Json.parseToJsonElement(jsonString).jsonObject.containsKey("id")){
                    json.decodeFromString<RpcResponse>(jsonString)
                }else{
                    json.decodeFromString<RpcNotification>(jsonString)
                }
        }.onFailure { logger.debug(it.message) }.getOrThrow()
    }

}