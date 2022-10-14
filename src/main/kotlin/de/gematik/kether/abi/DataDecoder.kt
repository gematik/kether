package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
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
    val byteArray = data.toByteArray()
    var pos: Int = 0

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> next(type: KClass<T>): T {
        return when {
            type == AbiSelector::class -> {
                checkSize(type, 4)
                val bytes = byteArray.copyOfRange(0, 4)
                pos += 4
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
                val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt()
                pos += 32
                val length = BigInteger(byteArray.copyOfRange(offset, offset + 32)).toInt()
                String(byteArray.copyOfRange(offset + 32, offset + 32 + length)) as T
            }

            type == AbiBytes32::class -> {
                checkSize<T>(type, 32)
                val bytes = byteArray.copyOfRange(pos, pos + 32)
                pos += 32
                AbiBytes32(bytes) as T
            }

            type.isSubclassOf(AbiTuple::class) -> {
                var dataDecoder = this
                if (isTypeDynamic(type)) {
                    checkSize<T>(type, 32)
                    val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt()
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

    fun <T : Any> next(array: AbiArray<T>): Array<*> {
        return nextArray(array)
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyArray(array: Array<*>, type: KClass<*>): Array<*> {
        return if (array[0] is Array<*>) {
            if(type == String::class){
                Array(array.size) { copyArray(array[it] as Array<*>, type) as Array<String> }
            }else{
                error("debug")
            }
        } else {
            Array(array.size) { array[it] as String }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> nextArray(array: AbiArray<T>): Array<*> {
        var dataDecoder = this
        var len = array.size
        if (len < 0 || array.isDynamic()) {
            checkSize(array.type ?: Array::class, 32)
            val offset = BigInteger(byteArray.copyOfRange(pos, pos + 32)).toInt()
            pos += 32
            len = BigInteger(byteArray.copyOfRange(offset, offset + 32)).toInt()
            dataDecoder = DataDecoder(Data(byteArray.copyOfRange(offset + 32, byteArray.size)))
        }
        return if (array.array != null) {
            when{
                array.arrayType() == AbiUint::class -> Array(len) { dataDecoder.nextArray(array.array) as Array<AbiUint> }
                array.arrayType() == AbiString::class -> Array(len) { dataDecoder.nextArray(array.array) as Array<AbiString> }
                else -> Array(len) { dataDecoder.nextArray(array.array) }
            }
        } else if (array.type != null) {
            when {
                array.type == AbiUint::class -> Array(len) { dataDecoder.next(AbiUint::class) }
                array.type == AbiString::class -> Array(len) { dataDecoder.next(AbiString::class) }
                else -> Array<Any>(len) { dataDecoder.next(array.type) }
            }
        } else {
            error("internal error: invalid AbiArray")
        }
    }

    fun <T : Any> checkSize(type: KClass<T>, length: Int) {
        check(byteArray.size - pos >= length) { "data decoding error: remaining data too short (pos: $pos, limit: ${byteArray.size}, type: ${type.simpleName})" }
    }
}

fun <T : Any> isTypeDynamic(type: KClass<T>): Boolean {
    return when {
        type == String::class -> true
        type.isSubclassOf(AbiTuple::class) == true -> (type.companionObject?.objectInstance as Dynamic).isDynamic()
        else -> false
    }
}