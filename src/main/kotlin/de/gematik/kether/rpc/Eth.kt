package de.gematik.kether.rpc

import de.gematik.kether.extensions.toRLP
import de.gematik.kether.types.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.Response

private val logger = KotlinLogging.logger{}

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class Eth(val rpc: Rpc) {

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
     * Returns the number of most recent block.
     * @return number of the current block.
     * @throws Exception if failure
     */
    fun ethBlockNumber() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_blockNumber, emptyList())))
    }

    /**
     * Returns the EIP155 chain ID used for transaction signing at the current best block. Null is returned if not available.
     * @return EIP155 Chain ID, or null if not available.
     * @throws Exception if failure
     */
    fun ethChainId() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_chainId, emptyList())))
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws Exception if failure
     */
    fun ethGetBalance(account: Address, blockNumber: Quantity) : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_getBalance, listOf(account, blockNumber))))
    }

    /**
     * Returns the current price per gas in wei.
     * @return current price per gas in wei.
     * @throws Exception if failure
     */
    fun ethGasPrice() : RpcResponse<Quantity> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_gasPrice, emptyList())))
    }

    /**
     * Returns the balance of the account of given address.
     * @return current balance in wei.
     * @throws Exception if failure
     */
    fun ethAccounts() : RpcResponse<List<Address>> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_accounts, emptyList())))
    }

    /**
     * Executes a new message call immediately without creating a transaction on the block chain.
     * @return the return value of executed contract.
     * @throws Exception if failure
     */
    fun ethCall(transaction: Transaction, blockNumber: Quantity) : RpcResponse<Data> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_call, listOf(transaction, blockNumber))))
    }

    /**
     * Creates new message call transaction or a contract creation, if the data field contains code.
     * @return transaction hash, or the zero hash if the transaction is not yet available.
     * @throws Exception if failure
     */
    fun ethSendTransaction(transaction: Transaction) : RpcResponse<Data32> {
        return deserialize(rpc.call(RpcRequest(RpcMethods.eth_sendTransaction, listOf(transaction))))
    }

    fun ethSubscribe(type: SubscriptionTypes, filter: Filter = Filter()) : RpcResponse<String> {
        return runBlocking {
            when(type){
                SubscriptionTypes.newHeads -> rpc.send(RpcRequest(RpcMethods.eth_subscribe, listOf(type.name)))
                SubscriptionTypes.logs -> rpc.send(RpcRequest(RpcMethods.eth_subscribe, listOf(type.name, filter)))
            }
            @Suppress("UNCHECKED_CAST")
            rpc.responses.first() as RpcResponse<String>
        }
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