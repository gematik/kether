package de.gematik.kether.contracts

import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.rpc.Rpc
import de.gematik.kether.eth.types.Transaction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractGLDTokenTests {
    companion object {
        val account1Address = Address("0xB389e2Ac92361c81481aFeF1cBF29881005996a3")
        val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        lateinit var gldToken: GLDToken

        @BeforeAll
        @JvmStatic
        fun gldTokenDeploy() {
            runBlocking {
                val ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))
                val initialSupply = Quantity(1E18.toLong())
                val receipt = GLDToken.deploy(ethereum1, account2Address, initialSupply)
                val gLDTokenAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                gldToken = GLDToken(
                    ethereum1,
                    Transaction(to = gLDTokenAddress, from = account2Address)
                )
            }
        }

        @AfterAll
        @JvmStatic
        fun cancelGldToken() {
            gldToken.cancel()
        }
    }

    @Test
    fun gldTokenBalanceOf() {
        val balance = gldToken.balanceOf(account2Address)
        assert(balance == Quantity(1E18.toLong()))
    }

    @Test
    fun gldTokenName() {
        val name = gldToken.name()
        assert(name == "Gold")
    }

    @Test
    fun gldTokenSymbol() {
        val symbol = gldToken.symbol()
        assert(symbol == "GLD")
    }

    @Test
    fun gldTokenTransfer() {
        runBlocking {
            launch {
                val subscriptionId = gldToken.subscribe(GLDToken.eventTransfer)
                gldToken.events.collect{
                    if(it is GLDToken.EventTransfer){
                        gldToken.unsubscribe(subscriptionId)
                        cancel()
                    }
                }
            }
            val receipt = gldToken.transfer(account1Address, Quantity(1E16.toLong()))
            assert(receipt.isSuccess)
            val balance = gldToken.balanceOf(account1Address)
            assert(balance == Quantity(1E16.toLong()))
        }
    }
}
