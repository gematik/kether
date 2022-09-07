package de.gematik.kether.types

import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.rpc.DataSerializer
import de.gematik.kether.rpc.QuantitySerializer
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */
@Serializable(with = DataSerializer::class)
open class Data {
     var value: ByteArray
        private set

    constructor(length: Int? = null, hexString: String? = null) {
        if(hexString==null){
            value = ByteArray(0)
        }else{
            val len = if(length==null) (hexString.length+1)/2-1 else length
            value = hexString.hexToByteArray(len)
        }
    }

    constructor(byteArray: ByteArray) {
        value = byteArray
    }
}

@Serializable(with = DataSerializer::class)
class Data20 : Data {
    constructor(hexString: String) : super(20, hexString)
    constructor(byteArray: ByteArray) : super(byteArray.copyOf(20))
}

@Serializable(with = DataSerializer::class)
class Data32 : Data {
    constructor(hexString: String) : super(32, hexString)
    constructor(byteArray: ByteArray) : super(byteArray.copyOf(32))
}
