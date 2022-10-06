package de.gematik.kether.abi.types

import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.eth.types.Data

/**
 * Created by rk on 05.10.2022.
 * gematik.de
 */
interface AbiTuple {
    fun encode(): DataEncoder
}