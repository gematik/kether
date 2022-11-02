package de.gematik.kether.abi.types

import de.gematik.kether.eth.types.Data20
import de.gematik.kether.eth.types.Data32
import de.gematik.kether.eth.types.Data4
import de.gematik.kether.eth.types.Quantity

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

typealias AbiUint = Quantity
typealias AbiUint8 = Quantity
typealias AbiUint32 = Quantity
typealias AbiUint256 = Quantity
typealias AbiString = String
typealias AbiSelector = Data4
typealias AbiBytes32 = Data32
typealias AbiAddress = Data20
typealias AbiBool = Boolean
