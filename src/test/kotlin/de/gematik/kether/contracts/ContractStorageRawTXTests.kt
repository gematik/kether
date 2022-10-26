package de.gematik.kether.contracts

import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.eth.types.Transaction
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.math.BigInteger
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractStorageRawTXTests {

    companion object {
        val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        val account2PrivateKey = BigInteger("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63", 16)
        lateinit var storage: Storage

        @BeforeClass
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val receipt = Storage.deploy(
                    Eth(
                        Rpc(
                            "http://ethereum1.lab.gematik.de:8545",
                            "ws://ethereum1.lab.gematik.de:8546"
                        )
                    ), account2Address, account2PrivateKey
                )
                val storageAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                storage = Storage(
                    Eth(Rpc("http://ethereum1.lab.gematik.de:8545", "ws://ethereum1.lab.gematik.de:8546")),
                    Transaction(to = storageAddress, from = account2Address),
                    account2PrivateKey
                )
            }
        }

        @AfterClass
        @JvmStatic
        fun cancelStorage() {
            storage.cancel()
        }
    }

    @Test
    fun storageRetrieve() {
        storage.retrieve()
    }

    @Test
    fun storageIncAndRetrieve() {
        runBlocking {
            val start = storage.retrieve()
            val receipt = storage.inc()
            assert(receipt.isSuccess)
            val end = storage.retrieve()
            assert(end == start.inc())
        }
    }

    @Test
    fun storageStoreAndRetrieve() {
        runBlocking {
            val random = Quantity(Random.Default.nextLong())
            val receipt = storage.store(num = random)
            assert(receipt.isSuccess)
            val result = storage.retrieve()
            assert(random == result)
        }
    }

}
