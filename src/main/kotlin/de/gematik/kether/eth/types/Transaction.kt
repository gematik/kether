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

import de.gematik.kether.crypto.accountStore
import de.gematik.kether.extensions.RlpEmpty
import de.gematik.kether.extensions.keccak
import de.gematik.kether.extensions.toRLP
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Created by rk on 08.08.2022.
 */


@kotlinx.serialization.Serializable
data class Transaction(
    val from: Address? = null,
    val to: Address? = null,
    val gas: Quantity? = null,
    val gasPrice: Quantity? = null,
    val value: Quantity? = null,
    val data: Data? = null,
    val nonce: Quantity? = null
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun sign(chainId: Quantity): Data {
        require(from != null) { "from required to sign transaction" }
        val hash = listOf(
            nonce?.toRLP() ?: RlpEmpty,
            gasPrice?.toRLP() ?: RlpEmpty,
            gas?.toRLP() ?: RlpEmpty,
            to?.toRLP() ?: RlpEmpty,
            value?.toRLP() ?: RlpEmpty,
            data?.toRLP() ?: RlpEmpty,
            // EIP-155: "... you SHOULD hash nine rlp encoded elements
            // (nonce, gasprice, startgas, to, value, data, chainid, 0, 0)."
            chainId.toRLP(),
            RlpEmpty,
            RlpEmpty
        ).toRLP().keccak()
        val account = accountStore.getAccount(from)
        check(account.keyPair.privateKey != null) { "error: no private key" }
        val signature = account.keyPair.privateKey.sign(hash, account.keyPair.publicKey)
        return Data(
            listOf(
                nonce?.toRLP() ?: RlpEmpty,
                gasPrice?.toRLP() ?: RlpEmpty,
                gas?.toRLP() ?: RlpEmpty,
                to?.toRLP() ?: RlpEmpty,
                value?.toRLP() ?: RlpEmpty,
                data?.toRLP() ?: RlpEmpty,
                signature.getV(chainId).toRLP(),
                signature.r.toRLP(),
                signature.s.toRLP()
            ).toRLP()
        )
    }
}