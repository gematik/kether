package de.gematik.kether.contracts

import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.jsonObject
import java.math.BigInteger

/**
 * Created by rk on 09.09.2022.
 * gematik.de
 */

@OptIn(ExperimentalSerializationApi::class)
abstract class Contract(
    val eth: Eth,
    val baseTransaction: Transaction = Transaction(),
    val privateKey: BigInteger? = null
) {
    private val scope = CoroutineScope(CoroutineName(this.toString()))

    protected val _events = MutableSharedFlow<Event>()
    internal val events = _events.asSharedFlow()

    protected abstract val listOfEventDecoders: List<(Log) -> Event?>

    init {
        scope.launch {
            eth.notifications.filter { it.params.result.jsonObject.containsKey("logIndex") }.collect {
                val log = it.result<Log>()
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

    companion object {
        fun deploy(eth: Eth, from: Address, params: Data, privateKey: BigInteger? = null): TransactionReceipt {
            return runBlocking {
                val transaction = Transaction(
                    from = from,
                    value = Quantity(0),
                    gasPrice = Quantity(0),
                    data = params
                )
                transact(eth, transaction, privateKey)
            }
        }

        suspend fun transact(eth: Eth, transaction: Transaction, privateKey: BigInteger?): TransactionReceipt {
            require(transaction.from!=null) {"sender address required to send transaction"}
            return withTimeout(10000) {
                val tx = transaction.copy(
                    gas = eth.ethEstimateGas(transaction),
                    nonce = eth.ethGetTransactionCount(transaction.from, Quantity(Tag.pending))
                )
                if (privateKey != null) {
                    eth.ethSendRawTransaction(Data(tx.sign(chainId = eth.chainId, privateKey = privateKey)))
                } else {
                    eth.ethSendTransaction(tx)
                }.let {
                    val subscription = eth.ethSubscribe(SubscriptionTypes.newHeads)
                    eth.notifications.first { it.params.subscription == subscription }
                    eth.ethUnsubscribe(subscription)
                    val receipt = it.let {
                        eth.ethGetTransactionReceipt(it)
                    }
                    receipt
                }
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
        )
    }

    suspend fun transact(params: Data): TransactionReceipt {
        val transaction = baseTransaction.copy(
            data = params
        )
        return Companion.transact(eth, transaction, privateKey)
    }

    suspend fun subscribe(eventSelector: Data32): String {
        return eth.ethSubscribe(
            SubscriptionTypes.logs,
            Filter(
                fromBlock = Quantity(Tag.latest),
                toBlock = Quantity(Tag.latest),
                address = baseTransaction.to,
                topics = arrayOf(eventSelector)
            )
        )
    }

    suspend fun unsubscribe(subscriptionId: String): Boolean {
        return eth.ethUnsubscribe(subscriptionId)
    }

}