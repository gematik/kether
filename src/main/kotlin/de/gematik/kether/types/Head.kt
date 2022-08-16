package de.gematik.kether.types

import de.gematik.kether.rpc.BigIntegerSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Head  constructor(
    val author: Address? = null,
    val difficulty: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val totalDifficulty: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val extraData: Data? = null,
    val gasLimit: Quantity? = null,
    val gasUsed: Quantity? = null,
    val hash: Data32? = null,
    val logsBloom: Data? = null,
    val miner: Address? = null,
    val mixHash: Data32? = null,
    val nonce: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val number: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val parentHash: Data32? = null,
    val receiptsRoot: Data32? = null,
    val sealFields: List<Data>? = null,
    val sha3Uncles: Data32? = null,
    val size: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val stateRoot: Data32? = null,
    val timestamp: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val transactionsRoot: Data32? = null,
    val uncles: List<Data>? = null,
    val transactions: List<Data>? = null
)