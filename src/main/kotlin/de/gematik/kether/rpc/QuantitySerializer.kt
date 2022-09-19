package de.gematik.kether.rpc

import de.gematik.kether.types.Block
import de.gematik.kether.types.Quantity
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