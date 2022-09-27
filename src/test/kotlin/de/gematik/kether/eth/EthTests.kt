package de.gematik.kether.eth

import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Storage
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.BeforeClass
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class EthTests {
    companion object {
        val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        val ethereum1 =  Eth(Rpc("http://ethereum1.lab.gematik.de:8547"))
        lateinit var storageAddress: Address

        @BeforeClass
        @JvmStatic
        fun storageDeploy() {
            runBlocking {
                val eth = Eth(Rpc("http://ethereum1.lab.gematik.de:8547", "ws://ethereum1.lab.gematik.de:8546"))
                val receipt = Storage.deploy(eth, account2Address)
                storageAddress = receipt.contractAddress!!
                assert(receipt.isSuccess)
                eth.close()
            }
        }
    }

    @Test
    fun ethBlockNumber() {
        val rpcResponse = ethereum1.ethBlockNumber()
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethChainId() {
        val rpcResponse = ethereum1.ethChainId()
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethGetBalance() {
        val rpcResponse = ethereum1.ethGetBalance(account2Address, Quantity(Tag.latest))
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethAccounts() {
        val rpcResponse = ethereum1.ethAccounts()
        assert(rpcResponse.result!!.isNotEmpty())
    }

    @Test
    fun ethGasPrice() {
        val rpcResponse = ethereum1.ethGasPrice()
        assert(rpcResponse.result!! >= Quantity(0))
    }

    @Test
    fun ethEstimateGas() {
        val rpcResponse = ethereum1.ethEstimateGas(
            Transaction(
                to = storageAddress,
                from = account2Address,
                data = DataEncoder()
                    .encode(Data4(Storage.functionStore))
                    .encode(Quantity(10))
                    .build()
            ),
        )
        assert(rpcResponse.result!! >= Quantity(0))
    }

    @Test
    fun ethCall() {
        val rpcResponse = ethereum1.ethCall(
            Transaction(
                to = storageAddress,
                data = DataEncoder().encode(Data4(Storage.functionRetrieve)).build()
            ),
            Quantity(Tag.latest)
        )
        assert(DataDecoder(rpcResponse.result!!).next<Quantity>() == Quantity(0))
    }

    @Test
    fun ethSendTransTransaction() {
        val num = Quantity(10)
        val rpcResponse = ethereum1.ethSendTransaction(
            Transaction(
                to = storageAddress,
                from = account2Address,
                data = DataEncoder()
                    .encode(Data4(Storage.functionStore))
                    .encode(num)
                    .build()
            )
        )
        assert(rpcResponse.result!! != Data32("0x0")) // transaction hash not equal null hash
    }

}