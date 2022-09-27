package de.gematik.kether.eth

import de.gematik.kether.abi.types.toTopic
import de.gematik.kether.contracts.HelloWorld
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

@ExperimentalSerializationApi
class EthPubSubTests {

    companion object {
        val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        lateinit var helloWorld: HelloWorld
        lateinit var ethereum1: Eth

        @BeforeClass
        @JvmStatic
        fun helloWorldDeploy() {
            runBlocking {
                ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))
                val greet = "Hello World"
                val receipt = HelloWorld.deploy(ethereum1, account2Address, greet)
                val helloWorldAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                helloWorld = HelloWorld(
                    ethereum1,
                    Transaction(to = helloWorldAddress, from = account2Address)
                )
            }
        }

        @AfterClass
        @JvmStatic
        fun cancelGldToken() {
            helloWorld.cancel()
            ethereum1.close()
        }
    }

    @Test
    fun ethSubscribeNewHeads() {
        runBlocking {
            val subscription = ethereum1.ethSubscribe(SubscriptionTypes.newHeads).result!!
            val newHead = ethereum1.notifications.first { it.params.subscription == subscription }.params.result as Head
            assert(newHead.number?.toInt() != null)
            val isSuccess = ethereum1.ethUnsubscribe(subscription).result!!
            assert(isSuccess)
        }
    }

    @Test
    fun ethSubscribeLogs() {
        runBlocking {
            val newGreeting = "Greetings at ${Date()}"

            launch {
                ethereum1.ethSubscribe(SubscriptionTypes.logs)
                ethereum1.notifications.collect {
                    if (it.params.result is Log) {
                        val log = it.params.result as Log
                        assert(log.topics?.get(0)?.toByteArray()?.contentEquals(HelloWorld.eventModified.toByteArray()) == true)
                        assert(log.topics?.get(2)?.toByteArray()?.contentEquals(newGreeting.toTopic().toByteArray()) == true)
                        cancel()
                        helloWorld.cancel()
                    }
                }
            }

            //firing log
            launch {
                helloWorld.newGreeting(_greet = newGreeting)
            }
        }
    }

}