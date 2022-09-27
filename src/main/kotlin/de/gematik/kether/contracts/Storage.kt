package de.gematik.kether.contracts

import de.gematik.kether.abi.DataDecoder
import de.gematik.kether.abi.DataEncoder
import de.gematik.kether.abi.types.*
import de.gematik.kether.contracts.Contract
import de.gematik.kether.contracts.Event
import de.gematik.kether.eth.Eth
import de.gematik.kether.eth.types.*
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.keccak
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class Storage(
    eth: Eth,
    baseTransaction: Transaction = Transaction()
) : Contract(eth, baseTransaction) {
    companion object {
        // deployment
        val byteCode =
            "0x608060405234801561001057600080fd5b50610201806100206000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80632e64cec114610046578063371303c0146100645780636057361d1461006e575b600080fd5b61004e61008a565b60405161005b9190610105565b60405180910390f35b61006c610093565b005b610088600480360381019061008391906100c9565b6100aa565b005b60008054905090565b60016000546100a29190610120565b600081905550565b8060008190555050565b6000813590506100c3816101b4565b92915050565b6000602082840312156100df576100de6101af565b5b60006100ed848285016100b4565b91505092915050565b6100ff81610176565b82525050565b600060208201905061011a60008301846100f6565b92915050565b600061012b82610176565b915061013683610176565b9250827fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0382111561016b5761016a610180565b5b828201905092915050565b6000819050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b600080fd5b6101bd81610176565b81146101c857600080fd5b5056fea26469706673582212200a89d87559d4d26643dc5630a93b01a85c2d71e0ce5093279eb69a15535f8c2164736f6c63430008070033".hexToByteArray()

        fun deploy(eth: Eth, from: Address) = deploy(eth, from, Data(byteCode))

        // 4 byte selectors (functions) and topics (events)
        val functionInc = "inc()".keccak().copyOfRange(0, 4)
        val functionRetrieve = "retrieve()".keccak().copyOfRange(0, 4)
        val functionStore = "store(uint256)".keccak().copyOfRange(0, 4)
    }

    // events
    override val listOfEventDecoders: List<(Log) -> Event?> = listOf()

    // functions
    suspend fun inc(): TransactionReceipt {
        val params = DataEncoder()
            .encode(Data4(functionInc)).build()
        return transact(params)
    }

    data class ResultsRetrieve(
        val value: AbiUint256
    )

    fun retrieve(): ResultsRetrieve {
        val params = DataEncoder()
            .encode(Data4(functionRetrieve)).build()
        val decoder = DataDecoder(call(params))
        return ResultsRetrieve(
            decoder
                .next()
        )
    }

    suspend fun store(num: AbiUint256): TransactionReceipt {
        val params = DataEncoder()
            .encode(Data4(functionStore))
            .encode(num).build()
        return transact(params)
    }
}
