package de.gematik.kether.contracts

import de.gematik.kether.abi.AbiBytes32
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Created by rk on 09.09.2022.
 * gematik.de
 */

@OptIn(ExperimentalSerializationApi::class)
abstract class Contract(
    val eth: Eth,
    val baseTransaction: Transaction = Transaction()
) {
    private val scope = CoroutineScope(CoroutineName(this.toString()))

    protected val _events = MutableSharedFlow<Event>()
    internal val events = _events.asSharedFlow()

    protected abstract val listOfEventDecoders: List<(Log) -> Event?>

    init {
        scope.launch {
            eth.notifications.collect {
                if (it.params.result is Log) {
                    val log = it.params.result
                    listOfEventDecoders.forEach {
                        it(log)?.let {
                            _events.emit(it)
                        }
                    }
                }
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }

    fun subscribe(eventSelector: AbiBytes32): String? {
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

    protected fun checkEvent(log: Log, eventType: ByteArray): Log? {
        return if (log.address?.value.contentEquals(baseTransaction.to?.value) &&
            log.topics?.get(0)?.value.contentEquals(eventType)
        ) {
            log
        } else {
            null
        }
    }

}