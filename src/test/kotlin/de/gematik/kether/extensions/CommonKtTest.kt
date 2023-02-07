package de.gematik.kether.extensions

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.math.BigInteger
import kotlin.random.Random

class CommonKtTest {

    @Test
    fun toByteArray() {
        for (i in 1..1000) {
            val a = BigInteger(Random.Default.nextBytes(32))
            val b = a.toByteArray(32)
        }
    }
}