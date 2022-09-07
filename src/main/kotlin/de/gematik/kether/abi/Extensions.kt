package de.gematik.kether.abi

import de.gematik.kether.types.Data32
import keccak

/**
 * Created by rk on 07.09.2022.
 * gematik.de
 */

fun AbiBytes32.toTopic() = keccak()
fun AbiString.toTopic() = keccak()