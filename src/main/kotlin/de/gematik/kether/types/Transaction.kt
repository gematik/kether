package de.gematik.kether.types

import java.math.BigInteger

/**
 * Created by rk on 08.08.2022.
 * gematik.de
 */


@kotlinx.serialization.Serializable
data class Transaction(
    val from: Address? = null,
    val to: Address? = null,
    val gas: Quantity? = null,
    val gasPrice: Quantity? = null,
    val value: Quantity? = null,
    val data: Data?=null,
    val nonce: Quantity? = null
)