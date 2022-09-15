package de.gematik.kether.rpc

import de.gematik.kether.types.Address
import de.gematik.kether.types.Quantity
import de.gematik.kether.types.RpcResponse
import de.gematik.kether.types.TransactionReceipt
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

    @Test
    fun deserializeTransactionReceipt() {
        val string = """{
            "blockHash":"0x74d183a39de533b27cc375b395c0db4128425a8b6158f95a63ecbd419c0629b5",
            "blockNumber":"0xe962b",
            "contractAddress":"0x4721ab4c4c8e087dfb4a486c5573f9ac09580b4e",
            "cumulativeGasUsed":"0x8ca98",
            "from":"0xfe3b557e8fb62b89f4916b721be55ceb828dbd73",
            "gasUsed":"0x8ca98",
            "effectiveGasPrice":"0x0",
            "logs":[],"logsBloom":"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
            "status":"0x1",
            "to":null,
            "transactionHash":"0x57d07ce7ab0be69594341ab14c4d0a1d49ba7dd7065a23fcc7ddffc8e81a35f7",
            "transactionIndex":"0x0"
            }""".trimMargin()
        val deSerialized = deserialize<TransactionReceipt>(string)
        assert(deSerialized.status.value==1L)
    }

    inline fun <reified T> deserialize(string: String) : T {
        val value = Json.decodeFromString<T>(string)
        return value
    }

}