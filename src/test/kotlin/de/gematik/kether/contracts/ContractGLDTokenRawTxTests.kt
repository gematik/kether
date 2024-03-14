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

package de.gematik.kether.contracts

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.TransactionHandler
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
 */
@ExperimentalSerializationApi
class ContractGLDTokenRawTxTests {
    companion object {
        val account1 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1_R)
        val account4 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_4_R)

        lateinit var gldToken: GLDToken

        @BeforeAll
        @JvmStatic
        fun gldTokenDeploy() {
            runBlocking {
                val ethereum1 = Eth(Rpc("http://besu.lab.gematik.de:8545", "ws://besu.lab.gematik.de:8546"))
                val initialSupply = Quantity(1E18.toLong())
                val receipt = TransactionHandler.receipt(ethereum1,GLDToken.deploy(ethereum1, account1.address, initialSupply))
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
            val receipt = TransactionHandler.receipt(gldToken.eth,gldToken.transfer(account4.address, Quantity(1E16.toLong())))
            assert(receipt.isSuccess)
            val balance = gldToken.balanceOf(account4.address)
            assert(balance.toBigInteger().toLong() == 1E16.toLong())
        }
    }
}
