package de.gematik.kether

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite

/**
 * Created by rk on 04.08.2022.
 * gematik.de
 */
@OptIn(ExperimentalSerializationApi::class)
@Suite
@SelectPackages(
    "de.gematik.kether.extensions",
    "de.gematik.kether.rpc",
    "de.gematik.kether.eth",
    "de.gematik.kether.abi",
    "de.gematik.kether.contracts"
)
class TestSuite
