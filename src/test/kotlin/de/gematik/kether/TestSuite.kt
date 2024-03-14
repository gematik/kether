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

package de.gematik.kether

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite

/**
 * Created by rk on 04.08.2022.
 */
@OptIn(ExperimentalSerializationApi::class)
@Suite
@SelectPackages(
    "de.gematik.kether.extensions",
    "de.gematik.kether.crypto",
    "de.gematik.kether.rpc",
    "de.gematik.kether.eth",
    "de.gematik.kether.abi",
    "de.gematik.kether.contracts"
)
class TestSuite
