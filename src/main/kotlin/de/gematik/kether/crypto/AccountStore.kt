package de.gematik.kether.crypto

import de.gematik.kether.eth.types.Address
import de.gematik.kether.extensions.hexToByteArray
import de.gematik.kether.extensions.toAccountAddress
import mu.KotlinLogging
import org.apache.tuweni.bytes.Bytes32
import org.hyperledger.besu.crypto.SECP256K1
import org.hyperledger.besu.crypto.SECP256R1
import org.hyperledger.besu.crypto.SECPPrivateKey
import kotlin.random.Random
import kotlin.reflect.full.createInstance

/**
 * Created by rk on 04.11.2022.
 * gematik.de
 */

private val logger = KotlinLogging.logger {}

val accountStore = AccountStore.getInstance()

open class AccountStore protected constructor() {

    companion object {
        // quorum test accounts: https://consensys.net/docs/goquorum/en/stable/tutorials/quorum-dev-quickstart/using-the-quickstart/
        val TEST_ACCOUNT_1 = "testAccount1" // ethsigner account1
        val TEST_ACCOUNT_2 = "testAccount2"
        val TEST_ACCOUNT_3 = "testAccount3"

        // Meta Mask - self created account
        val TEST_ACCOUNT_4 = "testAccount4" // ethsigner account4

        // test accounts for SECP256R1
        val TEST_ACCOUNT_1_R = "testAccount1_SECP256R1"
        val TEST_ACCOUNT_2_R = "testAccount2_SECP256R1"
        val TEST_ACCOUNT_3_R = "testAccount3_SECP256R1"
        val TEST_ACCOUNT_4_R = "testAccount4_SECP256R1"



        private lateinit var instance: AccountStore
        fun getInstance(): AccountStore {
            if (!this::instance.isInitialized) {
                kotlin.runCatching {
                    Class.forName("de.gematik.kether.crypto.AndroidKeyStore").kotlin
                }.onSuccess {
                    instance = it.createInstance() as AccountStore
                }
            }
            if (!this::instance.isInitialized) {
                instance = AccountStore()
            }
            return instance
        }
    }

    data class Account(
        val address: Address,
        val alias: String,
        val accountType: AccountType,
        val keyType: KeyType,
        val privateKey: SECPPrivateKey?
    )

    enum class AccountType { TEST, IN_MEMORY, HARDWARE_BACKED }
    enum class KeyType { SECP256K1, SECP256R1 }

    private val accounts = mutableListOf<Account>()

    init {
        createAndAddTestAccount(
            TEST_ACCOUNT_1,
            KeyType.SECP256K1,
            "0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_2,
            KeyType.SECP256K1,
            "0xc87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_3,
            KeyType.SECP256K1,
            "0xae6ae8e5ccbfb04590405997ee2d52d2b330726137b875053c36d94e974d162f".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_4,
            KeyType.SECP256K1,
            "0x64f2a57bccb23a83e3b8bd0755cc66bfb362f175a4996e066fd964497c504128".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_1_R,
            KeyType.SECP256R1,
            "0x4b49f3978424dd4ad9822f97ef050db21a6822031a4246769646a596a4a194c5".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_2_R,
            KeyType.SECP256R1,
            "0xfe25c47e220516dbbfa0a8d81354905639d15a5dca385f8a43745df5ba79dbcd".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_3_R,
            KeyType.SECP256R1,
            "0x5dad04b88e4cc83778ca8bf68bb29feb1e02ac9b1d366b1f804ee3537ca1d020".hexToByteArray()
        )
        createAndAddTestAccount(
            TEST_ACCOUNT_4_R,
            KeyType.SECP256R1,
            "0x64f2a57bccb23a83e3b8bd0755cc66bfb362f175a4996e066fd964497c504128".hexToByteArray()
        )
    }

    fun getAccount(account: Address): Account {
        return accounts.first { it.address == account }
    }

    fun getAccount(alias: String): Account {
        return accounts.first { it.alias == alias }
    }

    fun createAccount(
        alias: String,
        accountType: AccountType,
        keyType: KeyType,
        privateKey: ByteArray? = null
    ): Boolean {
        return when (accountType) {
            AccountType.TEST -> {
                logger.info { "test accounts are predefined and cannot be created" }
                return false
            }

            AccountType.IN_MEMORY -> {
                val account =
                    createInMemoryAccount(alias, accountType, keyType, privateKey ?: Random.nextBytes(32))
                        ?: return false
                accounts.add(account)
                true
            }

            AccountType.HARDWARE_BACKED -> {
                val account =
                    createHardwareBackedAccount(alias, accountType, privateKey ?: kotlin.random.Random.nextBytes(32))
                        ?: return false
                accounts.add(account)
                true
            }
        }
    }

    private fun createAndAddTestAccount(
        alias: String,
        keyType: KeyType,
        random: ByteArray
    ) {
        when(keyType){
            KeyType.SECP256K1 -> SECP256K1()
            KeyType.SECP256R1 -> SECP256R1()
        }.run {
            val privateKey = createPrivateKey(Bytes32.wrap(random))
            val keyPair = createKeyPair(privateKey)
            val address = keyPair.publicKey.toAccountAddress()
            accounts.add(Account(address, alias, AccountType.TEST, keyType, privateKey))
        }
    }

    open protected fun createInMemoryAccount(
        alias: String,
        accountType: AccountType,
        keyType: KeyType,
        random: ByteArray
    ): Account? {
        return when(keyType){
            KeyType.SECP256K1 -> SECP256K1()
            KeyType.SECP256R1 -> SECP256R1()
        }.run {
            val privateKey = createPrivateKey(Bytes32.wrap(random))
            val keyPair = createKeyPair(privateKey)
            val address = keyPair.publicKey.toAccountAddress()
            Account(address, alias, accountType, keyType, privateKey)
        }
    }

    open protected fun createHardwareBackedAccount(alias: String, type: AccountType, random: ByteArray): Account? {
        logger.info { "AccountStore doesn't support hardware backed keys. Use a specialized store instead, e.g. AndroidAccountStore" }
        return null
    }

}