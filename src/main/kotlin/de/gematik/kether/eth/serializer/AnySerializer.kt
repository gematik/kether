/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

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