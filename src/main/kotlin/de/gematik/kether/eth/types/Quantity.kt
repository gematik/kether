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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = QuantitySerializer::class)
class Quantity {
    private val value: BigInteger?
    private val tag: Tag?

    constructor(bytes: ByteArray) {
        this.value = BigInteger(bytes)
        tag = null
    }

    constructor(value: BigInteger) {
        this.value = value
        tag = null
    }

    constructor(str: String, radix: Int = 10) {
        this.value = BigInteger(str, radix)
        tag = null
    }

    constructor(value: Long) {
        this.value = value.toBigInteger()
        tag = null
    }

    constructor(tag: Tag) {
        value = null
        this.tag = tag
    }

    fun toBigInteger(): BigInteger {
        check(value != null)
        return value
    }

    fun toTag(): Tag {
        check(tag != null)
        return tag
    }

    fun isTag() = tag!=null

    operator fun plus(other: Quantity): Quantity {
        check(value != null)
        return Quantity(value + other.toBigInteger())
    }

    operator fun minus(other: Quantity): Quantity {
        check(value != null)
        return Quantity(value + other.toBigInteger())
    }

    operator fun times(other: Quantity): Quantity {
        check(value != null)
        return Quantity(value * other.toBigInteger())
    }

    operator fun div(other: Quantity): Quantity {
        check(value != null)
        return Quantity(value / other.toBigInteger())
    }

    operator fun rem(other: Quantity): Quantity {
        check(value != null)
        return Quantity(value % other.toBigInteger())
    }

    operator fun inc(): Quantity {
        check(value != null)
        return Quantity(value.inc())
    }

    operator fun dec(): Quantity {
        check(value != null)
        return Quantity(value.dec())
    }

    operator fun compareTo(other: Quantity): Int {
        check(value != null)
        return value.compareTo(other.toBigInteger())
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!(other is Quantity)) return false
        return if (isTag()){
            other.toTag() == toTag()
        }else{
            other.toBigInteger() == toBigInteger()
        }
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (tag?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return if(isTag()){
            tag!!.name
        }else{
            value.toString()
        }
    }
}



enum class Tag {
    latest,
    earlist,
    pending
}

