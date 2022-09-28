package de.gematik.kether.eth.types

import de.gematik.kether.eth.serializer.BigIntegerSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Log(
    val logIndex: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val removed: Boolean? = null,
    val blockNumber: Quantity? = null,
    val blockHash: Data32? = null,
    val transactionHash: Data32? = null,
    val transactionIndex: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val address: Address? = null,
    val data: Data? = null,
    val topics: List<Data32>? = null
) : EthEvent