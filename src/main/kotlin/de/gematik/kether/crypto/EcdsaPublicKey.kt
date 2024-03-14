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

package de.gematik.kether.crypto

import de.gematik.kether.eth.types.Address
import de.gematik.kether.extensions.keccak
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import java.math.BigInteger
import java.security.PublicKey

class EcdsaPublicKey(
    private val encoded: ByteArray,
    val curve: EllipticCurve
) : PublicKey {

    internal val ecDomainParameters =
        SECNamedCurves.getByName(curve.name).let { ECDomainParameters(it.curve, it.g, it.n, it.h, it.seed) }

    init {
        check(encoded.size == ecDomainParameters.curve.fieldSize / 4) { "wrong encoded size" }
    }

    override fun getAlgorithm(): String {
        return SignatureAlgorithm.ECDSA.name
    }

    override fun getFormat(): String? {
        return null
    }

    override fun getEncoded(): ByteArray {
        return encoded
    }

    fun toAccountAddress(): Address {
        return Address(encoded.keccak().copyOfRange(12, 32))
    }

    fun verify(messageHash: ByteArray, ecdsaSignature: EcdsaSignature): Boolean {
        val signer = ECDSASigner(curve.dsakCalculator());
        val ecPublicKeyParameters =
            ECPublicKeyParameters(ecDomainParameters.curve.decodePoint(byteArrayOf(4) + encoded), ecDomainParameters)
        signer.init(false, ecPublicKeyParameters)
        return kotlin.runCatching {
            signer.verifySignature(messageHash, BigInteger(1, ecdsaSignature.r), BigInteger(1, ecdsaSignature.s))
        }.getOrElse { false }
    }

    override fun equals(other: Any?) =
        other is EcdsaPublicKey && other.encoded.contentEquals(this.encoded) && other.curve == this.curve

}
