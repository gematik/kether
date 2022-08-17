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
    SerializerTests::class,
    CryptoTests::class,
    EthTests::class,
    EthPubSubTests::class,
    ContractCommonTest::class,
    ContractStorageTests::class,
    ContractHelloWorldTests::class
)

class TestSuite {
}
