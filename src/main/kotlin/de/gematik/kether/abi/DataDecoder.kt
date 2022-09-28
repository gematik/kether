package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
import de.gematik.kether.eth.types.Data
import java.math.BigInteger

/**
 * Created by rk on 12.08.2022.
 * gematik.de
 */
class DataDecoder(val data: Data) {
    var pos: Int = 0

    inline fun <reified T> next(): T {
        return when (T::class) {
            AbiSelector::class -> {
                check(pos == 0 && data.toByteArray().size >=4) {"data decoding error: remaining data too short (pos: $pos, limit: ${data.toByteArray().size}, type: ${T::class.simpleName}"}
                val bytes = data.toByteArray().copyOfRange(0, 4)
                pos+=4
                AbiSelector(bytes) as T
            }
            AbiUint256::class -> {
                check(data.toByteArray().size - pos >= 32) {"data decoding error: remaining data too short (pos: $pos, limit: ${data.toByteArray().size}, type: ${T::class.simpleName}"}
                val bytes = data.toByteArray().copyOfRange(pos, pos + 32)
                pos += 32
                AbiUint256(bytes) as T
            }
            AbiString::class -> {
                check(data.toByteArray().size - pos >= 32) {"data decoding error: remaining data too short (pos: $pos, limit: ${data.toByteArray().size}, type: ${T::class.simpleName}"}
                val offset = BigInteger(data.toByteArray().copyOfRange(pos, pos + 32)).toInt()
                pos += 32
                val length = BigInteger(data.toByteArray().copyOfRange(offset, offset + 32)).toInt()
                String(data.toByteArray().copyOfRange(offset + 32, offset + 32 + length)) as T
            }
            AbiBytes32::class -> {
                check(data.toByteArray().size - pos >= 32) {"data decoding error: remaining data too short (pos: $pos, limit: ${data.toByteArray().size}, type: ${T::class.simpleName}"}
                val bytes = data.toByteArray().copyOfRange(pos, pos + 32)
                pos += 32
                AbiBytes32(bytes) as T
            }
            else -> {
                error("data type not supported: ${T::class.qualifiedName}")
                //TODO: bytes<M>, bytes, tuple and arrays
            }
        }
    }
}