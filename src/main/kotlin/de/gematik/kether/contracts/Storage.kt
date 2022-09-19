import de.gematik.kether.abi.AbiUint256
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Contract
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class Storage constructor(
    eth: Eth,
    baseTransaction: Transaction = Transaction()
) : Contract(eth, baseTransaction) {
    companion object {
        // selectors
        val retrieveSelector = "retrieve()".keccak().copyOfRange(0, 4)
        val incSelector = "inc()".keccak().copyOfRange(0, 4)
        val storeSelector = "store(uint256)".keccak().copyOfRange(0, 4)
    }

    //events
    override val listOfEventDecoders: List<(Log) -> Event?> = emptyList()

    // functions
    // retrieve():(uint256)
    data class ResultsRetrieve(
        val value: AbiUint256
    )

    fun retrieve(): ResultsRetrieve {
        val params = DataEncoder()
            .encodeSelector(retrieveSelector)
            .build()
        val decoder = DataDecoder(call(params))
        return ResultsRetrieve(
            decoder
                .next()
        )
    }

    // inc()
    suspend fun inc(): TransactionReceipt {
        val params = DataEncoder()
            .encodeSelector(incSelector)
            .build()
        return transact(params)
    }

    // store(uint256)
    suspend fun store(num: AbiUint256): TransactionReceipt {
        val params = DataEncoder()
            .encodeSelector(storeSelector)
            .encode(num)
            .build()
        return transact(params)
    }

}
