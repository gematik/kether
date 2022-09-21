package codegen

import kotlinx.serialization.json.*
import java.io.File

/**
 * Created by rk on 20.09.2022.
 * gematik.de
 */
@OptIn(ExperimentalStdlibApi::class)
class CodeGenerator(private val contractName: String, private val abi: JsonArray, private val byteCode: String? = null) {
    constructor(abiFile: File, byteCodeFile: File?) : this(abiFile.name.substring(0, abiFile.name.indexOfLast{it=='.'} ),
        abi = Json.parseToJsonElement(abiFile.readText(Charsets.UTF_8)).jsonArray,
        byteCode = byteCodeFile?.let {
            Json.parseToJsonElement(it.readText(Charsets.UTF_8)).jsonObject.get("object")?.jsonPrimitive?.content
        })

    private val template = """import de.gematik.kether.abi.*
        import de.gematik.kether.contracts.Contract
        import de.gematik.kether.extensions.hexToByteArray
        import de.gematik.kether.rpc.Eth
        import de.gematik.kether.types.*
        import kotlinx.serialization.ExperimentalSerializationApi

        @OptIn(ExperimentalSerializationApi::class)
        class $contractName(
            eth: Eth,
            baseTransaction: Transaction = Transaction()
        ) : Contract(eth, baseTransaction) {

            companion object {

                // deployment
                ${generateDeployment()}

                // selectors
                ${generateSelectors()}
            }

            // events
            ${generateEvents()}
            
            // functions
            ${generateFunctions()}

        }
    """.replace(Regex("""^\s+""", RegexOption.MULTILINE), "")

    private enum class StateMutability {
        view,
        pure,
        nonpayable,
        payable
    }

    fun generateCode(): String {
        return template
    }

    private fun generateDeployment(): String {
        byteCode?:return "// deployment data (bytecode) not available"
        val stringBuilder = StringBuilder()
        val stringBuilderParams = StringBuilder()
        abi.filter { it.jsonObject["type"]?.jsonPrimitive?.content == "constructor"
        }.filter {
            stringBuilder.append("val byteCode = \"0x$byteCode\".hexToByteArray()\n")
            stringBuilder.append("fun deploy(eth:Eth, from: Address, ")
            stringBuilderParams.append("val params = Data(\nbyteCode + DataEncoder()\n")
            it.jsonObject["inputs"]?.jsonArray?.forEach {
                val name = it.jsonObject.get("name")?.jsonPrimitive?.content
                val type = it.jsonObject.get("type")?.jsonPrimitive?.content
                if (name != null || type != null) {
                    stringBuilder.append("$name: Abi${type?.replaceFirstChar(Char::titlecase)},")
                    stringBuilderParams.append(".encode($name)")
                }
            }
            stringBuilderParams.append(".build().value\n)\n")
            if (stringBuilder.last() == ',') stringBuilder.deleteAt(stringBuilder.length - 1)
            stringBuilder.append("): TransactionReceipt {\n")
            stringBuilder.append(stringBuilderParams)
            stringBuilder.append("return deploy(eth, from, params)\n}\n")
            return stringBuilder.toString()
        }
        return stringBuilder.toString()
    }

    private fun generateSelectors(): String {
        val stringBuilder = StringBuilder()
        abi.filter {
            val type = it.jsonObject["type"]?.jsonPrimitive?.content
            type == "event" || type == "function"
        }.forEach {
            val type = it.jsonObject["type"]?.jsonPrimitive?.content
            val name = it.jsonObject["name"]?.jsonPrimitive?.content
            if (type != null && name != null) {
                stringBuilder.append("val $type${name.replaceFirstChar(Char::titlecase)} = \"$name(")
                it.jsonObject["inputs"]?.jsonArray?.forEach {
                    it.jsonObject.get("type")?.jsonPrimitive?.content?.let {
                        stringBuilder.append(it + ",")
                    }
                }
                if (stringBuilder.last() == ',') {
                    stringBuilder.deleteAt(stringBuilder.length - 1)
                }
                stringBuilder.append(")\".keccak().copyOfRange(0, 4)\n")
            }
        }
        return stringBuilder.toString()
    }

