/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.*
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
                val receipt = TransactionHandler.receipt(ethereum1,Storage.deploy(ethereum1, key.address))
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
            val receipt = TransactionHandler.receipt(storage.eth,storage.inc())
            assert(receipt.isSuccess)
            val end = storage.retrieve()
            assert(end == start.inc())
        }
    }

    @Test
    fun storageStoreAndRetrieve() {
        runBlocking {
            val random = Quantity(Random.Default.nextLong())
            val receipt = TransactionHandler.receipt(
                storage.eth,storage.store(num = random))
            assert(receipt.isSuccess)
            val result = storage.retrieve()
            assert(random == result)
        }
    }

}
