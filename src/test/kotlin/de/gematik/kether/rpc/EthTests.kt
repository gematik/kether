package de.gematik.kether.rpc

import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class EthTests {
    val eth = Eth(Rpc("http:ethereum1.lab.gematik.de:8547"))

    @Test
    fun ethBlockNumber() {
        val rpcResponse = eth.ethBlockNumber()
        assert(rpcResponse.result!!.value > 0)
    }

    @Test
    fun ethChainId() {
        val rpcResponse = eth.ethChainId()
        assert(rpcResponse.result!!.value > 0)
    }

    @Test
    fun ethGetBalance() {
        val rpcResponse =
            eth.ethGetBalance(Address("0xB389e2Ac92361c81481aFeF1cBF29881005996a3"), Quantity(Block.latest.value))
        assert(rpcResponse.result!!.value >= 0)
    }

    @Test
    fun ethAccounts() {
        val rpcResponse = eth.ethAccounts()
        assert(rpcResponse.result!!.isNotEmpty())
    }

    @Test
    fun ethGasPrice() {
        val rpcResponse = eth.ethGasPrice()
        assert(rpcResponse.result!!.value >= 0)
    }

    @Test
    fun ethCall() {
        val rpcResponse = eth.ethCall(
            Transaction(
                to = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78"),
                data = DataEncoder().encodeSelector(Storage.retrieveSelector).build()
            ),
            Quantity(Block.latest.value)
        )
        assert(rpcResponse.result!!.value.size >= 0)
    }

}