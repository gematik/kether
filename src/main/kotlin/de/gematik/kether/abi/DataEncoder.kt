package de.gematik.kether.abi

import de.gematik.kether.eth.types.Data
import java.nio.charset.Charset

/**
 * Created by rk on 12.08.2022.
 * gematik.de
 */
class DataEncoder() {
    data class Chunk(val isDynamic: Boolean, val bytes: ByteArray)

    private val chunks = mutableListOf<Chunk>()
    private var containsSelector = false

    fun encodeSelector(selector: ByteArray): DataEncoder {
        require(chunks.isEmpty()) { "selector must be first element" }
        require(selector.size == 4) { "selector must be 4 bytes long" }
        chunks.add(Chunk(false, selector))
        containsSelector = true
        return this
    }

    fun encode(int: AbiUint256): DataEncoder {
        var b = int.toByteArray()
        check(b.size <= 32)
        b = b.copyInto(
            if (int.signum() < 0) ByteArray(32, { (-1).toByte() }) else ByteArray(32),
            32 - b.size
        )
        chunks.add(Chunk(false, b))
        return this
    }

    fun encode(address: AbiAddress): DataEncoder {
        var b = address.toByteArray()
        check(b.size <= 32)
        b = b.copyInto(ByteArray(32),32 - b.size)
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

    fun build(): Data {
        var bytes = byteArrayOf()
        var bytesDynamic = byteArrayOf()
        chunks.forEach {
            if (it.isDynamic) {
                val offset = ((if (containsSelector) chunks.size - 1 else chunks.size) * 32 + bytesDynamic.size).toBigInteger().toByteArray()
                bytes += offset.copyInto(ByteArray(32), 32 - offset.size)
                bytesDynamic += it.bytes
            } else {
                bytes += it.bytes
            }
        }
        return Data(bytes + bytesDynamic)
    }
}