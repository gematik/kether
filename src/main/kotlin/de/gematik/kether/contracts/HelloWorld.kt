import de.gematik.kether.abi.AbiBytes32
import de.gematik.kether.abi.AbiString
import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class HelloWorld(
    val eth: Eth,
    val baseTransaction: Transaction = Transaction()
) {
    companion object {
        val functionNewGreeting = "newGreeting(string)".keccak().copyOfRange(0, 4)
        val functionGreeting = "greeting()".keccak().copyOfRange(0, 4)
        val functionKill = "kill()".keccak().copyOfRange(0, 4)
        val eventModified = "Modified(string,string,string,string)".keccak()
    }

    private val scope = CoroutineScope(CoroutineName("HelloWorld"))

    private val _events = MutableSharedFlow<Event>()
    internal val events = _events.asSharedFlow()

    init {
        scope.launch {
            eth.notifications.collect {
                val log = it.params.result as Log
                processEventModified(log)?.let { _events.emit(it) }
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }

    // Modified(string,string,string,string)
    data class EventModified(
        val eventSelector: AbiBytes32,
        val oldGreetingIdx: AbiBytes32,
        val newGreetingIdx: AbiBytes32,
        val oldGreeting: AbiString,
        val newGreeting: AbiString
    ) : Event(listOf(eventSelector, oldGreetingIdx, newGreetingIdx))

    fun processEventModified(log: Log): Event? {
        if (!log.address?.value.contentEquals(baseTransaction.to?.value)) return null
        if (!log.topics?.get(0)?.value.contentEquals(eventModified)) return null
        val decoder = DataDecoder(log.data!!)
        val oldGreeting = decoder.next<AbiString>()
        val newGreeting = decoder.next<AbiString>()
        val event = EventModified(
            eventSelector = log.topics!!.get(0).value,
            oldGreetingIdx = log.topics.get(1).value,
            newGreetingIdx = log.topics.get(2).value,
            oldGreeting = oldGreeting,
            newGreeting = newGreeting
        )
        return event
    }

    suspend fun subscribe(eventSelector: AbiBytes32): String? {
        return eth.ethSubscribe(
            SubscriptionTypes.logs,
            Filter(
                fromBlock = Quantity.blockLatest,
                toBlock = Quantity.blockLatest,
                address = baseTransaction.to,
                topics = arrayOf(Data32(eventSelector))
            )
        ).result
    }

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
