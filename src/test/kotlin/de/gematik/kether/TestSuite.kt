package de.gematik.kether

import de.gematik.kether.abi.AbiDecodingTests
import de.gematik.kether.abi.AbiEncodingTests
import de.gematik.kether.contracts.*
import de.gematik.kether.extensions.CryptoTests
import de.gematik.kether.eth.EthPubSubTests
import de.gematik.kether.eth.EthTests
import de.gematik.kether.eth.EthSerializerTests
import de.gematik.kether.eth.EthTestsAssumingContract
import de.gematik.kether.extensions.RlpTests
import de.gematik.kether.rpc.RpcSerializerTests
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
    RpcSerializerTests::class,
    CryptoTests::class,
    RlpTests::class,
    EthSerializerTests::class,
    EthTests::class,
    EthTestsAssumingContract::class,
    EthPubSubTests::class,
    AbiEncodingTests::class,
    AbiDecodingTests::class,
    ContractStorageTests::class,
    ContractHelloWorldTests::class,
    ContractGLDTokenTests::class,
    ContractStorageRawTXTests::class,
    ContractHelloWorldRawTxTests::class,
    ContractGLDTokenRawTxTests::class
)

class TestSuite {
}
