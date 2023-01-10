package de.gematik.kether.extensions

import org.bouncycastle.jcajce.provider.digest.Keccak

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
