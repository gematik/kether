package de.gematik.kether.crypto

import de.gematik.kether.eth.types.Address
import de.gematik.kether.extensions.keccak
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.security.PublicKey

class EcdsaPublicKey(
    privateKey: EcdsaPrivateKey
) : PublicKey {
    val curve = privateKey.curve
    val algorithm = privateKey.algorithm
    val ecDomainParameters = privateKey.ecDomainParameters
    private val encoded = createEcdsaPublicKeyEncoded(privateKey)

    override fun getAlgorithm(): String {
        return algorithm.name
    }

    override fun getFormat(): String? {
        return null
    }

    override fun getEncoded(): ByteArray {
        return encoded
    }

    private fun createEcdsaPublicKeyEncoded(privateKey: EcdsaPrivateKey): ByteArray {
        var privKey = BigInteger(byteArrayOf(0x0) + privateKey.encoded)
        if (privKey.bitLength() > ecDomainParameters.n.bitLength()) {
            privKey = privKey.mod(ecDomainParameters.n)
        }
        val point = FixedPointCombMultiplier().multiply(ecDomainParameters.g, privKey)
        return point.getEncoded(false).copyOfRange(1, 65)
    }

    fun toAccountAddress(): Address {
        return Address(encoded.keccak().copyOfRange(12, 32))
    }

    fun verify(messageHash: ByteArray, ecdsaSignature: EcdsaSignature) : Boolean {
        val signer = ECDSASigner(curve.dsakCalculator());
        val ecPublicKeyParameters = ECPublicKeyParameters(ecDomainParameters.curve.decodePoint(byteArrayOf(4) + encoded), ecDomainParameters)
        signer.init(false, ecPublicKeyParameters)
        return kotlin.runCatching {
            signer.verifySignature(messageHash, ecdsaSignature.r, ecdsaSignature.s)
        }.getOrElse { false }
    }

}
