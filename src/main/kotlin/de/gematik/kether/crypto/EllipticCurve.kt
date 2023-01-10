package de.gematik.kether.crypto

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.signers.DSAKCalculator
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.crypto.signers.RandomDSAKCalculator

enum class EllipticCurve {
    secp256r1 {
        override fun dsakCalculator(): DSAKCalculator = RandomDSAKCalculator()
    },
    secp256k1 {
        override fun dsakCalculator(): DSAKCalculator = HMacDSAKCalculator(SHA256Digest())
    };

    abstract fun dsakCalculator(): DSAKCalculator
}