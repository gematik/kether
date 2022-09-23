package de.gematik.kether.eth.serializer

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

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
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        if(element.jsonPrimitive.isString){
            return element.jsonPrimitive.content
        }else{
            return element.jsonPrimitive.boolean
        }
    }
}