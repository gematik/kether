import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
class HelloWorld constructor(
    val eth: Eth,
    val baseTransaction: Transaction = Transaction()
) {
    companion object {
        val newGreetingSelector = "newGreeting(string)".keccak().copyOfRange(0, 4)
        val greetingSelector = "greeting()".keccak().copyOfRange(0, 4)
        val killSelector = "kill()".keccak().copyOfRange(0, 4)
    }

    // greeting():(string)
    data class ResultsGreeting(
        val value: EthString
    )

    fun greeting(): ResultsGreeting {
        return eth.ethCall(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(greetingSelector)
                    .data()
            ),
            Quantity(Block.latest.value)
        ).result!!.let {
            ResultsGreeting(DataDecoder(it).next())
        }
    }

    // kill()
    fun kill() {
        eth.ethSendTransaction(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(killSelector)
                    .data()
            )
        )
    }

    // newGreeting(string)
    fun newGreeting(greeting: EthString) {
        eth.ethSendTransaction(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(newGreetingSelector)
                    .encode(greeting)
                    .data()
            )
        )
    }

}
