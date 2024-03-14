/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.kether.crypto

import de.gematik.kether.extensions.toHex
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

/**
 * Created by rk on 02.08.2022.
 */
@ExperimentalSerializationApi
class CryptoTests {
    @Test
    fun keyStoreGetAccount() {
        val account = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1)
        assert(account.alias == AccountStore.TEST_ACCOUNT_1)
        assert(account.address.toString() == "0xfe3b557e8fb62b89f4916b721be55ceb828dbd73")
        assert(account.keyPair.privateKey.encoded?.toHex() == "0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63")
    }

    @Test
    fun keyStoreCreateAndDeleteAccount() {
        assert(accountStore.createAccount("test", EcdsaPrivateKey("0x0011223344556677889900112233445566778899001122334455667788990011", EllipticCurve.secp256k1)))
        val account = accountStore.getAccount("test")
        assert(account.alias == "test")
        assert(account.keyPair.publicKey.encoded?.toHex() == "0xe8fd45b27014c09955518127c1c93921d6acc32328879a445994cf255e7a479a1800204dc8532f68c1751bdc0cd75e25b46de0034e11e74cc4f55fd09c1a9077")
        assert(accountStore.deleteAccount(account))
    }

    @Test
    fun keyStoreDeleteAccountUnsuccessful() {
        assert(accountStore.createAccount("test", EcdsaPrivateKey("0x0011223344556677889900112233445566778899001122334455667788990011", EllipticCurve.secp256k1)))
        val account = accountStore.getAccount("test")
        assert(accountStore.deleteAccount(account))
        assert(!accountStore.deleteAccount(account)) // trying to delete non-existing account
    }


}