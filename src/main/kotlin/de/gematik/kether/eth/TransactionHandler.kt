/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.kether.eth

import de.gematik.kether.eth.types.SubscriptionTypes
import de.gematik.kether.eth.types.TransactionReceipt
import de.gematik.kether.eth.types.Data32
import de.gematik.kether.rpc.Rpc
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.io.File
import javax.swing.text.StyledEditorKit.BoldAction


object TransactionHandler {
    @OptIn(ExperimentalSerializationApi::class)
    @Synchronized
    fun loadPendingTransactions(filename: String) {
        with(File(filename)) {
            if (this.isFile()){
                var pt = Json.decodeFromString<MutableList<Triple<String,String?,Data32>>>(this.readText())
                var eths = mutableMapOf<String,Eth>()
                for (p in pt){
                    eths.getOrPut(p.first+"/"+p.second, defaultValue = { Eth(Rpc(url = p.first, wsUrl = p.second)) }).let {
                        register(it,p.third)
                    }
                }
            }
        }
    }
    fun savePendingTransactions(filename: String) {
        var pt = mutableListOf<Triple<String,String?,Data32>>()
        for (p in pendingTransactions.entries){
            for(q in p.value){
                pt.add(Triple(p.key.rpc.url,p.key.rpc.wsUrl,q))
            }
        }
        File(filename).writeText(Json.encodeToString(pt))
    }
    @Synchronized
    fun register(eth: Eth, transactionHash: Data32) {
        initTransactionReceipt(transactionHash, CompletableDeferred())
        if (pendingTransactions.containsKey(eth)) {
            pendingTransactions[eth]?.add(transactionHash)
        } else {
            pendingTransactions[eth] = mutableListOf(transactionHash)
            runBlocking {
                subscriptions[eth] = eth.ethSubscribe(SubscriptionTypes.newHeads)
            }
            GlobalScope.launch {
                collectReceipts(eth)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun receipt(eth: Eth, hash: Data32): TransactionReceipt {
        register(eth, hash)
        return popReceipt(hash)!!
    }

    fun deferredReceipt(eth: Eth, hash: Data32): CompletableDeferred<TransactionReceipt> {
        register(eth, hash)
        val r: CompletableDeferred<TransactionReceipt> = transactionReceipts[hash]!!
        removeTransactionReceipt(hash)
        return r
    }

    @OptIn(ExperimentalSerializationApi::class)
    val subscriptions = mutableMapOf<Eth, String>()
    @OptIn(ExperimentalSerializationApi::class)
    var pendingTransactions = mutableMapOf<Eth, MutableList<Data32>>()
    private var transactionReceipts = mutableMapOf<Data32, CompletableDeferred<TransactionReceipt>>()

    @Synchronized
    private fun removeTransactionReceipt(hash: Data32) {
        transactionReceipts.remove(hash)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun popReceipt(hash: Data32): TransactionReceipt? {
        if (transactionReceipts.containsKey(hash)) {
            transactionReceipts[hash]!!.await()
            val r: TransactionReceipt = transactionReceipts[hash]!!.getCompleted()
            removeTransactionReceipt(hash)
            return r
        }
        return null
    }

    @Synchronized
    private fun initTransactionReceipt(hash: Data32, receipt: CompletableDeferred<TransactionReceipt>) {
        transactionReceipts[hash] = receipt
    }

    private fun completeTransactionReceipt(hash: Data32, receipt: TransactionReceipt) {
        transactionReceipts[hash]!!.complete(receipt)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Synchronized
    private fun getTransactionReceipt(eth: Eth, hash: Data32): TransactionReceipt? {
        return eth.ethGetTransactionReceipt(hash)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun collectReceipts(eth: Eth) {
        while (pendingTransactions[eth]!!.size != 0) {
            eth.notifications.first { it.params.subscription == subscriptions[eth] }
            pendingTransactions[eth]?.iterator()?.let {
                while (it.hasNext()) {
                    val pt = it.next()
                    val receipt = getTransactionReceipt(eth, pt)
                    println(receipt)
                    if (receipt != null) {
                        completeTransactionReceipt(pt, receipt)
                        it.remove()
                        if (pendingTransactions[eth]!!.size == 0) {
                            pendingTransactions.remove(eth)
                            eth.ethUnsubscribe(subscriptions[eth]!!)
                            return
                        }

                    }
                }
            }
        }
    }
}

