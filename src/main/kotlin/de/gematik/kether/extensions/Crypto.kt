package de.gematik.kether.extensions

import de.gematik.kether.eth.types.Address
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.hyperledger.besu.crypto.SECPPublicKey

/**
 * Calculates keccak hash.
 * @return keccak hash of string
 */
fun String.keccak() : ByteArray{
    return toByteArray().keccak()
}

/**
 * Calculates keccak hash.
 * @return keccak hash of string
 */
fun ByteArray.keccak() : ByteArray{
    return Keccak.Digest256().let{
        it.update(this)
        it.digest()
    }
}

fun SECPPublicKey.toAccountAddress(): Address {
    return Address(encodedBytes.toArray().keccak().copyOfRange(12, 32))
}
