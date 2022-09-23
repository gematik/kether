package de.gematik.kether.eth

import de.gematik.kether.eth.types.*
import de.gematik.kether.rpc.Rpc
import de.gematik.kether.rpc.types.RpcRequest
import de.gematik.kether.rpc.types.RpcResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.Response
import java.io.Closeable

private val logger = KotlinLogging.logger{}

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class Eth(val rpc: Rpc) : Closeable {

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

    /**
     * Closes the used resources.
     */

    override fun close(){

        rpc.close()
    }

    /**
     * Returns the number of most recent block.
     * @return number of the current block.
     * @throws Exception if failure
     */
    fun ethBlockNumber() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_blockNumber, emptyList())))
    }

    /**
     * Returns the EIP155 chain ID used for transaction signing at the current best block. Null is returned if not available.
     * @return EIP155 Chain ID, or null if not available.
     * @throws Exception if failure
     */
    fun ethChainId() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_chainId, emptyList())))
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws Exception if failure
     */
    fun ethGetBalance(account: Address, blockNumber: Quantity) : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_getBalance, listOf(account, blockNumber))))
    }

    /**
     * Returns the current price per gas in wei.
     * @return current price per gas in wei.
     * @throws Exception if failure
     */
    fun ethGasPrice() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_gasPrice, emptyList())))
    }

    /**
     * Makes a call or transaction, which won’t be added to the blockchain and returns the used gas, which can be used for estimating the used gas.
     * @param Transaction - where from field is optional and nonce field is ommited.
     * @param Quantity - (optional) Integer block number, or the string 'latest', 'earliest' or 'pending', see the default block parameter.Returns the current price per gas in wei.
     * @return current price per gas in wei.
     * @throws Exception if failure
     */
    fun ethEstimateGas(transaction: Transaction, blockNumber: Quantity? = null) : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_estimateGas, if(blockNumber!=null) listOf(transaction, blockNumber) else listOf(transaction))))
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws Exception if failure
     */
    fun ethAccounts() : RpcResponse<List<Address>> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_accounts, emptyList())))
    }

    /**
     * Executes a new message call immediately without creating a transaction on the block chain.
     * @param Transaction - where from field is optional and nonce field is ommited.
     * @param Quantity - (optional) Integer block number, or the string 'latest', 'earliest' or 'pending', see the default block parameter     * @return the return value of executed contract.
     * @throws Exception if failure
     */
    fun ethCall(transaction: Transaction, blockNumber: Quantity? = null) : RpcResponse<Data> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_call, if(blockNumber!=null) listOf(transaction, blockNumber) else listOf(transaction))))
    }

    /**
     * Creates new message call transaction or a contract creation, if the data field contains code.
     * @param Transaction - with optional condition field.
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws Exception if failure
     */
    fun ethSendTransaction(transaction: Transaction) : RpcResponse<Data32> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_sendTransaction, listOf(transaction))))
    }

    /**
     * Returns the receipt of a transaction by transaction hash. Note That the receipt is available even for pending transactions.
     * @param Data32 - transaction hash
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws Exception if failure
     */
    fun ethGetTransactionReceipt(hash: Data32) : RpcResponse<TransactionReceipt> {
        return deserialize(rpc.call(RpcRequest(EthMethods.eth_getTransactionReceipt, listOf(hash))))
    }

    /**
     * Starts a subscription (on WebSockets / IPC / TCP transports) to a particular event.
     * For every event that matches the subscription a JSON-RPC notification with event details
     * and subscription ID will be sent to a client.
     * @param type subscription type - either [SubscriptionTypes.newHeads] or [SubscriptionTypes.logs]
     * @return subscription id
     * @throws Exception if failure
     */
    suspend fun ethSubscribe(type: SubscriptionTypes, filter: Filter = Filter()) : RpcResponse<String> {
        return rpc.subscribe(type, filter)
    }

    /**
     * Unsubscribes from a subscription.
     * @param subscriptionId id of subscription to unsubscribe from
     * @return true if successful, false otherwise
     * @throws Exception if failure
     */
    suspend fun ethUnsubscribe(subscriptionId: String) : RpcResponse<Boolean> {
        return rpc.unsubscribe(subscriptionId)
    }

    private inline fun <reified T> deserialize(response: Response): RpcResponse<T> {
        val json = response.body!!.string()
        return deserialize(json)
    }

    private inline fun <reified T> deserialize(json: String): RpcResponse<T> {
        logger.debug (json.replace("\\s".toRegex(), ""))
        return Json.decodeFromString(json)
    }

}