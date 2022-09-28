package de.gematik.kether.eth

import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Storage
import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import kotlinx.serialization.ExperimentalSerializationApi
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
    }

    @Test
    fun ethBlockNumber() {
        val rpcResponse = ethereum1.ethBlockNumber()
        assert(rpcResponse > Quantity(0))
    }

    @Test
    fun ethChainId() {
        val rpcResponse = ethereum1.ethChainId()
        assert(rpcResponse > Quantity(0))
    }

    @Test
    fun ethGetBalance() {
        val rpcResponse = ethereum1.ethGetBalance(account2Address, Quantity(Tag.latest))
        assert(rpcResponse > Quantity(0))
    }

    @Test
    fun ethAccounts() {
        val rpcResponse = ethereum1.ethAccounts()
        assert(rpcResponse.isNotEmpty())
    }

    @Test
    fun ethGasPrice() {
        val rpcResponse = ethereum1.ethGasPrice()
        assert(rpcResponse >= Quantity(0))
    }

    @Test
    fun ethEstimateGas() {
        val rpcResponse = ethereum1.ethEstimateGas(
            Transaction(
                from = account2Address,
                data = DataEncoder()
                    .encode(Data4(Storage.functionStore))
                    .encode(Quantity(10))
                    .build()
            ),
        )
        assert(rpcResponse >= Quantity(0))
    }

}