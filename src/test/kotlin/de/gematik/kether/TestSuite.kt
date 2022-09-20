package de.gematik.kether

import de.gematik.kether.contracts.ContractCommonTest
import de.gematik.kether.contracts.ContractHelloWorldTests
import de.gematik.kether.contracts.ContractStorageTests
import de.gematik.kether.extensions.CryptoTests
import de.gematik.kether.rpc.EthPubSubTests
import de.gematik.kether.rpc.EthTests
import de.gematik.kether.rpc.SerializerTests
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
