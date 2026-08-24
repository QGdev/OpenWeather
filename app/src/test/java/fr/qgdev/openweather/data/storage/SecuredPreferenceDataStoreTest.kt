/*
 *  Copyright (c) 2019 - 2026
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather.
 *
 *  OpenWeather is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenWeather is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests the recovery policy behind [SecuredPreferenceDataStore].
 *
 * The class itself needs a Context and the Android keystore, so it cannot be built on the JVM.
 * [openWithOneRecoveryAttempt] holds the part worth testing: what happens when the encrypted
 * preferences cannot be opened because the master key is gone.
 */
class SecuredPreferenceDataStoreTest {

    private class Boom(message: String) : Exception(message)

    @Test
    fun `opens without discarding anything when the first attempt succeeds`() {
        var opens = 0
        var discards = 0

        val result = openWithOneRecoveryAttempt(
            open = { opens++; "prefs" },
            discardCorruptedState = { discards++ }
        )

        assertEquals("prefs", result)
        assertEquals(1, opens)
        assertEquals("stored state must not be discarded on the happy path", 0, discards)
    }

    @Test
    fun `discards the stored state and retries once when the first attempt fails`() {
        var opens = 0
        var discards = 0

        val result = openWithOneRecoveryAttempt(
            open = {
                opens++
                if (opens == 1) throw Boom("master key unavailable") else "prefs"
            },
            discardCorruptedState = { discards++ }
        )

        assertEquals("prefs", result)
        assertEquals(2, opens)
        assertEquals(1, discards)
    }

    @Test
    fun `gives up after one retry rather than looping`() {
        var opens = 0
        var discards = 0

        try {
            openWithOneRecoveryAttempt<String>(
                open = { opens++; throw Boom("attempt $opens") },
                discardCorruptedState = { discards++ }
            )
            fail("expected the second failure to propagate")
        } catch (expected: IllegalStateException) {
            //  If this ever grows past one retry the app would spin on an unrecoverable device.
            assertEquals(2, opens)
            assertEquals(1, discards)
        }
    }

    @Test
    fun `keeps both failures so a crash report can be diagnosed`() {
        var opens = 0

        try {
            openWithOneRecoveryAttempt<String>(
                open = { opens++; throw Boom("attempt $opens") },
                discardCorruptedState = { }
            )
            fail("expected the second failure to propagate")
        } catch (expected: IllegalStateException) {
            //  The previous implementation threw a bare RuntimeException and dropped the cause
            //  entirely, leaving nothing to diagnose from.
            val cause = expected.cause
            assertTrue("the second failure must be the cause", cause is Boom)
            assertEquals("attempt 2", cause!!.message)

            val suppressed = cause.suppressed
            assertEquals("the first failure must be kept too", 1, suppressed.size)
            assertEquals("attempt 1", suppressed[0].message)
        }
    }

    @Test
    fun `returns the instance from the successful attempt, not the failed one`() {
        val recovered = Any()

        val result = openWithOneRecoveryAttempt(
            open = object : () -> Any {
                var calls = 0
                override fun invoke(): Any {
                    calls++
                    if (calls == 1) throw Boom("first")
                    return recovered
                }
            },
            discardCorruptedState = { }
        )

        assertSame(recovered, result)
    }
}
