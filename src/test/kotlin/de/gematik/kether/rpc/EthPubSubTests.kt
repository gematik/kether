package de.gematik.kether.rpc

import de.gematik.kether.types.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import mu.KotlinLogging
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger {}

@ExperimentalSerializationApi
class EthPubSubTests {
    val eth = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))

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
        }
    }

}