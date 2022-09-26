package de.gematik.kether.abi.types

import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.Data32
import de.gematik.kether.eth.types.Data4
import de.gematik.kether.eth.types.Quantity
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

typealias AbiUint256 = Quantity
typealias AbiString = String
typealias AbiSelector = Data4
typealias AbiBytes32 = Data32
typealias AbiAddress = Address
typealias AbiBool = Boolean
typealias AbiUint8 = Quantity