    private fun generateEvents(): String {
        val stringBuilder = StringBuilder()
        val stringBuilderEventDecoders = StringBuilder()
        abi.filter {
            it.jsonObject["type"]?.jsonPrimitive?.content == "event"
        }.forEach {
            val eventName = it.jsonObject["name"]?.jsonPrimitive?.content
            if (eventName != null) {
                val eventClassName = "Event${eventName.replaceFirstChar(Char::titlecase)}"
                stringBuilderEventDecoders.append("$eventClassName::decoder,")
                stringBuilder.append("data class $eventClassName(")
                stringBuilder.append("val eventSelector: AbiBytes32,")
                val stringBuilderTopics = StringBuilder()
                stringBuilderTopics.append("eventSelector,")
                val stringBuilderValues = StringBuilder()
                val stringBuilderArguments = StringBuilder()
                stringBuilderArguments.append("eventSelector=log.topics!!.get(0).value,")
                var index = 1
                it.jsonObject["inputs"]?.jsonArray?.forEach {
                    val type = it.jsonObject.get("type")?.jsonPrimitive?.content
                    val name = it.jsonObject.get("name")?.jsonPrimitive?.content
                    if (type != null && name != null) {
                        val isIndexed = it.jsonObject.get("indexed")?.jsonPrimitive?.boolean
                        if (isIndexed == true) {
                            stringBuilder.append("val $name: AbiBytes32,")
                            stringBuilderTopics.append("$name,")
                            stringBuilderArguments.append("$name = log.topics.get(${index++}).value,")
                        } else {
                            val abiTypeName = "Abi${type.replaceFirstChar(Char::titlecase)}"
                            stringBuilder.append("val $name: $abiTypeName,")
                            stringBuilderValues.append("val $name = decoder.next<$abiTypeName>()\n")
                            stringBuilderArguments.append("$name = $name,")
                        }
                    }
                }
                if (stringBuilder.last() == ',') stringBuilder.deleteAt(stringBuilder.length - 1)
                if (stringBuilderTopics.last() == ',') stringBuilderTopics.deleteAt(stringBuilderTopics.length - 1)
                stringBuilder.append(") : Event(topics = listOf(${stringBuilderTopics})) {\n")
                stringBuilder.append("companion object {\n")
                stringBuilder.append("fun decoder(log: Log): Event? {\n")
                stringBuilder.append("return checkEvent(log, event$eventName)?.let {\n")
                stringBuilder.append("val decoder = DataDecoder(log.data!!)\n")
                stringBuilder.append(stringBuilderValues)
                stringBuilder.append("$eventClassName(\n")
                if (stringBuilderArguments.last() == ',') stringBuilderArguments.deleteAt(stringBuilderArguments.length - 1)
                stringBuilder.append(stringBuilderArguments)
                stringBuilder.append(")\n}\n}\n}\n}\n")
            }
        }
        if (!stringBuilderEventDecoders.isEmpty() && stringBuilderEventDecoders.last() == ',') stringBuilderEventDecoders.deleteAt(stringBuilderEventDecoders.length - 1)
        stringBuilder.append("override val listOfEventDecoders: List<(Log) -> Event?> = listOf($stringBuilderEventDecoders)")
        return stringBuilder.toString()
    }

    private fun generateFunctions(): String {
        val stringBuilder = StringBuilder()
        abi.filter {
            it.jsonObject["type"]?.jsonPrimitive?.content == "function"
        }.forEach {
            val functionName = it.jsonObject["name"]?.jsonPrimitive?.content
            if (functionName != null) {
                val stateMutability =
                    it.jsonObject["stateMutability"]?.jsonPrimitive?.content?.let { StateMutability.valueOf(it) }
                val outputs = it.jsonObject["outputs"]?.jsonArray
                var resultClassName: String? = null
                if (outputs?.isEmpty() != true && stateMutability != StateMutability.payable && stateMutability != StateMutability.nonpayable) {
                    resultClassName = "Results${functionName.replaceFirstChar(Char::titlecase)}"
                    stringBuilder.append("data class $resultClassName(\n")
                    outputs?.forEach {
                        val name = it.jsonObject.get("name")?.jsonPrimitive?.content?.let {
                            if (it.isEmpty()) "value" else it
                        } ?: ""
                        val type = it.jsonObject.get("type")?.jsonPrimitive?.content
                        if (type != null) stringBuilder.append("val $name: Abi${type.replaceFirstChar(Char::titlecase)}\n")
                    }
                    stringBuilder.append(")\n")
                }
                val stringBuilderParams = StringBuilder()
                val inputs = it.jsonObject["inputs"]?.jsonArray
                if (stateMutability == StateMutability.payable || stateMutability == StateMutability.nonpayable) {
                    stringBuilder.append("suspend fun $functionName(")
                    stringBuilderParams.append(
                        "val params = DataEncoder()\n.encodeSelector(function${
                            functionName.replaceFirstChar(
                                Char::titlecase
                            )
                        })"
                    )
                    inputs?.forEach {
                        val name = it.jsonObject.get("name")?.jsonPrimitive?.content
                        val type = it.jsonObject.get("type")?.jsonPrimitive?.content
                        if (name != null || type == null) {
                            stringBuilder.append("$name: Abi${type?.replaceFirstChar(Char::titlecase)},")
                            stringBuilderParams.append("\n.encode($name)")
                        }
                    }
                    stringBuilderParams.append(".build()\n")
                    if (stringBuilder.last() == ',') stringBuilder.deleteAt(stringBuilder.length - 1)
                    stringBuilder.append("): TransactionReceipt {\n")
                    stringBuilder.append(stringBuilderParams.toString())
                    stringBuilder.append("return transact(params)\n}\n")
                } else {
                    stringBuilder.append("fun $functionName(")
                    stringBuilderParams.append(
                        "val params = DataEncoder()\n.encodeSelector(function${
                            functionName.replaceFirstChar(
                                Char::titlecase
                            )
                        })"
                    )
                    inputs?.forEach {
                        val name = it.jsonObject.get("name")?.jsonPrimitive?.content
                        val type = it.jsonObject.get("type")?.jsonPrimitive?.content
                        if (name != null || type == null) {
                            stringBuilder.append("$name: Abi${type?.replaceFirstChar(Char::titlecase)},")
                            stringBuilderParams.append("\n.encode($name)")
                        }
                    }
                    stringBuilderParams.append(".build()\n")
                    if (stringBuilder.last() == ',') stringBuilder.deleteAt(stringBuilder.length - 1)
                    stringBuilder.append(if (resultClassName != null) "): $resultClassName {\n" else "){\n")
                    stringBuilder.append(stringBuilderParams.toString())
                    stringBuilder.append("val decoder = DataDecoder(call(params))\n")
                    stringBuilder.append("return $resultClassName(\ndecoder")
                    outputs?.filter { it.jsonObject["type"] != null }?.forEach {
                        stringBuilder.append("\n.next()")
                    }
                    stringBuilder.append(")\n}\n")
                }
            }
        }
        return stringBuilder.toString()
    }
}