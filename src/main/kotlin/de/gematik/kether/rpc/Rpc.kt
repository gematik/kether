package de.gematik.kether.rpc

import de.gematik.kether.types.RpcRequest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger{}

@ExperimentalSerializationApi
class Rpc(val url: String = "http:localhost:8547") {
    private var id: Int = 0

    private val okClient by lazy { OkHttpClient() }

    fun call(request: RpcRequest): Response {
        request.id = id++
        val json = Json.encodeToString(request)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        logger.debug ( "$req\n$json" )
        return okClient.newCall(req).execute()
    }

}