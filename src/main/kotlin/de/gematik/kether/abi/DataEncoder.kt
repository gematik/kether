package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
import de.gematik.kether.eth.types.Data
import de.gematik.kether.eth.types.Quantity
import java.nio.charset.Charset

/**
 * Created by rk on 12.08.2022.
 * gematik.de
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

    fun encode(any: Any, size: Int = -1): DataEncoder {
        return when(any){
            is String -> encode(any)
            is Quantity -> encode(any)
            is Array<*> -> encodeArray(any as Array<Any>, size)
            else -> throw IllegalArgumentException("${any::class.simpleName} not yet implemented")
        }
    }

    private fun <T> encodeArray(array: Array<T>,size: Int = -1): DataEncoder where T : Any {
        if( size >= 0){
            require(array.size == size)
        }
        val dataEncoder = DataEncoder()
        array.forEach {
            dataEncoder.encode(it)
        }
        val isDynamic = !dataEncoder.isPureStatic() || size < 0
        val length = if(isDynamic){
            array.size.toBigInteger().toByteArray().let { it.copyInto(ByteArray(32), 32 - it.size)}
        }else{
            ByteArray(0)
        }
        chunks.add(Chunk(isDynamic, length + dataEncoder.build().toByteArray()))
        return this
    }

    fun encode(tuple: AbiTuple): DataEncoder {
        val dataEncoder = tuple.encode()
        chunks.add(Chunk(!dataEncoder.isPureStatic(), dataEncoder.build().toByteArray()))
        return this
    }

    fun build(): Data {
        var bytes = byteArrayOf()
        var bytesDynamic = byteArrayOf()
        val baseOffset = chunks.let {
            var len = 0
            it.forEach {
                len += if(it.isDynamic) 32 else it.bytes.size
            }
            if(containsSelector) len-4 else len
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

    fun isPureStatic() = chunks.firstOrNull() { it.isDynamic } == null

}