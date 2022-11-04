package de.gematik.kether.eth

import de.gematik.kether.abi.types.toTopic
import de.gematik.kether.contracts.HelloWorld
import de.gematik.kether.crypto.AccountStore
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
 * gematik.de
 */

@ExperimentalSerializationApi
class EthPubSubTests {

    companion object {
        val account1 = AccountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        lateinit var helloWorld: HelloWorld
        lateinit var ethereum1: Eth

        @BeforeAll
        @JvmStatic
        fun prepare() {
                ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546", isSigner = true))
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
            val receipt = HelloWorld.deploy(ethereum1, account1.address, "Hello World")
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