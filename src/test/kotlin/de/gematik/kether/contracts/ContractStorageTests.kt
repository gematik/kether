package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.TransactionHandler
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
class ContractStorageTests {

    companion object {
        val key = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        lateinit var storage: Storage

        @BeforeAll
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val ethereum1 = Eth(
                    Rpc(
                        "http://besu.lab.gematik.de:8547",
                        "ws://besu.lab.gematik.de:8546",
                        isSigner = true
                    )
                )
                val hash = Storage.deploy(ethereum1, key.address)
                TransactionHandler.register(ethereum1,hash)
                val receipt = TransactionHandler.popReceipt(hash)
                val storageAddress = receipt?.contractAddress!!
                assert(receipt.isSuccess)
                storage = Storage(
                    ethereum1,
                    Transaction(to = storageAddress, from = key.address)
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
