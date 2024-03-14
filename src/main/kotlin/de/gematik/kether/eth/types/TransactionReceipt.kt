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

import java.math.BigInteger

/**
 * Created by rk on 14.09.2022.
 */
@kotlinx.serialization.Serializable
data class TransactionReceipt(
    val blockHash: Data32,
    val blockNumber: Quantity,
    val contractAddress: Address?=null,
    val cumulativeGasUsed: Quantity,
    val from: Address,
    val to: Address?=null,
    val gasUsed: Quantity,
    val effectiveGasPrice: Quantity,
    val logs: List<Log>,
    val logsBloom: Data,
    val root: Data32? = null,
    val status: Quantity,
    val transactionHash: Data32,
    val transactionIndex: Quantity,
    val type: Quantity? = null
){
    val isSuccess = status.toBigInteger() == BigInteger.ONE
}

