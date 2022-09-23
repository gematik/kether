package de.gematik.kether.eth.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
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