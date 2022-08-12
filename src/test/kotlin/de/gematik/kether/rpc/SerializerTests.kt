package de.gematik.kether.rpc

import de.gematik.kether.types.Address
import de.gematik.kether.types.Quantity
import de.gematik.kether.types.RpcResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class SerializerTests {

    @Test
    fun serializeRpcResponse() {
        val jsonObject = JsonObject(
            mapOf(
                "jsonrpc" to JsonPrimitive("2.0"),
                "id" to JsonPrimitive(0),
                "result" to JsonPrimitive("0x331e7")
            )
        )
        val rpcResponse = RpcResponse(0, Quantity("331e7".toLong(16)))
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
        val deSerialized = Json.decodeFromString<RpcResponse<Quantity>>(string)
        val test = RpcResponse(0, Quantity("331e7".toLong(16)))
        assert(deSerialized.id == test.id &&
                deSerialized.jsonrpc == test.jsonrpc &&
                deSerialized.result?.value == test.result?.value
        )
    }

    @Test
    fun deserializeRpcResponseQantity() {
        val string = """{
            "jsonrpc" : "2.0",
            "id" : 0,
            "result" : "0x331e7"
        }"""
        val deSerialized = Json.decodeFromString<RpcResponse<Quantity>>(string)
        val test = RpcResponse(0, Quantity("331e7".toLong(16)))
        assert(deSerialized.id == test.id &&
                deSerialized.jsonrpc == test.jsonrpc &&
                deSerialized.result?.value == test.result?.value
        )
    }

    @Test
    fun deserializeRpcResponseAddressList() {
        val string = """{
            "jsonrpc" : "2.0",
            "id" : 0,
            "result" : ["0x1122334455667788990011223344556677889900"]
        }"""
        val deSerialized = deserialize<RpcResponse<List<Address>>>(string)
        val test = RpcResponse(0, listOf(Address("0x1122334455667788990011223344556677889900")))
        assert(deSerialized.id == test.id &&
                deSerialized.jsonrpc == test.jsonrpc &&
                deSerialized.result?.get(0)?.value.contentEquals(test.result?.get(0)?.value)
        )
    }

    inline fun <reified T> deserialize(string: String) : T {
        val value = Json.decodeFromString<T>(string)
        return value
    }

}