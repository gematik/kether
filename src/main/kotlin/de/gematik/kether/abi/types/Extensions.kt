package de.gematik.kether.abi.types

import de.gematik.kether.eth.types.Data32
import de.gematik.kether.eth.types.Quantity
import de.gematik.kether.extensions.keccak
import java.util.Arrays

/**
 * Created by rk on 07.09.2022.
 * gematik.de
 */

fun AbiBytes32.toTopic() = Data32(toByteArray().keccak())
fun AbiString.toTopic() = Data32(keccak())
