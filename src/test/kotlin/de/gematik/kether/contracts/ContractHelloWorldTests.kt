package de.gematik.kether.contracts

import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.SubscriptionTypes
import de.gematik.kether.eth.types.Transaction
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractHelloWorldTests {
    val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
    val helloWorldAddress = Address("0xa767c46126e850D7D6715Aa61Cb0D0aA9ACE6E5b")
    lateinit var ethereum1: Eth

    @Before
    fun init() {
        ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))
    }

    @After
    fun close() {
        if (this::ethereum1.isInitialized) {
            ethereum1.close()
        }
    }

    @Test
    fun helloWorldDeploy() {
        runBlocking {
            val receipt = HelloWorld.deploy(ethereum1, account2Address, "Hello World")
            assert(receipt.isSuccess)
            val helloWorld = HelloWorld(
                ethereum1,
                Transaction(
                    to = receipt.contractAddress,
                    from = account2Address
                ),
            )
            val greeting = helloWorld.greeting().value
            assert(greeting == "Hello World")
            helloWorld.kill()
        }
    }

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
            val helloWorld = HelloWorld(
                ethereum1,
                Transaction(
                    from = account2Address,
                    to = helloWorldAddress
                )
            )
            val greeting = "Greetings at ${Date()}"
            launch {
                val receipt = helloWorld.newGreeting(_greet = greeting)
                assert(receipt.isSuccess)
            }
            launch {
                helloWorld.eth.ethSubscribe(SubscriptionTypes.logs)
                helloWorld.eth.notifications.collect {
                    val result = helloWorld.greeting().value
                    assert(greeting == result)
                    helloWorld.cancel()
                    cancel()
                }
            }
        }
    }

}
