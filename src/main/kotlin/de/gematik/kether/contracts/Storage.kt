package de.gematik.kether.contracts

import de.gematik.kether.abi.*
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.*
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.keccak
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
            .encode(Data4(functionInc)).build()
        return transact(params)
    }

    data class ResultsRetrieve(
        val value: AbiUint256
    )

    fun retrieve(): ResultsRetrieve {
        val params = DataEncoder()
            .encode(Data4(functionRetrieve)).build()
        val decoder = DataDecoder(call(params))
        return ResultsRetrieve(
            decoder
                .next()
        )
    }

    suspend fun store(num: AbiUint256): TransactionReceipt {
        val params = DataEncoder()
            .encode(Data4(functionStore))
            .encode(num).build()
        return transact(params)
    }
}
