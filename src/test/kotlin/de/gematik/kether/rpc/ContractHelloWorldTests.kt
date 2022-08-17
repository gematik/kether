package de.gematik.kether.rpc

import HelloWorld
import Storage
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.extensions.toRLP
import de.gematik.kether.types.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import java.util.*
import kotlin.random.Random

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractHelloWorldTests {
    val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
    val helloWorldAddress = Address("0xa767c46126e850D7D6715Aa61Cb0D0aA9ACE6E5b")

    val ethereum1 = Eth(Rpc("http:ethereum1.lab.gematik.de:8547", "http:ethereum1.lab.gematik.de:8546"))

    @Test
    fun helloWorldGreeting() {
        val helloWorld = HelloWorld(
            ethereum1,
            Transaction(to = helloWorldAddress)
        )
        helloWorld.greeting()
    }

    @Test
    fun helloWorldNewGreeting() {
        runBlocking {
            launch {
                val helloWorld = HelloWorld(
                    ethereum1,
                    Transaction(
                        from = account2Address,
                        to = helloWorldAddress
                    )
                )
                val greeting = "Greetings at ${Date()}"
                helloWorld.newGreeting(greeting = greeting)
                helloWorld.eth.ethSubscribe(SubscriptionTypes.logs)
                helloWorld.eth.notifications.collect {
                    val result = helloWorld.greeting().value
                    assert(greeting == result)
                    cancel()
                }
            }
        }
    }

}
