package de.gematik.kether.abi.types

import de.gematik.kether.abi.DataEncoder

/**
 * Created by rk on 05.10.2022.
 * gematik.de
 */
interface AbiTuple {
    companion object : Dynamic {
        override fun isDynamic(): Boolean = true
    }
    fun encode(): DataEncoder = error("not implemented for output types")
}

interface Dynamic{
    fun isDynamic(): Boolean
}