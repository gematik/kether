package de.gematik.kether.crypto

import de.gematik.kether.eth.types.Address
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.toAccount
import mu.KotlinLogging
import org.apache.tuweni.bytes.Bytes32
import org.hyperledger.besu.crypto.SECP256K1
import org.hyperledger.besu.crypto.SECPPrivateKey

/**
 * Created by rk on 04.11.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger {}

object AccountStore {
    // quorum test accounts: https://consensys.net/docs/goquorum/en/stable/tutorials/quorum-dev-quickstart/using-the-quickstart/
    val TEST_ACCOUNT_1 = "testAccount1" // ethsigner account1
    val TEST_ACCOUNT_2 = "testAccount2"
    val TEST_ACCOUNT_3 = "testAccount3"
    // Meta Mask - self created account
    val TEST_ACCOUNT_4 = "testAccount4" // ethsigner account4

    data class Key(val address: Address, val alias: String, val type: KeyType, val privateKey: SECPPrivateKey?)

    enum class KeyType { TEST, IN_MEMORY, HARDWARE_BACKED }

    private val accounts = mutableListOf(
        createSECP256K1Account(
            TEST_ACCOUNT_1,
            KeyType.TEST,
            "0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63".hexToByteArray()
        ),
        createSECP256K1Account(
            TEST_ACCOUNT_2,
            KeyType.TEST,
            "0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3".hexToByteArray()
        ),
        createSECP256K1Account(
            TEST_ACCOUNT_3,
            KeyType.TEST,
            "0xae6ae8e5ccbfb04590405997ee2d52d2b330726137b875053c36d94e974d162f".hexToByteArray()
        ),
        createSECP256K1Account(
            TEST_ACCOUNT_4,
            KeyType.TEST,
            "0x64f2a57bccb23a83e3b8bd0755cc66bfb362f175a4996e066fd964497c504128".hexToByteArray()
        )
    )

    fun getAccount(account: Address): Key {
        return accounts.first { it.address == account }
    }

    fun getAccount(alias: String): Key {
        return accounts.first { it.alias == alias }
    }

    fun createAccount(alias: String, type: KeyType, privateKey: ByteArray? = null): Boolean {
        return when (type) {
            KeyType.TEST -> {
                logger.info { "test accounts are predefined and cannot be created" }
                return false
            }

            KeyType.IN_MEMORY -> {
                accounts.add(createSECP256K1Account(alias, type, privateKey ?: kotlin.random.Random.nextBytes(32)))
                return true
            }

            KeyType.HARDWARE_BACKED -> {
                //TODO: implement hardware backed keys
                logger.info { "accounts with hardware backed keys are not implemented yet" }
                return false
            }
        }
    }

    private fun createSECP256K1Account(alias: String, type: KeyType, random: ByteArray): Key {
        val signer = SECP256K1()
        val privateKey = signer.createPrivateKey(Bytes32.wrap(random))
        val keyPair = signer.createKeyPair(privateKey)
        val account = keyPair.publicKey.toAccount()
        return Key(account, alias, type, privateKey)
    }

}