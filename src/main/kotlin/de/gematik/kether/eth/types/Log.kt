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

package de.gematik.kether.eth.types

import de.gematik.kether.eth.serializer.BigIntegerSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 16.08.2022.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Log(
    val logIndex: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val removed: Boolean? = null,
    val blockNumber: Quantity? = null,
    val blockHash: Data32? = null,
    val transactionHash: Data32? = null,
    val transactionIndex: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val address: Address? = null,
    val data: Data? = null,
    val topics: List<Data32>? = null
) : EthEvent