package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.TransactionHandler
import de.gematik.kether.eth.types.SubscriptionTypes
import de.gematik.kether.eth.types.Transaction
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.cancel
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
class ContractHelloWorldTests {

    companion object {
        val account4 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_4)
        lateinit var helloWorld: HelloWorld

        @BeforeAll
        @JvmStatic
        fun helloWorldDeploy() {
            runBlocking {
                val ethereum1 = Eth(Rpc("http://besu.lab.gematik.de:8547", "ws://besu.lab.gematik.de:8546", isSigner = true))
                val greet = "Hello World"
                val receipt = TransactionHandler.receipt(ethereum1,HelloWorld.deploy(ethereum1, account4.address, greet))
                val helloWorldAddress = receipt?.contractAddress!!
                assert(receipt.isSuccess)
                helloWorld = HelloWorld(
                    ethereum1,
                    Transaction(to = helloWorldAddress, from = account4.address)
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun cancelGldToken() {
            helloWorld.cancel()
        }
    }


    @Test
    fun helloWorldGreeting() {
        helloWorld.greeting()
    }

    @Test
    fun helloWorldNewGreeting() {
        runBlocking {
            val greeting = "Greetings at ${Date()}"
            launch {
                val receipt = TransactionHandler.receipt(helloWorld.eth,helloWorld.newGreeting(_greet = greeting))
                assert(receipt.isSuccess)
            }
            launch {
                helloWorld.eth.ethSubscribe(SubscriptionTypes.logs)
                helloWorld.eth.notifications.collect {
                    val result = helloWorld.greeting()
                    assert(greeting == result)
                    helloWorld.cancel()
                    cancel()
                }
            }
        }
    }

}
