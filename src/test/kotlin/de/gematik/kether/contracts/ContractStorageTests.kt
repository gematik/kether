package de.gematik.kether.contracts

import de.gematik.kether.codegen.Storage
import de.gematik.kether.rpc.Eth
import de.gematik.kether.rpc.Rpc
import de.gematik.kether.types.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractStorageTests {
    val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
    val storageAddress = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78")

    val ethereum1 = Eth(Rpc("http:ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))

    @Test
    fun storageRetrieve() {
        val storage = Storage(
            ethereum1,
            Transaction(to = storageAddress)
        )
        storage.retrieve()
    }

    @Test
    fun storageIncAndRetrieve() {
        runBlocking {
            val storage = Storage(
                ethereum1,
                Transaction(
                    from = account2Address,
                    to = storageAddress
                )
            )
            val start = storage.retrieve().value
            val receipt = storage.inc()
            assert(receipt.isSuccess)
            val end = storage.retrieve().value
            assert(end == start.inc())
        }
    }

    @Test
    fun storageStoreAndRetrieve() {
        runBlocking {
            val storage = Storage(
                ethereum1,
                Transaction(
                    from = account2Address,
                    to = storageAddress
                )
            )
            val random = Random.Default.nextLong()
            val receipt = storage.store(num = random.toBigInteger())
            assert(receipt.isSuccess)
            val result = storage.retrieve().value.toLong()
            assert(random == result)
        }
    }

}
