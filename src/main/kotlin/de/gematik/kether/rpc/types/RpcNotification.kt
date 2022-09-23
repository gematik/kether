package de.gematik.kether.rpc.types

import kotlinx.serialization.Serializable

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@Serializable
data class RpcNotification<T>(val jsonrpc: String, val method: String, val params: Params<T>)

@Serializable
data class Params<T>(val subscription: String, val result: T)


