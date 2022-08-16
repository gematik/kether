package de.gematik.kether.types

import de.gematik.kether.rpc.BigIntegerSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigInteger

/**
 * Created by rk on 16.08.2022.
 * gematik.de
 */
@Serializable
data class RpcNotification<T>(val jsonrpc: String, val method: String, val params: Params<T>)

@Serializable
data class Params<T>(val subscription: String, val result: T)


