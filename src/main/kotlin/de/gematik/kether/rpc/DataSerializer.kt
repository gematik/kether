package de.gematik.kether.rpc

import de.gematik.kether.extensions.toHex
import de.gematik.kether.types.Data
import de.gematik.kether.types.Data20
import de.gematik.kether.types.Data32
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
            else -> Data(hexString = hexString)
        }
    }
}