package de.gematik.kether.contracts

import de.gematik.kether.abi.*
import de.gematik.kether.abi.types.AbiUint256
import de.gematik.kether.eth.types.*
import de.gematik.kether.extensions.toRLP
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
    fun encodingFunction() {
        val selector = byteArrayOf(1, 2, 3, 4)
        val function = DataEncoder().encode(Data4(selector)).build()
        val decoder = DataDecoder(function)
        assert(
            decoder.next<Data4>().toByteArray().contentEquals(selector))
    }

    @Test
    fun decodingError() {
        var message: String? = null
        val data = Data20("0x00")
        runCatching {
            DataDecoder(data).next<AbiUint256>()
            DataDecoder(data).next<AbiUint256>()
        }.onFailure {
            message = it.message
        }
        assert(message=="data decoding error: remaining data too short (pos: 0, limit: 20, type: Quantity")
    }

    @Test
    fun encodingInt() {
        val num = Quantity(1)
        val result = DataEncoder().encode(num).build()
        val decoder = DataDecoder(result)
        assert(decoder.next<Quantity>() == num)
    }

    @Test
    fun encodingString() {
        val string = "test"
        val result = DataEncoder().encode(string).build()
        val decoder = DataDecoder(result)
        assert(decoder.next<String>() == string)
    }

    @Test
    fun encodeTransaction() {
        val transaction = Transaction(
            to = Address("0x1122334455667788990011223344556677889900"),
            data = DataEncoder().encode(Data4(byteArrayOf(1,2,3,4))).build()
        )
        val byteArray = transaction.toRLP()
        assert(byteArray.size > 0)
    }
}
