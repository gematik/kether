package de.gematik.kether.rpc.types

import de.gematik.kether.eth.types.Data
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

@Serializable
@ExperimentalSerializationApi
class RpcResponse {

    val id: Int

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val jsonrpc = "2.0"
    var error: Error? = null
    var result: JsonElement? = null

    @Serializable
    data class Error(
        val code: Int,
        val message: String,
        val data: Data? = null
    )

    constructor(id: Int, result: JsonElement) {
        this.id = id
        this.result = result
    }

    constructor(id: Int, error: Error) {
        this.id = id
        this.error = error
        this.result = null
    }

    inline fun <reified T> result(): T {
        val r = result
        if (r == null) throw RpcException(error!!.code, error!!.message)
        return Json.decodeFromJsonElement(r)
    }

}
