package de.gematik.kether.extensions

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

/**
 * Converts hex string to byte array. Hex string must start with prefix 0x followed by two characters per byte.
 * @param length of the resulting byte array, if null length is defined by length of hex string
 * @return [ByteArray] of hex string
 * @throws [IllegalStateException] if wrong length, wrong prefix or invalid characters
 */
fun String.hexToByteArray(len: Int? = null): ByteArray {
    check(lowercase().startsWith("0x")) {"Must have prefix 0x"}
    var string = drop(2)
    if(length % 2 != 0) {
        string = string.padStart(1,'0')
    }
    val byteArray = string.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    return if(len==null) byteArray else byteArray.copyInto(ByteArray(len), len-byteArray.size)
}

/**
 * Converts byte array to hex string with prefix 0x.
 * @param discardLeadingZeros if true discards leading zeros
 * @return hex string of byte array
 * @throws [IllegalStateException] if wrong length, wrong prefix or invalid characters
 */

fun ByteArray.toHex(discardLeadingZeros : Boolean = false): String {
    val string = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    return "0x${if(discardLeadingZeros) string.trimStart('0') else string}"
}




