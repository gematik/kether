package de.gematik.kether.rpc

import HelloWorld
import de.gematik.kether.types.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import mu.KotlinLogging
import org.junit.Test
import java.util.*

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class EthPubSubTests {
    val eth = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))
    val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
    val helloWorldAddress = Address("0xa767c46126e850D7D6715Aa61Cb0D0aA9ACE6E5b")

    @Test
    fun ethSubscribeNewHeads() {
        runBlocking {
            val rpcResponse = eth.ethSubscribe(SubscriptionTypes.newHeads)
            assert(rpcResponse.result != null)
            val subscription = rpcResponse.result
            launch {
                eth.notifications.collect {
                    assert(subscription == it.params.subscription)
                    val newHead = it.params.result as Head
                    assert(newHead.number?.toInt()!=null)
                    cancel()
                }
            }
        }
    }

    @Test
    // requires a new log to succeed
    fun ethSubscribeLogs() {
        runBlocking {
            val rpcResponse = eth.ethSubscribe(
                SubscriptionTypes.logs,
                Filter(fromBlock = Quantity.blockLatest, toBlock = Quantity.blockLatest)
            )
            assert(rpcResponse.result != null)
            val subscription = rpcResponse.result
            launch {
                eth.notifications.collect {
                    assert(subscription == it.params.subscription)
                    val log = it.params.result as Log
                    assert(log.logIndex?.toInt()!=null)
                    cancel()
                }
            }
            launch {
                val helloWorld = HelloWorld(
                    eth,
                    Transaction(
                        from = account2Address,
                        to = helloWorldAddress
                    )
                )
                val greeting = "Greetings at ${Date()}"
                helloWorld.newGreeting(greeting = greeting)
            }
        }
    }

}