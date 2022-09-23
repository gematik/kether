package de.gematik.kether.rpc

import de.gematik.kether.types.Quantity
import de.gematik.kether.types.Tag
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

@ExperimentalSerializationApi
class QuantitySerializer : KSerializer<Quantity> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("QuantitySerializer", PrimitiveKind.STRING)

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Quantity) {
        val string = if (value.isTag()) value.toTag().name  else "0x${value.toBigInteger().toString(16)}"
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Quantity {
        val string = decoder.decodeString()
        return if(string.lowercase().startsWith("0x")) {
            Quantity(string.drop(2).toBigInteger(16))
        } else {
            Quantity(Tag.valueOf(string))
        }
    }
}