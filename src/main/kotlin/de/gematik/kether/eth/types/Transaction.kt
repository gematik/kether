package de.gematik.kether.eth.types

import de.gematik.kether.crypto.AccountStore
import de.gematik.kether.crypto.accountStore
import de.gematik.kether.extensions.RlpEmpty
import de.gematik.kether.extensions.keccak
import de.gematik.kether.extensions.toRLP
import kotlinx.serialization.ExperimentalSerializationApi
import org.apache.tuweni.bytes.Bytes32
import org.hyperledger.besu.crypto.SECP256K1
import org.hyperledger.besu.crypto.SECP256R1
import java.math.BigInteger

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
        val signer = when(account.keyType){
            AccountStore.KeyType.SECP256K1 -> SECP256K1()
            AccountStore.KeyType.SECP256R1 -> SECP256R1().apply {
                disableNative()
            }
        }
        val secKeyPair = signer.createKeyPair(account.privateKey)
        val signature = signer.sign(Bytes32.wrap(hash), secKeyPair)
        // EIP-155: "... v of the signature MUST be set to {0,1} + CHAIN_ID * 2 + 35
        // where {0,1} is the parity of the y value of the curve point for which r
        // is the x-value in the secp256k1 signing process.
        val v = BigInteger(byteArrayOf(signature.recId)) + chainId.toBigInteger() * BigInteger.TWO + 35.toBigInteger()
        return Data(
            listOf(
                nonce?.toRLP() ?: RlpEmpty,
                gasPrice?.toRLP() ?: RlpEmpty,
                gas?.toRLP() ?: RlpEmpty,
                to?.toRLP() ?: RlpEmpty,
                value?.toRLP() ?: RlpEmpty,
                data?.toRLP() ?: RlpEmpty,
                v.toRLP(),
                signature.r.toRLP(),
                signature.s.toRLP()
            ).toRLP()
        )
    }
}