package de.gematik.kether.eth.types

import de.gematik.kether.eth.serializer.AnySerializer
import kotlinx.serialization.Serializable

/**
 * Created by rk on 03.08.2022.
 * gematik.de
 */

typealias Address = Data20

@Serializable(with = AnySerializer::class)
class AnyResult : Any()
