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

import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 */

/**
 * Converts hex string to byte array. Hex string must start with prefix 0x followed by two characters per byte.
 * @param length of the resulting byte array, if null length is defined by length of hex string
 * @return [ByteArray] of hex string
 * @throws [IllegalStateException] if wrong length, wrong prefix or invalid characters
 */
fun String.hexToByteArray(len: Int? = null): ByteArray {
    check(lowercase().startsWith("0x")) { "Must have prefix 0x" }
    var string = drop(2)
    if (length % 2 != 0) {
        string = string.padStart(1, '0')
    }
    val byteArray = string.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    return if (len == null) byteArray else byteArray.copyInto(ByteArray(len), len - byteArray.size)
}

/**
 * Converts byte array to hex string with prefix 0x.
 * @param discardLeadingZeros if true discards leading zeros
 * @return hex string of byte array
 * @throws [IllegalStateException] if wrong length, wrong prefix or invalid characters
 */

fun ByteArray.toHex(discardLeadingZeros: Boolean = false): String {
    val string = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    return "0x${if (discardLeadingZeros) string.trimStart('0') else string}"
}

/**
 * Returns a byteArray of length size containing the unsigned representation of this BigInteger
 * @param size of destination array
 * @return byteArray of length size containing the unsigned representation of this BigInteger
 * @throws [IllegalStateException] if the BigInteger doesn't fit into defined size
 */

fun BigInteger.toByteArray(size: Int): ByteArray {
    val byteList = toByteArray().dropWhile { it == 0.toByte() }
    check(byteList.size <= size){"BigInteger to big"}
    return byteList.toByteArray().copyInto(ByteArray(size),size - byteList.size, 0, byteList.size)
}



