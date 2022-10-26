package de.gematik.kether.extensions

import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

/**
 * Converts RLP coded byte array to [ByteArray].
 * @return [ByteArray] payload of RLP coded element
 */
fun ByteArray.toByteArrayFromRLP(): ByteArray? = when(this[0].toUByte().toInt()) {
     in 0 .. 0x7f -> { // byte
        check(this.size == 1)
        this
    }
    0x80 -> { // null
        check(this.size == 1)
        null
    }
    in 0x81 ..0xb7 -> { // byteArray with length less than 55 byte
        val length = this[0].toInt() - 0x80
        check(size == 1 + length)
        copyOfRange(1, this.size)
    }
    in 0xb8 .. 0xbf -> { // byteArray with length greater than 55 bytes
        val lengthOfLength = this[0].toInt()-0xb7
        val length = BigInteger(copyOfRange(1, lengthOfLength)).toInt()
        check(size == 1 + lengthOfLength + length)
        copyOfRange(1 + lengthOfLength, this.size)
    }
    in 0xc0 ..0xf7 -> { // payload with length less than 55 byte
        val length = this[0].toInt() - 0xc0
        check(size == 1 + length)
        copyOfRange(1, this.size)
    }
    in 0xf8 .. 0xff -> { // payload with length greater than 55 bytes
        val lengthOfLength = this[0].toInt()-0xf7
        val length = BigInteger(copyOfRange(1, lengthOfLength)).toInt()
        check(size == 1 + lengthOfLength + length)
        copyOfRange(1 + lengthOfLength, this.size)
    }
    else -> error("wrong RLP coding")
}

/**
 * Converts RLP coded byte array to [Int].
 * @return [Int] value of RLP coded element
 */
fun ByteArray.toIntFromRLP(): Int? = toByteArrayFromRLP()?.let{
    check(it.size < 4)
    BigInteger(it).toInt()
}

/**
 * Converts RLP coded byte array to [BigInteger].
 * @return [BigInteger] value of RLP coded element
 */
fun ByteArray.toBigIntegerFromRLP(): BigInteger? = toByteArrayFromRLP()?.let{BigInteger(1, it)}

/**
 * Converts RLP coded byte array to [Byte].
 * @return [Byte] value of RLP coded element
 * @throws [IllegalStateException] if byte array length not equal 1 or byte value greater 127
 */
fun ByteArray.toByteFromRLP(): Byte? = toByteArrayFromRLP()?.let{
    check(it.size == 1)
    this[0]
}

/**
 * Converts RLP coded byte array to [String].
 * @return [BigInteger] value of RLP coded element
 */
fun ByteArray.toStringFromRLP() = String(this)