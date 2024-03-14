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

import de.gematik.kether.rpc.types.RpcResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
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

    @Test
    fun deserializeRpcResponseNullResult() {
        val string = """{
            "jsonrpc" : "2.0",
            "id" : 0,
            "result" : "0x"
        }"""
        val deSerialized = Json.decodeFromString<RpcResponse>(string)
        val test = RpcResponse(0, JsonPrimitive("0x"))
        assert(deSerialized.id == test.id &&
                deSerialized.jsonrpc == test.jsonrpc &&
                deSerialized.result == test.result
        )
    }

}