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

package de.gematik.kether.abi

import de.gematik.kether.abi.types.AbiAddress
import de.gematik.kether.abi.types.AbiSelector
import de.gematik.kether.abi.types.AbiString
import de.gematik.kether.abi.types.AbiTuple
import de.gematik.kether.eth.types.Data
import de.gematik.kether.eth.types.Quantity
import java.nio.charset.Charset

/**
 * Created by rk on 12.08.2022.
 */
class DataEncoder() {
    data class Chunk(val isDynamic: Boolean, val bytes: ByteArray)

    private val chunks = mutableListOf<Chunk>()
    private var containsSelector = false

    fun encode(selector: AbiSelector): DataEncoder {
        require(chunks.isEmpty()) { "selector must be first element" }
        chunks.add(Chunk(false, selector.toByteArray()))
        containsSelector = true
        return this
    }

    fun encode(bytes: ByteArray): DataEncoder {
        chunks.add(Chunk(false, bytes))
        return this
    }

    fun encode(int: Quantity): DataEncoder {
        var b = int.toBigInteger().toByteArray()
        check(b.size <= 32)
        b = b.copyInto(
            if (int.toBigInteger().signum() < 0) ByteArray(32, { (-1).toByte() }) else ByteArray(32),
            32 - b.size
        )
        chunks.add(Chunk(false, b))
        return this
    }

    fun encode(address: AbiAddress): DataEncoder {
        var b = address.toByteArray()
        check(b.size <= 32)
        b = b.copyInto(ByteArray(32), 32 - b.size)
        chunks.add(Chunk(false, b))
        return this
    }

    fun encode(string: AbiString): DataEncoder {
        val str = string.toByteArray(Charset.forName("UTF-8"))
        val len = str.size.toBigInteger().toByteArray()
        val bytes = ByteArray((1 + str.size / 32 + 1) * 32)
        len.copyInto(bytes, 32 - len.size)
        str.copyInto(bytes, 32)
        chunks.add(Chunk(true, bytes))
        return this
    }

    fun encode(tuple: AbiTuple): DataEncoder {
        val dataEncoder = tuple.encode()
        chunks.add(
            Chunk(
                dataEncoder.chunks.filter { it.isDynamic }.isNotEmpty(),
                dataEncoder.build().toByteArray()
            )
        )
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun encode(array: List<Any>, vararg dimensions: Int): DataEncoder {
        require(dimensions.isNotEmpty())
        if (dimensions.last() > 0) {
            require(
                array.size == dimensions.last(),
                { "wrong dimension: expected ${dimensions.last()} is ${array.size}" })
        }
        val dataEncoder = DataEncoder()
        array.forEach {
            if (dimensions.size > 1) {
                dataEncoder.encode(it as List<Any>, *dimensions.copyOf(dimensions.size - 1))
            } else
                when (it) {
                    is String -> dataEncoder.encode(it)
                    is Quantity -> dataEncoder.encode(it)
                    is AbiAddress -> dataEncoder.encode(it)
                    is AbiTuple -> dataEncoder.encode(it)
                    else -> throw IllegalArgumentException("${it::class.simpleName} not supported in arrays yet")
                }
        }
        val isDynamic = (dataEncoder.chunks.isNotEmpty() && dataEncoder.chunks[0].isDynamic) || dimensions.last() < 0
        val length = if (isDynamic) {
            array.size.toBigInteger().toByteArray().let { it.copyInto(ByteArray(32), 32 - it.size) }
        } else {
            ByteArray(0)
        }
        chunks.add(Chunk(isDynamic, length + dataEncoder.build().toByteArray()))
        return this
    }

    fun build(): Data {
        var bytes = byteArrayOf()
        var bytesDynamic = byteArrayOf()
        val baseOffset = chunks.let {
            var len = 0
            it.forEach {
                len += if (it.isDynamic) 32 else it.bytes.size
            }
            if (containsSelector) len - 4 else len
        }
        chunks.forEach {
            if (it.isDynamic) {
                val offset = (baseOffset + bytesDynamic.size).toBigInteger().toByteArray()
                bytes += offset.copyInto(ByteArray(32), 32 - offset.size)
                bytesDynamic += it.bytes
            } else {
                bytes += it.bytes
            }
        }
        return Data(bytes + bytesDynamic)
    }

}