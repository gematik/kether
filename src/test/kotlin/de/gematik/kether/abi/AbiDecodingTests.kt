package de.gematik.kether.abi

import de.gematik.kether.abi.types.*
import de.gematik.kether.eth.types.*
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.toHex
import de.gematik.kether.extensions.toRLP
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class AbiDecodingTests {
    @Test
    fun decodingFunction() {
        val d = "0x01020304" // 4 byte value
        val selector = DataDecoder(Data(d.hexToByteArray())).next<AbiSelector>()
        val r = Data4(byteArrayOf(1,2,3,4))
        assert(selector.toByteArray().contentEquals(r.toByteArray()))
    }

    @Test
    fun decodingUint256() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000001" // value (32 byte)
        val result = DataDecoder(Data(d.hexToByteArray())).next<AbiUint256>()
        val r = AbiUint256(1)
        assert(result.equals(r))
    }

    @Test
    fun decodingString() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset
                "0000000000000000000000000000000000000000000000000000000000000004" + // length
                "7465737400000000000000000000000000000000000000000000000000000000" // utf8
        val result = DataDecoder(Data(d.hexToByteArray())).next<AbiString>()
        val r = "test"
        assert(result == r)
    }

    @Test
    fun decodingArray() {
        val d = "0x0000000000000000000000000000000000000000000000000000000000000008" + // element 0
                "0000000000000000000000000000000000000000000000000000000000000009" // element 1
        val result = DataDecoder(Data(d.hexToByteArray())).next<Array<AbiUint256>>()
        val r = arrayOf(AbiUint256(8), AbiUint256(9))
        assert(result.contentEquals(r))
    }

    @Test
    fun encodingList() {
        val list = listOf(AbiUint256(8), AbiUint256(9))
        val result = DataEncoder().encode(list).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000020" + //offset 1. dynamic parameter
                "0000000000000000000000000000000000000000000000000000000000000002" + //length
                "0000000000000000000000000000000000000000000000000000000000000008" + // element 0
                "0000000000000000000000000000000000000000000000000000000000000009" // element 1
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }

    @Test
    fun encodingArrayOfStrings() {
        val array = arrayOf("A", "B")
        val result = DataEncoder().encode(array).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset of array
                "0000000000000000000000000000000000000000000000000000000000000002" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset element 0
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset element 1
                "0000000000000000000000000000000000000000000000000000000000000001" + // length element 0
                "4100000000000000000000000000000000000000000000000000000000000000" + // value element 0
                "0000000000000000000000000000000000000000000000000000000000000001" + // length element 1
                "4200000000000000000000000000000000000000000000000000000000000000" // value element 1
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }

    @Test
    fun encodingArrayOfArrayOfStrings() {
        val array = arrayOf(arrayOf("A", "B"), arrayOf("C", "D"))
        val result = DataEncoder().encode(array).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset of outer array
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
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }

    @Test
    fun encodingTuple() {
        data class Tuple(var a: AbiUint32, var b: AbiUint8) : AbiTuple {
            override fun encode() : DataEncoder {
                return DataEncoder()
                    .encode(a)
                    .encode(b)
            }
        }
        val tuple = Tuple(a= AbiUint32(1L), AbiUint8(2L))
        val result = DataEncoder().encode(tuple).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000001" + // value component a
                "0000000000000000000000000000000000000000000000000000000000000002" // value component b
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }

    @Test
    fun encodingDynamicTuple() {
        data class Tuple(var a: AbiString, var b: AbiString) : AbiTuple {
            override fun encode() : DataEncoder {
                return DataEncoder()
                    .encode(a)
                    .encode(b)
            }
        }
        val tuple = Tuple( "A", "B")
        val result = DataEncoder().encode(tuple).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset tuple
                "0000000000000000000000000000000000000000000000000000000000000040" + // offset component a
                "0000000000000000000000000000000000000000000000000000000000000080" + // offset component b
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component a
                "4100000000000000000000000000000000000000000000000000000000000000" + // value component a
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component b
                "4200000000000000000000000000000000000000000000000000000000000000" // value component b
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }

    @Test
    fun encodingTupleWithArray() {
        data class Tuple(var a: Array<AbiUint256>, var b: AbiString) : AbiTuple {
            override fun encode() : DataEncoder {
                return DataEncoder()
                    .encode(a)
                    .encode(b)
            }
        }
        val tuple = Tuple(arrayOf(AbiUint256(8), AbiUint256(9)), "B")
        val result = DataEncoder().encode(tuple).build()
        val r = "0x0000000000000000000000000000000000000000000000000000000000000020" + // offset tuple
                "0000000000000000000000000000000000000000000000000000000000000008" + // component a: element 0
                "0000000000000000000000000000000000000000000000000000000000000009" + // component a: element 1
                "0000000000000000000000000000000000000000000000000000000000000060" + // offset commpoent b
                "0000000000000000000000000000000000000000000000000000000000000001" + // length component b
                "4200000000000000000000000000000000000000000000000000000000000000" // value component b
        assertByteArray(result.toByteArray(), r.hexToByteArray())
    }


    @Test
    fun decodingError() {
        var message: String? = null
        val data = Data20("0x00")
        runCatching {
            DataDecoder(data).next<AbiUint256>()
            DataDecoder(data).next<AbiUint256>()
        }.onFailure {
            message = it.message
        }
        assert(message == "data decoding error: remaining data too short (pos: 0, limit: 20, type: Quantity)")
    }

    @Test
    fun encodeTransaction() {
        val transaction = Transaction(
            to = Address("0x1122334455667788990011223344556677889900"),
            data = DataEncoder().encode(Data4(byteArrayOf(1, 2, 3, 4))).build()
        )
        val byteArray = transaction.toRLP()
        assert(byteArray.size > 0)
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
