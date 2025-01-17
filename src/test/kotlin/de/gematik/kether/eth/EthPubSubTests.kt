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

package de.gematik.kether.eth

import de.gematik.kether.abi.types.toTopic
import de.gematik.kether.contracts.HelloWorld
import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Created by rk on 02.08.2022.
 */

@ExperimentalSerializationApi
class EthPubSubTests {

    companion object {
        val account1 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1_R)
        lateinit var helloWorld: HelloWorld
        lateinit var ethereum1: Eth

        @BeforeAll
        @JvmStatic
        fun prepare() {
                ethereum1 = Eth(Rpc("http://besu.lab.gematik.de:8547", "ws://besu.lab.gematik.de:8546", isSigner = true))
        }

        @AfterAll
        @JvmStatic
        fun cleanUp() {
            ethereum1.close()
        }
    }

    @Test
    fun ethSubscribeNewHeads() {
        runBlocking {
            val subscription = ethereum1.ethSubscribe(SubscriptionTypes.newHeads)
            val newHead = ethereum1.notifications.first { it.params.subscription == subscription }.result<Head>()
            assert(newHead.number?.toInt() != null)
            val isSuccess = ethereum1.ethUnsubscribe(subscription)
            assert(isSuccess)
        }
    }

    @Test
    fun ethSubscribeLogs() {
        runBlocking {
            val newGreeting = "Greetings at ${Date()}"
            val receipt = TransactionHandler.receipt(ethereum1,HelloWorld.deploy(ethereum1, account1.address, "Hello World"))
            val helloWorldAddress = receipt.contractAddress!!
            assert(receipt.isSuccess)
            helloWorld = HelloWorld(
                ethereum1,
                Transaction(to = helloWorldAddress, from = account1.address)
            )

            launch {
                val subscription = ethereum1.ethSubscribe(SubscriptionTypes.logs)
                val log = ethereum1.notifications.first { it.params.subscription == subscription }.result<Log>()
                assert(log.topics?.get(0)?.toByteArray()?.contentEquals(HelloWorld.eventModified.toByteArray()) == true)
                assert(log.topics?.get(2)?.toByteArray()?.contentEquals(newGreeting.toTopic().toByteArray()) == true)
                cancel()
                helloWorld.cancel()
            }

            //firing log
            launch {
                helloWorld.newGreeting(_greet = newGreeting)
            }
        }
    }

}