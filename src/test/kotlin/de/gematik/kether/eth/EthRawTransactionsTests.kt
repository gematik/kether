package de.gematik.kether.eth

import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Storage
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class EthRawTransactionsTests {
    companion object {
        val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        val account2PrivateKey = BigInteger("8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63", 16)
        var ethereum1 = Eth(Rpc("http://ethereum1.lab.gematik.de:8545", "ws://ethereum1.lab.gematik.de:8546"))
        lateinit var storageAddress: Address

        @BeforeAll
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val eth = Eth(Rpc("http://ethereum1.lab.gematik.de:8545", "ws://ethereum1.lab.gematik.de:8546"))
                val receipt = Storage.deploy(eth, account2Address, account2PrivateKey)
                storageAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                eth.close()
            }
        }
    }

    @Test
    fun ethSendRawTransaction() {
        val rpcResponse = ethereum1.ethSendRawTransaction(
            Data(
                Transaction(
                    nonce = ethereum1.ethGetTransactionCount(account2Address, Quantity(Tag.pending)),
                    gasPrice = Quantity(0),
                    gas = Quantity(10000000),
                    value = Quantity(0),
                    to = storageAddress,
                    data = DataEncoder()
                        .encode(Data4(Storage.functionInc))
                        .build()
                ).sign(ethereum1.chainId, account2PrivateKey)
            )
        )
        assert(rpcResponse != Data32("0x0")) // transaction hash not equal null hash
    }

    @Test
    fun ethSendRawTransactionWithParameters() {
        val num = Quantity(10)
        val rpcResponse = ethereum1.ethSendRawTransaction(
            Data(
                Transaction(
                    nonce = ethereum1.ethGetTransactionCount(account2Address, Quantity(Tag.pending)),
                    gasPrice = Quantity(0),
                    gas = Quantity(10000000),
                    value = Quantity(0),
                    to = storageAddress,
                    data = DataEncoder()
                        .encode(Data4(Storage.functionStore))
                        .encode(num)
                        .build()
                ).sign(ethereum1.chainId, account2PrivateKey)
            )
        )
        assert(rpcResponse != Data32("0x0")) // transaction hash not equal null hash
    }

}