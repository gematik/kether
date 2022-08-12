package de.gematik.kether.rpc

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Created by rk on 04.08.2022.
 * gematik.de
 */
@OptIn(ExperimentalSerializationApi::class)
@RunWith(Suite::class)
@Suite.SuiteClasses(
    EthTests::class,
    SerializerTests::class,
    CryptoTest::class,
    ContractTests::class
)

class TestSuite {
}
