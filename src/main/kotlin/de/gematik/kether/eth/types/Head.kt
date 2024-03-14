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
data class Head  constructor(
    val author: Address? = null,
    val difficulty: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val totalDifficulty: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val extraData: Data? = null,
    val gasLimit: Quantity? = null,
    val gasUsed: Quantity? = null,
    val hash: Data32? = null,
    val logsBloom: Data? = null,
    val miner: Address? = null,
    val mixHash: Data32? = null,
    val nonce: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val number: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val parentHash: Data32? = null,
    val receiptsRoot: Data32? = null,
    val sealFields: List<Data>? = null,
    val sha3Uncles: Data32? = null,
    val size: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val stateRoot: Data32? = null,
    val timestamp: @Serializable(with = BigIntegerSerializer::class) BigInteger? = null,
    val transactionsRoot: Data32? = null,
    val uncles: List<Data>? = null,
    val transactions: List<Data>? = null
) : EthEvent