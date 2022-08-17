package de.gematik.kether.abi

import de.gematik.kether.types.Data
import de.gematik.kether.types.EthSelector
import de.gematik.kether.types.EthString
import de.gematik.kether.types.EthUint256
import java.math.BigInteger

/**
 * Created by rk on 12.08.2022.
 * gematik.de
 */
class DataDecoder(val data: Data) {
    var pos: Int = 0

    inline fun <reified T> next(): T {
        return when (T::class) {
            EthSelector::class -> {
                check(pos == 0 && data.value.size >=32)
                val bytes = data.value.copyOfRange(0, 4)
                pos+=4
                bytes as T
            }
            EthUint256::class -> {
                check(data.value.size - pos >= 32)
                val bytes = data.value.copyOfRange(pos, pos + 32)
                pos += 32
                EthUint256(bytes) as T
            }
            EthString::class -> {
                check(data.value.size - pos >= 32)
                val offset = BigInteger(data.value.copyOfRange(pos, pos + 32)).toInt()
                pos += 32
                val length = BigInteger(data.value.copyOfRange(offset, offset + 32)).toInt()
                String(data.value.copyOfRange(offset + 32, offset + 32 + length)) as T
            }
            else -> {
                error("data type not supported: ${T::class.qualifiedName}")
                //TODO: bytes<M>, bytes, tuple and arrays
            }
        }
    }
}