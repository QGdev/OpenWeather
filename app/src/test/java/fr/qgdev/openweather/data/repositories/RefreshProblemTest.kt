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

package fr.qgdev.openweather.data.repositories

import fr.qgdev.openweather.data.remote.RequestStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RefreshProblemTest {

    /**
     * Test a refresh which met no failure.
     * Test will not pass if a problem is reported.
     */
    @Test
    fun `a refresh with no failure has no problem`() {
        assertNull(RefreshProblem.of(emptyList(), 0L))
    }

    /**
     * Test failures which belong to single places.
     * Test will not pass if they raise a banner.
     */
    @Test
    fun `failures that belong to single places are no banner`() {
        assertNull(
            RefreshProblem.of(listOf(RequestStatus.NO_ANSWER, RequestStatus.UNKNOWN_ERROR), 0L)
        )
    }

    /**
     * Test the priority of a refused API key.
     * Test will not pass if being offline or the quota outranks it.
     */
    @Test
    fun `a refused key outranks being offline and the quota`() {
        val problem = RefreshProblem.of(
            listOf(RequestStatus.TOO_MANY_REQUESTS, RequestStatus.NOT_CONNECTED, RequestStatus.AUTH_FAILED),
            42L
        )
        assertEquals(RefreshProblem(RequestStatus.AUTH_FAILED, 42L), problem)
    }

    /**
     * Test the priority of being offline.
     * Test will not pass if the quota outranks it.
     */
    @Test
    fun `being offline outranks the quota`() {
        val problem = RefreshProblem.of(
            listOf(RequestStatus.TOO_MANY_REQUESTS, RequestStatus.NOT_CONNECTED),
            0L
        )
        assertEquals(RequestStatus.NOT_CONNECTED, problem?.status)
    }
}
