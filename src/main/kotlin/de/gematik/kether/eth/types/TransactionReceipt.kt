package de.gematik.kether.eth.types

import java.math.BigInteger

/**
 * Created by rk on 14.09.2022.
 * gematik.de
 */
@kotlinx.serialization.Serializable
data class TransactionReceipt(
    val blockHash: Data32,
    val blockNumber: Quantity,
    val contractAddress: Address?=null,
    val cumulativeGasUsed: Quantity,
    val from: Address,
    val to: Address?=null,
    val gasUsed: Quantity,
    val effectiveGasPrice: Quantity,
    val logs: List<Log>,
    val logsBloom: Data,
    val root: Data32? = null,
    val status: Quantity,
    val transactionHash: Data32,
    val transactionIndex: Quantity,
    val type: Quantity? = null
){
    val isSuccess = status.toBigInteger() == BigInteger.ONE
}

