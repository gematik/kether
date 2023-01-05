package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
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
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class ContractGLDTokenRawTxTests {
    companion object {
        val account1 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        val account4 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_4)

        lateinit var gldToken: GLDToken

        @BeforeAll
        @JvmStatic
        fun gldTokenDeploy() {
            runBlocking {
                val ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8545", "ws://ethereum1.lab.gematik.de:8546"))
                val initialSupply = Quantity(1E18.toLong())
                val receipt = GLDToken.deploy(ethereum1, account1.address, initialSupply)
                val gLDTokenAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                gldToken = GLDToken(
                    ethereum1,
                    Transaction(to = gLDTokenAddress, from = account1.address)
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
        val balance = gldToken.balanceOf(account1.address)
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
//            launch {
//                val subscriptionId = gldToken.subscribe(GLDToken.eventTransfer)
//                gldToken.events.collect{
//                    if(it is GLDToken.EventTransfer){
//                        gldToken.unsubscribe(subscriptionId)
//                        cancel()
//                    }
//                }
//            }
            val receipt = gldToken.transfer(account4.address, Quantity(1E16.toLong()))
            assert(receipt.isSuccess)
            val balance = gldToken.balanceOf(account4.address)
            assert(balance == Quantity(1E16.toLong()))
        }
    }
}
