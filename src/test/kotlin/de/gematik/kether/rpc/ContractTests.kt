package de.gematik.kether.rpc

import Storage
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.extensions.toRLP
import de.gematik.kether.types.Address
import de.gematik.kether.types.Data
import de.gematik.kether.types.EthUint256
import de.gematik.kether.types.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractTests {
    val ethereum1 = Eth(Rpc("http:ethereum1.lab.gematik.de:8547"))

    @Test
    fun encodeFunction() {
        val selector = byteArrayOf(1, 2, 3, 4)
        val arg = EthUint256("1")
        val function = DataEncoder().encodeSelector(selector).encode(arg).data()
        assert(function.value.copyOfRange(0, 4).contentEquals(selector))
        assert(function.value[35] == "1".toByte())
    }

    @Test
    fun decodeInt() {
        val data = Data(32, "0x1")
        val result: EthUint256 = DataDecoder(data).next()
        assert(result.toInt() == 1)
    }

    @Test
    fun encodeTransaction() {
        val transaction = Transaction(
            to = Address("0xB389e2Ac92361c81481aFeF1cBF29881005996a3"),
            data = DataEncoder().encodeSelector(Storage.retrieveSelector).data()
        )
        val byteArray = transaction.toRLP()
        assert(byteArray.size > 0)
    }

    @Test
    fun storageRetrieve() {
        val storage = Storage(
            ethereum1,
            Transaction(to = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78"))
        )
        storage.retrieve()
    }

    @Test
    fun storageIncAndRetrieve() {
        runBlocking {
            val storage = Storage(
                ethereum1,
                Transaction(
                    from = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73"),
                    to = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78")
                )
            )
            val start = storage.retrieve().value.toInt()
            storage.inc()
            delay(5000)
            val end = storage.retrieve().value.toInt()
            assert(end == start + 1)
        }
    }

    @Test
    fun testStorageStoreAndRetrieve() {
        runBlocking {
            val storage = Storage(
                ethereum1,
                Transaction(
                    from = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73"),
                    to = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78")
                )
            )
            val random = Random.Default.nextLong()
            storage.store(num = random.toBigInteger())
            delay(5000)
            val result = storage.retrieve().value.toLong()
            assert(random == result)
        }
    }

}
