package de.gematik.kether.contracts

import de.gematik.kether.abi.AbiBytes32
import de.gematik.kether.rpc.Eth
import de.gematik.kether.types.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
                    if (log.address?.value.contentEquals(baseTransaction.to?.value)) {
                        listOfEventDecoders.forEach {
                            it(log)?.let {
                                _events.emit(it)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun deploy(eth: Eth, from: Address, params: Data): TransactionReceipt {
            return runBlocking {
                eth.ethSendTransaction(
                    Transaction(
                        from = from,
                        gas = Quantity(1000000),
                        value = Quantity(0),
                        gasPrice = Quantity(0),
                        data = params
                    )
                ).result?.let {
                    val subscription = eth.ethSubscribe(SubscriptionTypes.newHeads).result!!
                    eth.notifications.first { it.params.subscription == subscription }
                    eth.ethUnsubscribe(subscription)
                    eth.ethGetTransactionReceipt(it).result ?: throw Exception("no result")
                } ?: throw Exception("no transaction hash")
            }
        }

        fun checkEvent(log: Log, eventType: ByteArray): Log? {
            return if (log.topics?.get(0)?.value.contentEquals(eventType)) {
                log
            } else {
                null
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }

    fun call(params: Data): Data {
        return eth.ethCall(
            baseTransaction.copy(
                data = params
            ),
            Quantity(Block.latest.value)
        ).result ?: throw Exception("no result")
    }

    suspend fun transact(params: Data): TransactionReceipt {
        return withTimeout(10000) {
            eth.ethSendTransaction(
                baseTransaction.copy(
                    data = params
                )
            ).let {
                it.result ?: throw Exception(it.error?.message ?: "undefined error")
                val subscription = eth.ethSubscribe(SubscriptionTypes.newHeads).result
                    ?: throw Exception("subscription id missing")
                eth.notifications.first { it.params.subscription == subscription }
                eth.ethUnsubscribe(subscription)
                val receipt = it.result?.let {
                    eth.ethGetTransactionReceipt(it)
                }?.result
                receipt ?: throw Exception(it.error?.message ?: "undefined error")
            }
        }
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

}