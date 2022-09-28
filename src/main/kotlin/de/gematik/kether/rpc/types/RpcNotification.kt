package de.gematik.kether.rpc.types

import de.gematik.kether.eth.types.EthEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@Serializable
data class RpcNotification(val jsonrpc: String, val method: String, val params: Params){
    @Serializable
    data class Params(val subscription: String, val result: JsonElement)

    inline fun <reified T : EthEvent> result(): T {
        val r = params.result
        return Json.decodeFromJsonElement(r)
    }
}



