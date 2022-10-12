package de.gematik.kether.abi.types

import de.gematik.kether.abi.isDynamic
import kotlin.reflect.KClass

/**
 * Created by rk on 11.10.2022.
 * gematik.de
 */
data class AbiArray<T : Any>(val type: KClass<T>? = null, val array: AbiArray<T>? = null, val size: Int = -1) {
    fun isDynamic() : Boolean {
        return size < 0 || elementsAreDynamic()
    }

    fun arrayType() : KClass<T>{
        return if(type!=null) type else array!!.arrayType()
    }

    private fun elementsAreDynamic() : Boolean{
        return if(array!=null) array.isDynamic() else isDynamic(type!!)
    }
}