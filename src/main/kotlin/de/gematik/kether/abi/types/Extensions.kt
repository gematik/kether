package de.gematik.kether.abi

import de.gematik.kether.eth.types.Data32
import de.gematik.kether.extensions.keccak

/**
 * Created by rk on 07.09.2022.
 * gematik.de
 */

fun AbiBytes32.toTopic() = Data32(keccak())
fun AbiString.toTopic() = Data32(keccak())