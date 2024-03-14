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

package de.gematik.kether.eth

import de.gematik.kether.eth.types.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 */
@ExperimentalSerializationApi
class EthSerializerTests {

    @Test
    fun serializeQuantity() {
        val quantity = Quantity(BigInteger.TEN)
        val serialized = Json.encodeToString(quantity)
        val elements = Json.parseToJsonElement(serialized)
        assert(elements.jsonPrimitive.content == "0xa")
    }

    @Test
    fun deserializeQantity() {
        val string = """"0x331e7""""
        val deSerialized = Json.decodeFromString<Quantity>(string)
        assert(
            deSerialized == Quantity("331e7".toBigInteger(16))
        )
    }

    @Test
    fun deserializeDataEmpty() {
        val string = """"0x""""
        val deSerialized = Json.decodeFromString<Data>(string)
        assert(
            deSerialized == Data("0x")
        )
    }

    @Test
    fun deserializeDataHex() {
        val string = """"0x1122""""
        val deSerialized = Json.decodeFromString<Data>(string)
        assert(
            deSerialized == Data("0x1122")
        )
    }

    @Test
    fun deserializeData4() {
        val string = """"0x00112233""""
        val deSerialized = Json.decodeFromString<Data>(string)
        assert(
            deSerialized == Data4("0x112233")
        )
    }

    @Test
    fun serializeQuantityTag() {
        val quantity = Quantity(Tag.latest)
        val serialized = Json.encodeToString(quantity)
        val elements = Json.parseToJsonElement(serialized)
        assert(elements.jsonPrimitive.content == "latest")
    }

    @Test
    fun deserializeAddressList() {
        val string = """["0x1122334455667788990011223344556677889900"]"""
        val deSerialized = Json.decodeFromString<List<Address>>(string)
        assert(
            deSerialized == listOf(Address("0x1122334455667788990011223344556677889900"))
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
        val deSerialized = Json.decodeFromString<TransactionReceipt>(string)
        assert(deSerialized.status == Quantity(BigInteger.ONE))
    }

}