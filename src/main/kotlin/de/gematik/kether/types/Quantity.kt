package de.gematik.kether.types

import de.gematik.kether.rpc.QuantitySerializer
import kotlinx.serialization.Serializable

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */
@Serializable(with = QuantitySerializer::class)
class Quantity(val value: Long) {
    companion object{
        val blockLatest = Quantity(Block.latest.value)
        val blockEarlist = Quantity(Block.earlist.value)
        val blockPending = Quantity(Block.pending.value)
    }
    init {
        check(value >= -3) { "invalid value" }
    }
}

enum class Block(val value: Long){
    latest(-1),
    earlist(-2),
    pending(-3)
}

