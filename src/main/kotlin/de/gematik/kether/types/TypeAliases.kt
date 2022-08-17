package de.gematik.kether.types

import de.gematik.kether.rpc.DataSerializer
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

typealias Address = Data20
typealias EthUint256 = BigInteger
typealias EthString = String
typealias EthSelector = ByteArray