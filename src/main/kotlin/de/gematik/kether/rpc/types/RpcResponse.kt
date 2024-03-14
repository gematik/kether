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

package de.gematik.kether.rpc.types

import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.types.AbiSelector
import de.gematik.kether.abi.types.AbiString
import de.gematik.kether.eth.types.Data
import de.gematik.kether.extensions.toHex
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Created by rk on 02.08.2022.
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
        if (r == null) {
            val message2 = if (error?.data != null) {
                val dataDecoder = DataDecoder(error!!.data!!)
                val selector = dataDecoder.next(AbiSelector::class)
                "${selector.toByteArray().toHex()} - ${dataDecoder.next(AbiString::class)}"
            } else {
                ""
            }
            throw RpcException(error!!.code, "${error!!.message} - $message2")
        }
        return Json.decodeFromJsonElement(r)
    }

    inline fun <reified T> resultOrNull(): T? {
        val r = result
        if (r == null) {
            val message2 = if (error?.data != null) {
                val dataDecoder = DataDecoder(error!!.data!!)
                val selector = dataDecoder.next(AbiSelector::class)
                "${selector.toByteArray().toHex()} - ${dataDecoder.next(AbiString::class)}"
            } else {
                ""
            }
            return null
        }
        return Json.decodeFromJsonElement(r)
    }
}
