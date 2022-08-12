package de.gematik.kether.abi

import de.gematik.kether.types.Data
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
            EthUint256::class -> {
                check(data.value.size - pos >= 32)
                val bytes = data.value.copyOfRange(pos, pos + 32)
                pos+=32
                EthUint256(bytes) as T
            }
            else -> error("data type not supported: ${T::class.qualifiedName}")
        }
    }
}