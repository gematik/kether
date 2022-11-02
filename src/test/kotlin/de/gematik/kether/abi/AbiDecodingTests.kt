package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
import de.gematik.kether.eth.types.*
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.toHex
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class AbiDecodingTests {
    @Test
    fun decodingFunction() {
        val d = "0x01020304" // 4 byte value
        val selector = DataDecoder(Data(d.hexToByteArray())).next(AbiSelector::class)
        val r = Data4(byteArrayOf(1,2,3,4))
        assert(selector.toByteArray().contentEquals(r.toByteArray()))
    }

    @Test
    fun decodingUint256() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000001" // value (32 byte)
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiUint256::class)
        val r = AbiUint256(1)
        assert(result == r)
    }

    @Test
    fun decodingAddress() {
        val d = "0x0000000000000000000000000102030405060708090001020304050607080900" // value (20 byte stored in 32 byte field)
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiAddress::class)
        val r = AbiAddress("0x0102030405060708090001020304050607080900")
        assert(result == r)
    }

    @Test
    fun decodingString() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset
                "0000000000000000000000000000000000000000000000000000000000000004" + // length
                "7465737400000000000000000000000000000000000000000000000000000000" // utf8
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiString::class)
        val r = "test"
        assert(result == r)
    }

    @Test
    fun decodingStaticArray() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000008" + // element 0
                "0000000000000000000000000000000000000000000000000000000000000009" // element 1
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiUint256::class,  2)
        val r = listOf(AbiUint256(8), AbiUint256(9))
        assert(result == r)
    }

    @Test
    fun decodingDynamicArray() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + //offset dynamic parameter
                "0000000000000000000000000000000000000000000000000000000000000002" + //length
                "0000000000000000000000000000000000000000000000000000000000000008" + // element 0
                "0000000000000000000000000000000000000000000000000000000000000009" // element 1
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiUint256::class, -1)
        val r = listOf(AbiUint256(8), AbiUint256(9))
        assert(result == r)
    }

    @Test
    fun decodingArrayOfStrings() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset of array
                "0000000000000000000000000000000000000000000000000000000000000002" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset element 0
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset element 1
                "0000000000000000000000000000000000000000000000000000000000000001" + // length element 0
                "4100000000000000000000000000000000000000000000000000000000000000" + // value element 0
                "0000000000000000000000000000000000000000000000000000000000000001" + // length element 1
                "4200000000000000000000000000000000000000000000000000000000000000" // value element 1
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiString::class, 2)
        val r = listOf("A", "B")
        assert(result == r)
    }

    @Test
    fun decodingArrayOfArrayOfStrings() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset of outer array
                "0000000000000000000000000000000000000000000000000000000000000002" + // length of outer array
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset of arrayOf("A","B")
                "0000000000000000000000000000000000000000000000000000000000000120" + // offset of arrayOf("C","D")
                "0000000000000000000000000000000000000000000000000000000000000002" + // length of arrayOf("A","B")
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset "A"
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset "B"
                "0000000000000000000000000000000000000000000000000000000000000001" + // length "A"
                "4100000000000000000000000000000000000000000000000000000000000000" + // value "A"
                "0000000000000000000000000000000000000000000000000000000000000001" + // length "B"
                "4200000000000000000000000000000000000000000000000000000000000000" + // value "B"
                "0000000000000000000000000000000000000000000000000000000000000002" + // length of arrayOf("C","D")
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset "C"
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset "D"
                "0000000000000000000000000000000000000000000000000000000000000001" + // length "C"
                "4300000000000000000000000000000000000000000000000000000000000000" + // value "C"
                "0000000000000000000000000000000000000000000000000000000000000001" + // length "D"
                "4400000000000000000000000000000000000000000000000000000000000000"   // value "D"
        val r = listOf(listOf("A", "B"), listOf("C", "D"))
        val result = DataDecoder(Data(d.hexToByteArray())).next(AbiString::class, -1, -1)
        assert(result == r)
    }

    data class StaticTuple(var a: AbiUint32, var b: AbiUint8) : AbiTuple {
        constructor(dataDecoder: DataDecoder) : this(a = dataDecoder.next(AbiUint32::class), b=dataDecoder.next(AbiUint8::class))
        companion object : Dynamic {
            override fun isDynamic() = isTypeDynamic(AbiUint32::class) || isTypeDynamic(AbiUint8::class)
        }
        override fun encode(): DataEncoder {
            error("not implemented")
        }
    }

    @Test
    fun decodingStaticTuple() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000001" + // value component a
                "0000000000000000000000000000000000000000000000000000000000000002" // value component b
        val r = StaticTuple(a= AbiUint32(1L), AbiUint8(2L))
        val result = DataDecoder(Data(d.hexToByteArray())).next(StaticTuple::class)
        assert(result.a == r.a && result.b == r.b)
    }

    data class DynamicTuple(var a: AbiString, var b: AbiString) : AbiTuple {
        constructor(dataDecoder: DataDecoder) : this(a = dataDecoder.next(AbiString::class), b=dataDecoder.next(AbiString::class))
        companion object : Dynamic {
            override fun isDynamic() = isTypeDynamic(AbiString::class) || isTypeDynamic(AbiString::class)
        }
        override fun encode(): DataEncoder {
            error("not implemented")
        }
    }

    @Test
    fun decodingDynamicTuple() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset tuple
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset component a
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset component b
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component a
                "4100000000000000000000000000000000000000000000000000000000000000" + // value component a
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component b
                "4200000000000000000000000000000000000000000000000000000000000000" // value component b
        val r = DynamicTuple(a= "A", "B")
        val result = DataDecoder(Data(d.hexToByteArray())).next(DynamicTuple::class)
        assert(result.a == r.a && result.b == r.b)
    }

    data class TupleWithArray(val a: List<AbiUint256>, val b: AbiString) : AbiTuple {
        @Suppress("UNCHECKED_CAST")
        constructor(dataDecoder: DataDecoder) : this(a = dataDecoder.next(AbiUint256::class, 2) as List<AbiUint256>, b=dataDecoder.next(AbiString::class))
        companion object : Dynamic {
            override fun isDynamic() = isTypeDynamic(AbiUint256::class) || isTypeDynamic(AbiString::class)
        }
        override fun encode(): DataEncoder {
            error("not implemented")
        }
    }
    @Test
    fun decodingTupleWithArray() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset tuple
                "0000000000000000000000000000000000000000000000000000000000000008" + // component a: element 0
                "0000000000000000000000000000000000000000000000000000000000000009" + // component a: element 1
                "0000000000000000000000000000000000000000000000000000000000000060" + // offset commpoent b
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component b
                "4200000000000000000000000000000000000000000000000000000000000000" // value component b
        val r = TupleWithArray(listOf(AbiUint256(8), AbiUint256(9)), "B")
        val result = DataDecoder(Data(d.hexToByteArray())).next(TupleWithArray::class)
        assert(result.a == r.a && result.b == r.b)
    }


    @Test
    fun decodingError() {
        var message: String? = null
        val data = Data20("0x00")
        runCatching {
            DataDecoder(data).next(AbiUint256::class)
            DataDecoder(data).next(AbiUint256::class)
        }.onFailure {
            message = it.message
        }
        assert(message == "data decoding error: remaining data too short (pos: 0, limit: 20, type: Quantity)")
    }

    private fun assertByteArray(result: ByteArray, expectedResult: ByteArray){
        assert(result.contentEquals(expectedResult)) {
            val left = result.toHex().drop(2).chunked(64)
            val right = expectedResult.toHex().drop(2).chunked(64)
            val stringBuilder = StringBuilder("byteArray's do not match: \n")
            for(i in 0 .. Math.min(left.size, right.size)-1){
                stringBuilder.append("${left[i]} - ${right[i]}")
                stringBuilder.append(if(left[i] != right[i]) " !\n" else "\n")
            }
            stringBuilder.toString()
        }
    }
}
