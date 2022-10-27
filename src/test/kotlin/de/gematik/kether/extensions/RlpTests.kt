package de.gematik.kether.extensions

import org.junit.jupiter.api.Test

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