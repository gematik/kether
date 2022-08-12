package de.gematik.kether.rpc

import de.gematik.kether.extensions.toHex
import de.gematik.kether.types.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

@ExperimentalSerializationApi
class AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor =
        ContextualSerializer(Any::class, null, emptyArray()).descriptor

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is List<*> -> encoder.encodeSerializableValue(ListSerializer(AnySerializer()), value as List<Any>)
            else -> encoder.encodeSerializableValue(value::class.serializer() as KSerializer<Any>, value)
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        error("unsupported")
    }
}

@ExperimentalSerializationApi
class BigIntegerSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigIntegerSerializer", PrimitiveKind.STRING)

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: BigInteger) {
        encoder.encodeString("0x${value.toString(16)}")

    }

    override fun deserialize(decoder: Decoder): BigInteger {
        return decoder.decodeString().drop(2).toBigInteger(16)
    }
}

@ExperimentalSerializationApi
class DataSerializer : KSerializer<Data> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DataSerializer", PrimitiveKind.STRING)

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Data) {
        encoder.encodeString(value.value.toHex())
    }

    override fun deserialize(decoder: Decoder): Data {
        val hexString = decoder.decodeString()
        return when (hexString.length) {
            42 -> Data20(hexString)
            66 -> Data32(hexString)
            else -> error("unsupported data length")
        }
    }
}

@ExperimentalSerializationApi
class QuantitySerializer : KSerializer<Quantity> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("QuantitySerializer", PrimitiveKind.STRING)

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Quantity) {
        val string = if (value.value < 0) Block.values().find { it.value == value.value }?.name ?: error(
            "invalid tag"
        ) else "0x${value.value.toString(16)}"
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Quantity {
        val string = decoder.decodeString()
        return Quantity(if(string.lowercase().startsWith("0x")) {
            string.drop(2).toLong(16)
        } else {
            Block.valueOf(string).value
        })
    }

}