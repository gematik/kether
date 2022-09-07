package de.gematik.kether.types

import de.gematik.kether.abi.AbiBytes32

/**
 * Created by rk on 07.09.2022.
 * gematik.de
 */
open class Event(val topics : List<AbiBytes32>)

