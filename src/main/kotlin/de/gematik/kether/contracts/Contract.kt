package de.gematik.kether.contracts

import de.gematik.kether.eth.*
import de.gematik.kether.eth.types.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import java.math.BigInteger

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
                    if (log.address?.toByteArray().contentEquals(baseTransaction.to?.toByteArray())) {
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
                val transaction = Transaction(
                    from = from,
                    value = Quantity(BigInteger.ZERO),
                    gasPrice = Quantity(BigInteger.ZERO),
                    data = params
                )
                val estimatedGas = eth.ethEstimateGas(transaction).result
                eth.ethSendTransaction(
                    transaction.copy(
                        gas = estimatedGas
                    )
                ).result?.let {
                    val subscription = eth.ethSubscribe(SubscriptionTypes.newHeads).result!!
                    eth.notifications.first { it.params.subscription == subscription }
                    eth.ethUnsubscribe(subscription)
                    eth.ethGetTransactionReceipt(it).result ?: throw Exception("no result")
                } ?: throw Exception("no transaction hash")
            }
        }

        fun checkEvent(log: Log, eventType: Data32): Log? {
            return if (log.topics?.get(0)?.toByteArray().contentEquals(eventType.toByteArray())) {
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
            Quantity(Tag.latest)
        ).result ?: throw Exception("no result")
    }

    suspend fun transact(params: Data): TransactionReceipt {
        return withTimeout(10000) {
            val transaction = baseTransaction.copy(
                data = params
            )
            eth.ethSendTransaction(
                transaction.copy(
                    gas = eth.ethEstimateGas(transaction).result
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

    suspend fun subscribe(eventSelector: Data32): String? {
        return eth.ethSubscribe(
            SubscriptionTypes.logs,
            Filter(
                fromBlock = Quantity(Tag.latest),
                toBlock = Quantity(Tag.latest),
                address = baseTransaction.to,
                topics = arrayOf(eventSelector)
            )
        ).result
    }

    suspend fun unsubscribe(subscriptionId: String) : Boolean {
        return eth.ethUnsubscribe(subscriptionId).result?:false
    }

}