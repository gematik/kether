package de.gematik.kether.rpc

import de.gematik.kether.abi.*
import de.gematik.kether.extensions.toRLP
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractCommonTest {
    @Test
    fun encodingFunctionIntArg() {
        val selector = byteArrayOf(1, 2, 3, 4)
        val arg = AbiUint256("1")
        val function = DataEncoder().encodeSelector(selector).encode(arg).build()
        val decoder = DataDecoder(function)
        assert(
            decoder.next<AbiSelector>().contentEquals(selector) &&
                    decoder.next<AbiUint256>().toInt() == 1
        )
    }

    @Test
    fun decodingIntResult() {
        val num = BigInteger("1")
        val result = DataEncoder().encode(num).build()
        val decoder = DataDecoder(result)
        assert(
            decoder.next<AbiUint256>() == num
        )
    }

    @Test
    fun decodingStringResult() {
        val string = "test"
        val result = DataEncoder().encode(string).build()
        val decoder = DataDecoder(result)
        assert(
            decoder.next<AbiString>() == "test"
        )
    }

    @Test
    fun encodeTransaction() {
        val transaction = Transaction(
            to = Address("0x1122334455667788990011223344556677889900"),
            data = DataEncoder().encodeSelector(byteArrayOf(1,2,3,4)).build()
        )
        val byteArray = transaction.toRLP()
        assert(byteArray.size > 0)
    }
}
