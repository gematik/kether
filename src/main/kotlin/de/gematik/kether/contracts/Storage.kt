package de.gematik.kether.contracts

import de.gematik.kether.abi.AbiUint256
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.extensions.keccak
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.Event
import de.gematik.kether.types.Log
import de.gematik.kether.types.Transaction
import de.gematik.kether.types.TransactionReceipt
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class Storage(
    eth: Eth,
    baseTransaction: Transaction = Transaction()
) : Contract(eth, baseTransaction) {
    companion object {
        // deployment
// deployment data (bytecode) not available
// 4 byte selectors (functions) and topics (events)
        val functionInc = "inc()".keccak().copyOfRange(0, 4)
        val functionRetrieve = "retrieve()".keccak().copyOfRange(0, 4)
        val functionStore = "store(uint256)".keccak().copyOfRange(0, 4)
    }

    // events
    override val listOfEventDecoders: List<(Log) -> Event?> = listOf()

    // functions
    suspend fun inc(): TransactionReceipt {
        val params = DataEncoder()
            .encodeSelector(functionInc).build()
        return transact(params)
    }

    data class ResultsRetrieve(
        val value: AbiUint256
    )

    fun retrieve(): ResultsRetrieve {
        val params = DataEncoder()
            .encodeSelector(functionRetrieve).build()
        val decoder = DataDecoder(call(params))
        return ResultsRetrieve(
            decoder
                .next()
        )
    }

    suspend fun store(num: AbiUint256): TransactionReceipt {
        val params = DataEncoder()
            .encodeSelector(functionStore)
            .encode(num).build()
        return transact(params)
    }
}
