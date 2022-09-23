package de.gematik.kether.extensions

import de.gematik.kether.eth.types.Data
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.eth.types.Transaction
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
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
            byteArrayOf((0xb7 + lengthOfLength).toByte()).plus(length.toByteArray()).plus(this)
        }
    }
}

/**
 * Wraps a [List] of RLP coded byte arrays into a RLP list.
 * @return RLP list coded byte array
 */

private fun List<*>.toRLP(): ByteArray {
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
            byteArrayOf((0xf7 + lengthOfLength).toByte()).plus(length.toByteArray()).plus(payload)
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
fun BigInteger.toRLP(): ByteArray = toByteArray().toRLP()

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
fun Quantity.toRLP(): ByteArray = toBigInteger().toByteArray().toRLP()

/**
 * Converts [Quantity] to RLP coded byte array.
 * @return RLP coded byte array
 */
fun Data.toRLP(): ByteArray = toByteArray().toRLP()


fun Transaction.toRLP(isEIP1559: Boolean = false): ByteArray {
    val rlpList = if (isEIP1559) {
        error("not implemented yet")
    } else {
        listOf(
            nonce?.toRLP() ?: RlpEmpty,
            gasPrice?.toRLP() ?: RlpEmpty,
            gas?.toRLP() ?: RlpEmpty,
            to?.toRLP() ?: RlpEmpty,
            value?.toRLP() ?: RlpEmpty,
            data?.toRLP() ?: RlpEmpty
        )
    }
    return rlpList.toRLP()
}

