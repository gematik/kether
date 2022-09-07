package de.gematik.kether.types

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@kotlinx.serialization.Serializable
data class Filter (
    val fromBlock : Quantity? = null,
    val toBlock: Quantity? = null,
    val address: Address? = null,
    val topics: Array<Data32>? = null,
    val limit: Quantity? = null
)