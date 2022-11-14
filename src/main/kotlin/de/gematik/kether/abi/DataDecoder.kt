package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
import de.gematik.kether.eth.types.Address
import de.gematik.kether.eth.types.Data
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.isSubclassOf

/**
 * Created by rk on 12.08.2022.
 * gematik.de
 */
class DataDecoder(data: Data) {
    private val byteArray = data.toByteArray()
    private var pos: Int = 0
    private var start: Int = 0
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> next(type: KClass<T>): T {
        return when {
            type == AbiSelector::class -> {
                checkSize(type, 4)
                val bytes = byteArray.copyOfRange(0, 4)
                pos += 4
                start = pos
                AbiSelector(bytes) as T
            }

            type == AbiUint::class -> {
                checkSize(type, 32)
                val bytes = byteArray.copyOfRange(pos, pos + 32)
                pos += 32
                AbiUint(bytes) as T
            }

            type == AbiString::class -> {
                checkSize(type, 32)
                val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt() + start
                pos += 32
                val length = BigInteger(byteArray.copyOfRange(offset, offset + 32)).toInt()
                String(byteArray.copyOfRange(offset + 32, offset + 32 + length)) as T
            }

            type == AbiBytes32::class -> {
                checkSize(type, 32)
                val bytes = byteArray.copyOfRange(pos, pos + 32)
                pos += 32
                AbiBytes32(bytes) as T
            }

            type == AbiAddress::class -> {
                checkSize(type, 32)
                val bytes = byteArray.copyOfRange(pos+12, pos + 32)
                pos += 32
                AbiAddress(bytes) as T
            }

            type.isSubclassOf(AbiTuple::class) -> {
                var dataDecoder = this
                if (isTypeDynamic(type)) {
                    checkSize(type, 32)
                    val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt() + start
                    pos += 32
                    dataDecoder = DataDecoder(Data(byteArray.copyOfRange(offset, byteArray.size)))
                }
                val constructor = type.constructors.find { it.parameters.size == 1 }
                constructor ?: error("constructor(DataDecoder) missing in ${type.simpleName}")
                constructor.call(dataDecoder)
            }

            else -> {
                error("data type not supported: ${type.qualifiedName}")
                //TODO: bytes<M>, bytes
            }
        }
    }

    fun next(type: KClass<*>, vararg dimensions: Int): List<*> {
        var dataDecoder = this
        var len = dimensions.last()
        if (dimensions.last() < 0 || isTypeDynamic(type)) {
            checkSize(type, 32)
            val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt() + start
            pos += 32
            len = BigInteger(byteArray.copyOfRange(offset, offset + 32)).toInt()
            dataDecoder = DataDecoder(Data(byteArray.copyOfRange(offset + 32, byteArray.size)))
        }
        return List(len) {
            if (dimensions.size > 1) {
                dataDecoder.next(type, *dimensions.copyOf(dimensions.size - 1))
            } else {
                dataDecoder.next(type)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyArray(array: Array<*>, type: KClass<*>): Array<*> {
        return if (array[0] is Array<*>) {
            if (type == String::class) {
                Array(array.size) { copyArray(array[it] as Array<*>, type) as Array<String> }
            } else {
                error("debug")
            }
        } else {
            Array(array.size) { array[it] as String }
        }
    }


    private fun <T : Any> checkSize(type: KClass<T>, length: Int) {
        check(byteArray.size - pos >= length) { "data decoding error: remaining data too short (pos: $pos, limit: ${byteArray.size}, type: ${type.simpleName})" }
    }
}

fun <T : Any> isTypeDynamic(type: KClass<T>): Boolean {
    return when {
        type == String::class -> true
        type.isSubclassOf(AbiTuple::class) -> (type.companionObject?.objectInstance as Dynamic).isDynamic()
        else -> false
    }
}