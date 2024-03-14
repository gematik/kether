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

import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import de.gematik.kether.rpc.types.RpcRequest
import de.gematik.kether.rpc.types.RpcResponse
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.Response
import java.io.Closeable

private val logger = KotlinLogging.logger{}



/**
 * Created by rk on 02.08.2022.
 */
@ExperimentalSerializationApi
class Eth(val rpc: Rpc) : Closeable {

    val chainId : Quantity by lazy {
        ethChainId()
    }

    /**
     * Provides a flow of notifications. Use [ethSubscribe] to subscribe for notifications and then [kotlinx.coroutines.flow.collect] to receive and process notifications. Please note that the result of a request is not a notification.
     * ##example
     * ```kt
     * val rpcResponse = eth.ethSubscribe()
     * ...
     * eth.notifications.collect{
     *    //process notifications here
     * }
     * ```
     */
    val notifications = rpc.notifications
    val responses = rpc.responses

    /**
     * Closes the used resources.
     */

    override fun close(){

        rpc.close()
    }

    /**
     * Returns the number of most recent block.
     * @return number of the current block.
     * @throws RpcException if failure
     */
    fun ethBlockNumber() : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_blockNumber, emptyList())).result()
    }

    /**
     * Returns the EIP155 chain ID used for transaction signing at the current best block. Null is returned if not available.
     * @return EIP155 Chain ID, or null if not available.
     * @throws RpcException if failure
     */
    fun ethChainId() : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_chainId, emptyList())).result()
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws RpcException if failure
     */
    fun ethGetBalance(account: Address, blockNumber: Quantity) : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_getBalance, listOf(account, blockNumber))).result()
    }

    /**
     * Returns the current price per gas in wei.
     * @return current price per gas in wei.
     * @throws RpcException if failure
     */
    fun ethGasPrice() : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_gasPrice, emptyList())).result()
    }

    /**
     * Makes a call or transaction, which won’t be added to the blockchain and returns the used gas, which can be used for estimating the used gas.
     * @param Transaction - where from field is optional and nonce field is ommited.
     * @param Quantity - (optional) Integer block number, or the string 'latest', 'earliest' or 'pending', see the default block parameter.Returns the current price per gas in wei.
     * @return current price per gas in wei.
     * @throws RpcException if failure
     */
    fun ethEstimateGas(transaction: Transaction, blockNumber: Quantity? = null) : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_estimateGas, if(blockNumber!=null) listOf(transaction, blockNumber) else listOf(transaction))).result()
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws RpcException if failure
     */
    fun ethAccounts() : List<Address> {
        return rpc.call(RpcRequest(EthMethods.eth_accounts, emptyList())).result()
    }

    /**
     * Returns the number of transactions sent from an address.
     * @return number of transactions sent from this address.
     * @throws RpcException if failure
     */
    fun ethGetTransactionCount(account: Address, blockNumber: Quantity) : Quantity {
        return rpc.call(RpcRequest(EthMethods.eth_getTransactionCount, listOf(account, blockNumber))).result()
    }

    /**
     * Executes a new message call immediately without creating a transaction on the block chain.
     * @param Transaction - where from field is optional and nonce field is ommited.
     * @param Quantity - (optional) Integer block number, or the string 'latest', 'earliest' or 'pending', see the default block parameter     * @return the return value of executed contract.
     * @throws RpcException if failure
     */
    fun ethCall(transaction: Transaction, blockNumber: Quantity? = null) : Data {
        return rpc.call(RpcRequest(EthMethods.eth_call, if(blockNumber!=null) listOf(transaction, blockNumber) else listOf(transaction))).result()
    }

    /**
     * Creates new message call transaction or a contract creation, if the data field contains code.
     * @param Transaction - with optional condition field.
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws RpcException if failure
     */
    fun ethSendTransaction(transaction: Transaction) : Data32 {
        return rpc.call(RpcRequest(EthMethods.eth_sendTransaction, listOf(transaction))).result()
    }

    /**
     * Creates new message call transaction or a contract creation for signed transactions.
     * @param Data - the signed transaction.
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws RpcException if failure
     */
    fun ethSendRawTransaction(transaction: Data) : Data32 {
        return rpc.call(RpcRequest(EthMethods.eth_sendRawTransaction, listOf(transaction))).result()
    }


    /**
     * Returns the receipt of a transaction by transaction hash. Note That the receipt is available even for pending transactions.
     * @param Data32 - transaction hash
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws RpcException if failure
     */
    fun ethGetTransactionReceipt(hash: Data32) : TransactionReceipt? {
        return rpc.call(RpcRequest(EthMethods.eth_getTransactionReceipt, listOf(hash))).resultOrNull()
    }

    /**
     * Starts a subscription (on WebSockets / IPC / TCP transports) to a particular event.
     * For every event that matches the subscription a JSON-RPC notification with event details
     * and subscription ID will be sent to a client.
     * @param type subscription type - either [SubscriptionTypes.newHeads] or [SubscriptionTypes.logs]
     * @return subscription id
     * @throws RpcRpcException if failure
     */
    suspend fun ethSubscribe(type: SubscriptionTypes, filter: Filter = Filter()) : String {
        when (type) {
            SubscriptionTypes.newHeads -> rpc.send(RpcRequest(EthMethods.eth_subscribe, listOf(type.name)))
            SubscriptionTypes.logs -> rpc.send(RpcRequest(EthMethods.eth_subscribe, listOf(type.name, filter)))
        }
        @Suppress("UNCHECKED_CAST")
        return responses.first().result()
    }

    /**
     * Unsubscribes from a subscription.
     * @param subscriptionId id of subscription to unsubscribe from
     * @return true if successful, false otherwise
     * @throws RpcException if failure
     */
    suspend fun ethUnsubscribe(subscriptionId: String) : Boolean {
        rpc.send(RpcRequest(EthMethods.eth_unsubscribe, listOf(subscriptionId)))
        @Suppress("UNCHECKED_CAST")
        return responses.first().result()
    }

}