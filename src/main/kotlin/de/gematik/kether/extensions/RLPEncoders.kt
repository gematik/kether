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

package de.gematik.kether.extensions

import de.gematik.kether.eth.types.Data
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.eth.types.Transaction
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 */

/**
 * Converts [ByteArray] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun ByteArray.toRLP(): ByteArray {
    return when {
        size == 1 && this[0] < 0x7f.toByte() -> this
        size <= 55 -> byteArrayOf((0x80 + size).toByte()).plus(this)
        else -> {
            val length = size.toBigInteger()
            val lengthOfLength = (length.bitLength() - 1) / 8 + 1
            byteArrayOf((0xb7 + lengthOfLength).toByte()).plus(length.toByteArray().dropWhile{it == 0.toByte()}.toByteArray()).plus(this)
        }
    }
}

/**
 * Wraps a [List] of RLP coded byte arrays into a RLP list.
 * @return RLP list coded byte array
 */

fun List<*>.toRLP(): ByteArray {
    var payload = ByteArray(0)
    forEach {
        payload = payload.plus(
            when (it) {
                is List<*> -> it.toRLP()
                is ByteArray -> it
                else -> error("only list or byte arrays are allowed in RLP lists")
            }
        )
    }
    return when {
        payload.size <= 55 -> byteArrayOf((0xc0 + payload.size).toByte()).plus(payload)
        else -> {
            val length = payload.size.toBigInteger()
            val lengthOfLength = (length.bitLength() - 1) / 8 + 1
            byteArrayOf((0xf7 + lengthOfLength).toByte()).plus(length.toByteArray().dropWhile{it == 0.toByte()}.toByteArray()).plus(payload)
        }
    }
}

/**
 * Converts [String] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun String.toRLP(): ByteArray = toByteArray().toRLP()

/**
 * Converts [Int] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun Int.toRLP(): ByteArray = toBigInteger().toByteArray().toRLP()

/**
 * Converts [BigInteger] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun BigInteger.toRLP(): ByteArray = toByteArray().dropWhile{it == 0.toByte()}.toByteArray().toRLP()

val RlpEmpty = byteArrayOf(0x80.toByte())

/**
 * Converts [Byte] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun Byte.toRLP(): ByteArray = byteArrayOf(this).toRLP()

/**
 * Converts [Quantity] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun Quantity.toRLP(): ByteArray = toBigInteger().toRLP()

/**
 * Converts [Quantity] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun Data.toRLP(): ByteArray = toByteArray().toRLP()

