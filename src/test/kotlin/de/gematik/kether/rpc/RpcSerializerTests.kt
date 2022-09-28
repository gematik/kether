package de.gematik.kether.rpc

import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.eth.types.Tag
import de.gematik.kether.eth.types.TransactionReceipt
import de.gematik.kether.rpc.types.RpcResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class RpcSerializerTests {

    @Test
    fun serializeRpcResponse() {
        val jsonObject = JsonObject(
            mapOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(0),
                "result" to JsonPrimitive("0x331e7")
            )
        )
        val rpcResponse = RpcResponse(0, JsonPrimitive("0x331e7"))
        val serialized = Json.encodeToString(rpcResponse)
        val elements = Json.parseToJsonElement(serialized)
        assert(elements.jsonObject == jsonObject)
    }

    @Test
    fun deserializeRpcResponse() {
        val string = """{
            "jsonrpc" : "2.0",
            "id" : 0,
            "result" : "0x331e7"
        }"""
        val deSerialized = Json.decodeFromString<RpcResponse>(string)
        val test = RpcResponse(0, JsonPrimitive("0x331e7"))
        assert(deSerialized.id == test.id &&
                deSerialized.jsonrpc == test.jsonrpc &&
                deSerialized.result == test.result
        )
    }

}