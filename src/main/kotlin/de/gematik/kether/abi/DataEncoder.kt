package de.gematik.kether.abi

import de.gematik.kether.types.Data
import de.gematik.kether.types.EthUint256
import java.math.BigInteger

/**
 * Created by rk on 12.08.2022.
 * gematik.de
 */
class DataEncoder() {
    private var bytes: ByteArray = ByteArray(0)

    fun encodeSelector(selector: ByteArray) : DataEncoder {
        require(bytes.size==0){"selector must be first element"}
        require(selector.size == 4){"selector must be 4 bytes long"}
        bytes = bytes.plus(selector)
        return this
    }

    fun encode(int: EthUint256) : DataEncoder {
        var b = int.toByteArray()
        check(b.size<=32)
        b = b.copyInto(if(int.signum()<0) ByteArray(32, {(-1).toByte()}) else ByteArray(32),
            32-b.size)
        bytes = bytes.plus(b)
        return this
    }

    fun data() : Data {
        return Data(bytes)
    }
}