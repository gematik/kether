package de.gematik.kether.eth

import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Storage
import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class EthRawTransactionsSecp256r1Tests {
    companion object {
        val account2 = accountStore.getAccount(AccountStore.TEST_ACCOUNT_2_R)
        var besu = Eth(Rpc("http://besu.lab.gematik.de:8545", "ws://besu.lab.gematik.de:8546"))
        lateinit var storageAddress: Address

        @BeforeAll
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val eth = Eth(Rpc("http://besu.lab.gematik.de:8545", "ws://besu.lab.gematik.de:8546"))
                val receipt = TransactionHandler.receipt(eth,Storage.deploy(eth, account2.address))
                storageAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                eth.close()
            }
        }
    }

    @Test
    fun ethSendRawTransaction() {
        val rpcResponse = besu.ethSendRawTransaction(
            Transaction(
                nonce = besu.ethGetTransactionCount(account2.address, Quantity(Tag.pending)),
                gasPrice = Quantity(0),
                gas = Quantity(100000),
                value = Quantity(0),
                to = storageAddress,
                from = account2.address,
                data = DataEncoder()
                    .encode(Data4(Storage.functionInc))
                    .build()
            ).sign(besu.chainId)
        )
        assert(rpcResponse != Data32("0x0")) // transaction hash not equal null hash
    }

    @Test
    fun ethSendRawTransactionWithParameters() {
        val num = Quantity(10)
        val rpcResponse = besu.ethSendRawTransaction(
            Transaction(
                nonce = besu.ethGetTransactionCount(account2.address, Quantity(Tag.pending)),
                gasPrice = Quantity(0),
                gas = Quantity(100000),
                value = Quantity(0),
                to = storageAddress,
                from = account2.address,
                data = DataEncoder()
                    .encode(Data4(Storage.functionStore))
                    .encode(num)
                    .build()
            ).sign(besu.chainId)
        )
        assert(rpcResponse != Data32("0x0")) // transaction hash not equal null hash
    }

}