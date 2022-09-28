package de.gematik.kether.rpc.types

/**
 * Created by rk on 28.09.2022.
 * gematik.de
 */
class RpcException(val code: Int, message: String) : Exception(message)