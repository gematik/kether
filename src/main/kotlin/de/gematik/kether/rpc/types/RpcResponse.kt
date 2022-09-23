package de.gematik.kether.rpc.types

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */

@Serializable
@ExperimentalSerializationApi
class RpcResponse<T> private constructor(val id: Int) {

    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val jsonrpc = "2.0"

    @Serializable
    data class Error(
        val code: Int,
        val message: String,
        val data: String? = null
    )

    constructor(id: Int, result: T) : this(id) {
        this.result = result
    }

    constructor(id: Int, error: Error) : this(id) {
        this.error = error
    }

    var error: Error? = null
        private set
    var result: T? = null
        private set
}
