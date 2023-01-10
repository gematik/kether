package de.gematik.kether.eth.types

import de.gematik.kether.crypto.accountStore
import de.gematik.kether.extensions.RlpEmpty
import de.gematik.kether.extensions.keccak
import de.gematik.kether.extensions.toRLP
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Created by rk on 08.08.2022.
 * gematik.de
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