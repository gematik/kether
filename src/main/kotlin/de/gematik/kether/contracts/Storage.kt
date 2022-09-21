package de.gematik.kether.codegen

import de.gematik.kether.abi.*
import de.gematik.kether.contracts.Contract
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.keccak
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class Storage(
    eth: Eth,
    baseTransaction: Transaction = Transaction()
) : Contract(eth, baseTransaction) {
    companion object {
        // deployment
// deployment data (bytecode) not available
// selectors
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
