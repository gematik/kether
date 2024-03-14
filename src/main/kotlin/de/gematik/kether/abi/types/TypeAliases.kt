/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.kether.abi.types

import de.gematik.kether.eth.types.Data20
import de.gematik.kether.eth.types.Data32
import de.gematik.kether.eth.types.Data4
import de.gematik.kether.eth.types.Quantity

/**
 * Created by rk on 03.08.2022.
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
