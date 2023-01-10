package de.gematik.kether.crypto

import de.gematik.kether.eth.types.Quantity
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.x9.X9IntegerConverter
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.util.*

class EcdsaSignature(
    val r: BigInteger,
    s: BigInteger,
    var recId: Byte? = null,
    val ecDomainParameters: ECDomainParameters
) {

    val hashLength = 32

    // Automatically adjust the S component to be less than or equal to half the curve
    // order, if necessary. This is required because for every signature (r,s) the signature
    // (r, -s (mod N)) is a valid signature of the same message. However, we dislike the
    // ability to modify the bits of a Bitcoin transaction after it's been signed, as that
    // violates various assumed invariants. Thus in future only one of those forms will be
    // considered legal and the other will be banned.
    // The order of the curve is the number of valid points that exist on that curve.
    // If S is in the upper half of the number of valid points, then bring it back to
    // the lower half. Otherwise, imagine that
    // N = 10
    // s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
    // 10 - 8 == 2, giving us always the latter solution, which is canonical.
    val s = if (s > ecDomainParameters.curve.order / BigInteger.TWO) {
        ecDomainParameters.n - s
    } else {
        s
    }

    init {
        require(r.bitLength() <= ecDomainParameters.curve.fieldSize) { "r too big" }
        require(s.bitLength() <= ecDomainParameters.curve.fieldSize) { "s too big" }
    }

    constructor (
        r: BigInteger,
        s: BigInteger,
        recId: Byte? = null,
        curve: EllipticCurve = EllipticCurve.secp256k1
    ) : this(
        r,
        s,
        recId,
        SECNamedCurves.getByName(curve.name).let { ECDomainParameters(it.curve, it.g, it.n, it.h, it.seed) })

    constructor (
        r: BigInteger,
        s: BigInteger,
        v: BigInteger,
        chainId: Quantity,
        curve: EllipticCurve = EllipticCurve.secp256k1
    ) : this(
        r,
        s,
        (v - 35.toBigInteger() -  chainId.toBigInteger() * BigInteger.TWO).toByte(),
        curve
    )

    fun getV(chainId : Quantity) : BigInteger {
        // EIP-155: "... v of the signature MUST be set to {0,1} + CHAIN_ID * 2 + 35
        // where {0,1} is the parity of the y value of the curve point for which r
        // is the x-value in the secp256k1 signing process.
        check(recId!=null){"recId required for complete signature"}
        return BigInteger(byteArrayOf(recId!!)) + chainId.toBigInteger() * BigInteger.TWO + 35.toBigInteger()
    }

    fun extendSignature(publicKey: EcdsaPublicKey, messageHash: ByteArray) {
        // Find the right recId by trail and error.
        var recId = -1
        val pubKey = BigInteger(byteArrayOf(0) + publicKey.encoded)
        for (i in 0..3) {
            val k = recoverPublicKey(i, messageHash)
            if (k != null && k == pubKey) {
                recId = i
                break
            }
        }
        if (recId == -1) {
            throw RuntimeException(
                "Could not construct a recoverable key. This should never happen."
            )
        }
        this.recId = recId.toByte()
    }

    fun recoverPublicKey(messageHash: ByteArray): BigInteger? {
        return recId?.let { recoverPublicKey(it.toInt(), messageHash) }
    }

    private fun recoverPublicKey(recId: Int, messageHash: ByteArray): BigInteger? {
        require(messageHash.size == hashLength) { "incorrect hash length: expected 32, but was ${messageHash.size}" }
        // 1.0 For j from 0 to h (h == recId here and the loop is outside this function)
        // 1.1 Let x = r + jn
        val n = ecDomainParameters.n // Curve order.
        val i = (recId / 2).toBigInteger()
        val x = r + (i * n)
        // 1.2. Convert the integer x to an octet string X of length mlen using the conversion
        // routine specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        // 1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R
        // using the conversion routine specified in Section 2.3.4. If this conversion
        // routine outputs "invalid", then do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        if (x.compareTo(ecDomainParameters.curve.field.characteristic) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are
        // two possibilities. So it's encoded in the recId.
        val r = decompressKey(x, (recId and 1) == 1)
        // 1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
        // responsibility).
        if (!r.multiply(n).isInfinity) {
            return null
        }
        // 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        val e = BigInteger(byteArrayOf(0) + messageHash)
        // 1.6. For k from 1 to 2 do the following. (loop is outside this function via
        // iterating recId)
        // 1.6.1. Compute a candidate public key as:
        // Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        // Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n).
        // In the above equation ** is point multiplication and + is point addition (the EC group
        // operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For
        // example the additive inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and
        // -3 mod 11 = 8.
        val eInv = BigInteger.ZERO.subtract(e).mod(n)
        val rInv = this.r.modInverse(n)
        val srInv = rInv.multiply(s).mod(n)
        val eInvrInv = rInv.multiply(eInv).mod(n)
        val q = ECAlgorithms.sumOfTwoMultiplies(ecDomainParameters.g, eInvrInv, r, srInv)
        if (q.isInfinity) {
            return null
        }
        val qBytes = q.getEncoded(false)
        // We remove the prefix
        return BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.size))
    }

    // Decompress a compressed public key (x co-ord and low-bit of y-coord).
    private fun decompressKey(xBN: BigInteger?, yBit: Boolean): ECPoint {
        val x9 = X9IntegerConverter()
        val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(ecDomainParameters.curve))
        compEnc[0] = (if (yBit) 0x03 else 0x02).toByte()
        return ecDomainParameters.curve.decodePoint(compEnc)
    }


}