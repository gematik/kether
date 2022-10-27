package de.gematik.kether.extensions

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class CryptoTests {
    @Test
    fun eccakHash() {
        val functionSignature = "retrieve()"
        val retrieveSelector: ByteArray = byteArrayOf(46, 100, -50, -63)
        val selector = functionSignature.keccak().copyOfRange(0, 4)
        assert(selector.contentEquals(retrieveSelector))
    }
}