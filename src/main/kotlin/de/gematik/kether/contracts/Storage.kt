import de.gematik.kether.abi.AbiUint256
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class Storage constructor(
    val eth: Eth,
    val baseTransaction: Transaction = Transaction()
) {
    companion object {
        val retrieveSelector = "retrieve()".keccak().copyOfRange(0, 4)
        val incSelector = "inc()".keccak().copyOfRange(0, 4)
        val storeSelector = "store(uint256)".keccak().copyOfRange(0, 4)
    }

    // retrieve():(uint256)
    data class ResultsRetrieve(
        val value: AbiUint256
    )

    fun retrieve(): ResultsRetrieve {
        return eth.ethCall(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(retrieveSelector)
                    .build()
            ),
            Quantity(Block.latest.value)
        ).result!!.let {
            ResultsRetrieve(DataDecoder(it).next())
        }
    }

    // inc()
    fun inc() {
        eth.ethSendTransaction(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(incSelector)
                    .build()
            )
        )

    }

    // store(uint256)
    fun store(num: AbiUint256) {
        eth.ethSendTransaction(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(storeSelector)
                    .encode(num)
                    .build()
            )
        )
    }

}
