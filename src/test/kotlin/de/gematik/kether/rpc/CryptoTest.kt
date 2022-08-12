package de.gematik.kether.rpc

import keccak
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class CryptoTest {
    @Test
    fun eccakHash() {
        val functionSignature = "retrieve()"
        val retrieveSelector: ByteArray = byteArrayOf(46, 100, -50, -63)
        val selector = functionSignature.keccak().copyOfRange(0,4)
        assert(selector.contentEquals(retrieveSelector))
    }
}