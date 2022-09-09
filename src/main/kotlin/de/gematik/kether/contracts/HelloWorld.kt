import de.gematik.kether.abi.AbiBytes32
import de.gematik.kether.abi.AbiString
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.contracts.Contract
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class HelloWorld(
    eth: Eth,
    baseTransaction: Transaction = Transaction()
) : Contract(eth, baseTransaction) {

    companion object {
        val functionNewGreeting = "newGreeting(string)".keccak().copyOfRange(0, 4)
        val functionGreeting = "greeting()".keccak().copyOfRange(0, 4)
        val functionKill = "kill()".keccak().copyOfRange(0, 4)
        val eventModified = "Modified(string,string,string,string)".keccak()
    }

    // events
    // Modified(string,string,string,string)
    data class EventModified(
        val eventSelector: AbiBytes32,
        val oldGreetingIdx: AbiBytes32,
        val newGreetingIdx: AbiBytes32,
        val oldGreeting: AbiString,
        val newGreeting: AbiString
    ) : Event(topics = listOf(eventSelector, oldGreetingIdx, newGreetingIdx))

    private val decoderEventModified = { log: Log ->
        checkEvent(log, eventModified)?.let {
            val decoder = DataDecoder(log.data!!)
            val oldGreeting = decoder.next<AbiString>()
            val newGreeting = decoder.next<AbiString>()
            EventModified(
                eventSelector = log.topics!!.get(0).value,
                oldGreetingIdx = log.topics.get(1).value,
                newGreetingIdx = log.topics.get(2).value,
                oldGreeting = oldGreeting,
                newGreeting = newGreeting
            )
        }
    }

    override val listOfEventDecoders: List<(Log) -> Event?> = listOf(
        decoderEventModified
    )

    //functions
    // greeting():(string)
    data class ResultsGreeting(
        val value: AbiString
    )

    fun greeting(): ResultsGreeting {
        return eth.ethCall(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(functionGreeting)
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
                    .encodeSelector(functionKill)
                    .data()
            )
        )
    }

    // newGreeting(string)
    fun newGreeting(greeting: AbiString) {
        eth.ethSendTransaction(
            baseTransaction.copy(
                data = DataEncoder()
                    .encodeSelector(functionNewGreeting)
                    .encode(greeting)
                    .data()
            )
        )
    }

}
