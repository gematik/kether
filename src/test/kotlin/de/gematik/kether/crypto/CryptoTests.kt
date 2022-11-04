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
        val key = AccountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        assert(key.alias == AccountStore.TEST_ACCOUNT_1)
        assert(key.address.toString() == "0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        assert(key.privateKey?.encodedBytes?.toArray()?.toHex() == "0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63")
        assert(key.type == AccountStore.KeyType.TEST)
    }

    @Test
    fun keyStoreCreateKey() {
        val success = AccountStore.createAccount("test", AccountStore.KeyType.IN_MEMORY)
        assert(success)
        val key = AccountStore.getAccount("test")
        assert(key.alias == "test")
        assert(key.type == AccountStore.KeyType.IN_MEMORY)
        assert(key.privateKey?.encodedBytes?.size() == 32)
    }


}