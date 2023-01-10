package de.gematik.kether.crypto

import de.gematik.kether.extensions.toHex
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 * gematik.de
 */
@ExperimentalSerializationApi
class CryptoTests {
    @Test
    fun keyStoreGetKey() {
        val key = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        assert(key.alias == AccountStore.TEST_ACCOUNT_1)
        assert(key.address.toString() == "0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        assert(key.keyPair.privateKey?.encoded?.toHex() == "0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63")
        assert(key.accountType == AccountStore.AccountType.TEST)
    }

    @Test
    fun keyStoreCreateKey() {
        val success = accountStore.createAccount("test", AccountStore.AccountType.IN_MEMORY, EllipticCurve.secp256k1)
        assert(success)
        val key = accountStore.getAccount("test")
        assert(key.alias == "test")
        assert(key.accountType == AccountStore.AccountType.IN_MEMORY)
        assert(key.keyPair.privateKey?.encoded?.size == 32)
    }


}