package de.gematik.kether.types

import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.rpc.DataSerializer
import de.gematik.kether.rpc.QuantitySerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = DataSerializer::class)
open class Data {
    private var value: ByteArray

    protected constructor (length: Int, hexString: String) {
        require((hexString.length + 1) / 2 - 1 <= length) { "hexString too long" }
        value = hexString.hexToByteArray(length)
    }

    protected constructor (length: Int, byteArray: ByteArray) {
        require(byteArray.size <= length) { "byteArray too long" }
        value = byteArray.copyOf(length)
    }

    constructor(hexString: String) {
        value = hexString.hexToByteArray()
    }

    constructor(byteArray: ByteArray) {
        value = byteArray
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!(other is Data)) return false
        return other.toByteArray().equals(value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    fun toByteArray() = value
}

@Serializable(with = DataSerializer::class)
class Data20 : Data {
    constructor(hexString: String) : super(20, hexString)
    constructor(byteArray: ByteArray) : super(20, byteArray)
}

@Serializable(with = DataSerializer::class)
class Data32 : Data {
    constructor(hexString: String) : super(32, hexString)
    constructor(byteArray: ByteArray) : super(32, byteArray)
}
