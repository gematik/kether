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

package de.gematik.kether.eth.types

import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.eth.serializer.DataSerializer
import de.gematik.kether.extensions.toHex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Created by rk on 03.08.2022.
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
        return other.toByteArray().contentEquals(value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return value.toHex()
    }

    fun toByteArray() = value

}

@Serializable(with = DataSerializer::class)
class Data4 : Data {
    constructor(hexString: String) : super(4, hexString)
    constructor(byteArray: ByteArray) : super(4, byteArray)
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
