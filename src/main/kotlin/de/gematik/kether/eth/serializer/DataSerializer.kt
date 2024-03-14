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

import de.gematik.kether.eth.types.Data
import de.gematik.kether.eth.types.Data20
import de.gematik.kether.eth.types.Data32
import de.gematik.kether.extensions.toHex
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
 */

@ExperimentalSerializationApi
class DataSerializer : KSerializer<Data> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DataSerializer", PrimitiveKind.STRING)

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Data) {
        encoder.encodeString(value.toByteArray().toHex())
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