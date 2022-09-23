package de.gematik.kether.rpc

import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Storage
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class EthTests {
    val account2Address = Address("0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
    val storageAddress = Address("0x218d5fe2E168656eBDE49e7a4A3C97E699D0be78")

    val eth = Eth(Rpc("http:ethereum1.lab.gematik.de:8547"))

    @Test
    fun ethBlockNumber() {
        val rpcResponse = eth.ethBlockNumber()
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethChainId() {
        val rpcResponse = eth.ethChainId()
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethGetBalance() {
        val rpcResponse = eth.ethGetBalance(account2Address, Quantity(Tag.latest))
        assert(rpcResponse.result!! > Quantity(0))
    }

    @Test
    fun ethAccounts() {
        val rpcResponse = eth.ethAccounts()
        assert(rpcResponse.result!!.isNotEmpty())
    }

    @Test
    fun ethGasPrice() {
        val rpcResponse = eth.ethGasPrice()
        assert(rpcResponse.result!! >= Quantity(0))
    }

    @Test
    fun ethEstimateGas() {
        val rpcResponse = eth.ethEstimateGas(
            Transaction(
                to = storageAddress,
                from = account2Address,
                data = DataEncoder()
                    .encodeSelector(Storage.functionStore)
                    .encode(BigInteger.TEN)
                    .build()
            ),
        )
        assert(rpcResponse.result!! >= Quantity(0))
    }

    @Test
    fun ethCall() {
        val rpcResponse = eth.ethCall(
            Transaction(
                to = storageAddress,
                data = DataEncoder().encodeSelector(Storage.functionRetrieve).build()
            ),
            Quantity(Tag.latest)
        )
        assert(DataDecoder(rpcResponse.result!!).next<BigInteger>() != BigInteger.ZERO)
    }

    @Test
    fun ethSendTransTransaction() {
        val num = BigInteger.TEN
        val rpcResponse = eth.ethSendTransaction(
            Transaction(
                to = storageAddress,
                from = account2Address,
                data = DataEncoder()
                    .encodeSelector(Storage.functionStore)
                    .encode(num)
                    .build()
            )
        )
        assert(rpcResponse.result!! != Data32("0x0")) // transaction hash not equal null hash
    }

}