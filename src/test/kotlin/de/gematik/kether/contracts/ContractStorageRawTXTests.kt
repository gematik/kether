package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.eth.types.Transaction
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractStorageRawTXTests {

    companion object {
        val account4 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_4)
        lateinit var storage: Storage

        @BeforeAll
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val ethereum1 = Eth(
                    Rpc(
                        "http://besu.lab.gematik.de:8545",
                        "ws://besu.lab.gematik.de:8546"
                    )
                )
                val receipt = Storage.deploy(ethereum1, account4.address)
                val storageAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                storage = Storage(
                    Eth(Rpc("http://besu.lab.gematik.de:8545", "ws://besu.lab.gematik.de:8546")),
                    Transaction(to = storageAddress, from = account4.address)
                )
            }
        }

        @AfterAll
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
