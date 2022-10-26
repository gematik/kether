package de.gematik.kether.extensions

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
class RlpTests {
    @Test
    fun decodeRlpEmptyToBigInteger() {
        val decodedValue = RlpEmpty.toBigIntegerFromRLP()
        assert(decodedValue == null)
    }
}