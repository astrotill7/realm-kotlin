/*
 * Copyright 2021 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.test.mongodb.shared

import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.AppException
import io.realm.mongodb.AuthenticationProvider
import io.realm.mongodb.Credentials
import io.realm.test.mongodb.TestApp
import io.realm.test.mongodb.asTestApp
import io.realm.test.util.TestHelper
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppTests {

    private lateinit var app: App

    @BeforeTest
    fun setup() {
        app = TestApp()
    }

    @AfterTest
    fun teadDown() {
        if (this::app.isInitialized) {
            app.asTestApp.close()
        }
    }

    @Test
    fun defaultApp() {
        val defaultApp = App.create("foo")
        assertEquals("foo", defaultApp.configuration.appId)
        assertEquals(AppConfiguration.DEFAULT_BASE_URL, defaultApp.configuration.baseUrl)
    }

    @Test
    fun defaultApp_emptyIdThrows() {
        assertFailsWith<IllegalArgumentException> {
            App.create("")
        }
    }

    // TODO Minimal subset of login tests. Migrate AppTest from realm-java, when full API is in
    //  place
    // TODO Exhaustive test on io.realm.mongodb.internal.Provider
    @Test
    fun loginAnonymous() {
        runBlocking {
            app.login(Credentials.anonymous())
        }
    }

    @Test
    fun loginEmailPassword() {
        // Create test user through REST admin api until we have EmailPasswordAuth.registerUser in place
        val randomEmail = TestHelper.randomEmail()
        app.asTestApp.createUser(randomEmail, "asdfasdf")
        runBlocking {
            app.login(Credentials.emailPassword(randomEmail, "asdfasdf"))
        }
    }

    @Test
    fun loginNonCredentialImplThrows() {
        runBlocking {
            assertFailsWith<IllegalArgumentException> {
                app.login(object : Credentials {
                    override val authenticationProvider: AuthenticationProvider =
                        AuthenticationProvider.ANONYMOUS
                })
            }
        }
    }

    @Test
    fun loginInvalidUserThrows() {
        val credentials = Credentials.emailPassword("foo", "bar")
        runBlocking {
            assertFailsWith<AppException> {
                app.login(credentials)
            }.let { exception: AppException ->
                assertTrue(exception.message!!.startsWith("invalid username/password [error_category=3, error_code=50, link_to_server_logs="))
            }
        }
    }
}
