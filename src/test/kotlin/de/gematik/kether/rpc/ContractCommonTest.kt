package de.gematik.kether.rpc

import HelloWorld
import Storage
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.extensions.toRLP
import de.gematik.kether.types.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import java.math.BigInteger
import java.util.*
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractCommonTest {
    @Test
    fun encodingFunctionIntArg() {
        val selector = byteArrayOf(1, 2, 3, 4)
        val arg = EthUint256("1")
        val function = DataEncoder().encodeSelector(selector).encode(arg).data()
        val decoder = DataDecoder(function)
        assert(
            decoder.next<EthSelector>().contentEquals(selector) &&
                    decoder.next<EthUint256>().toInt() == 1
        )
    }

    @Test
    fun decodingIntResult() {
        val num = BigInteger("1")
        val result = DataEncoder().encode(num).data()
        val decoder = DataDecoder(result)
        assert(
            decoder.next<EthUint256>() == num
        )
    }

    @Test
    fun decodingStringResult() {
        val string = "test"
        val result = DataEncoder().encode(string).data()
        val decoder = DataDecoder(result)
        assert(
            decoder.next<EthString>() == "test"
        )
    }

    @Test
    fun encodeTransaction() {
        val transaction = Transaction(
            to = Address("0x1122334455667788990011223344556677889900"),
            data = DataEncoder().encodeSelector(byteArrayOf(1,2,3,4)).data()
        )
        val byteArray = transaction.toRLP()
        assert(byteArray.size > 0)
    }
}
