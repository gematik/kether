package de.gematik.kether.crypto

import de.gematik.kether.extensions.hexToByteArray
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import java.math.BigInteger
import java.security.PrivateKey

class EcdsaPrivateKey(
    private val encoded: ByteArray,
    val curve: EllipticCurve,
    val algorithm: SignatureAlgorithm = SignatureAlgorithm.ECDSA
) : PrivateKey {

    internal val ecDomainParameters = SECNamedCurves.getByName(curve.name).let { ECDomainParameters(it.curve, it.g, it.n, it.h, it.seed) }

    init {
        require(encoded.size == ecDomainParameters.curve.fieldSize / 8) { "invalid length of encoded key - ${ecDomainParameters.curve.fieldSize / 8} expected, but was ${encoded.size}" }
    }

    constructor(
        encodedAsString: String,
        curve: EllipticCurve = EllipticCurve.secp256k1,
        algorithm: SignatureAlgorithm = SignatureAlgorithm.ECDSA
    ) : this(
        encodedAsString.hexToByteArray(),
        curve,
        algorithm
    )

    override fun getAlgorithm(): String {
        return algorithm.name
    }

    override fun getFormat(): String? {
        return null
    }

    override fun getEncoded(): ByteArray {
        return encoded
    }

    fun sign(messageHash: ByteArray, publicKey: EcdsaPublicKey? = null): EcdsaSignature {
        val signer = ECDSASigner(curve.dsakCalculator());
        val ecPrivateKeyParameters = ECPrivateKeyParameters(
            BigInteger(byteArrayOf(0) + encoded), ecDomainParameters
        );
        signer.init(true, ecPrivateKeyParameters);
        val components = signer.generateSignature (messageHash);
        return  EcdsaSignature(components[0], components[1], null, ecDomainParameters).apply { publicKey?.let{extendSignature(it, messageHash)}
    }
}}