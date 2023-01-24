package de.gematik.kether.crypto

import de.gematik.kether.extensions.hexToByteArray
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.security.PrivateKey

open class EcdsaPrivateKey(
    private val encoded: ByteArray?,
    val curve: EllipticCurve
) : PrivateKey {

    internal val ecDomainParameters =
        SECNamedCurves.getByName(curve.name).let { ECDomainParameters(it.curve, it.g, it.n, it.h, it.seed) }

    init {
        if (encoded != null) {
            require(encoded.size == ecDomainParameters.curve.fieldSize / 8) { "invalid length of encoded key - ${ecDomainParameters.curve.fieldSize / 8} expected, but was ${encoded.size}" }
        }
    }

    constructor(
        encodedAsString: String,
        curve: EllipticCurve = EllipticCurve.secp256k1,
    ) : this(
        encodedAsString.hexToByteArray(),
        curve
    )

    override fun getAlgorithm(): String {
        return SignatureAlgorithm.ECDSA.name
    }

    override fun getFormat(): String? {
        return null
    }

    override fun getEncoded(): ByteArray? {
        return encoded
    }

    open fun sign(messageHash: ByteArray, publicKey: EcdsaPublicKey? = null): EcdsaSignature {
        val signer = ECDSASigner(curve.dsakCalculator())
        val ecPrivateKeyParameters = ECPrivateKeyParameters(
            BigInteger(1, encoded), ecDomainParameters
        )
        signer.init(true, ecPrivateKeyParameters);
        val components = signer.generateSignature(messageHash);
        return EcdsaSignature(components[0], components[1], curve).apply {
            publicKey?.let { extendSignature(it, messageHash) }
        }
    }

    open fun createEcdsaPublicKey(): EcdsaPublicKey {
        require(encoded!=null){"cannot create public key for private key null"}
        var privKey = BigInteger(1, encoded)
        if (privKey.bitLength() > ecDomainParameters.n.bitLength()) {
            privKey = privKey.mod(ecDomainParameters.n)
        }
        val point = FixedPointCombMultiplier().multiply(ecDomainParameters.g, privKey)
        return EcdsaPublicKey(point.getEncoded(false).copyOfRange(1, 65), curve)
    }
}