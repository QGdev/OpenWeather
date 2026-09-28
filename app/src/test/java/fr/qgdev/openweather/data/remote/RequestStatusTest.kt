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

package fr.qgdev.openweather.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class RequestStatusTest {

    /**
     * Test the status of a request which got no response.
     * Test will not pass if it isn't read as no answer.
     */
    @Test
    fun `no response is no answer`() {
        assertEquals(RequestStatus.NO_ANSWER, RequestStatus.fromHttpStatus(null))
    }

    /**
     * Test the HTTP statuses both services react to.
     * Test will not pass if one of them maps to another status.
     */
    @Test
    fun `known HTTP statuses map to their status`() {
        assertEquals(RequestStatus.TOO_MANY_REQUESTS, RequestStatus.fromHttpStatus(429))
        assertEquals(RequestStatus.NOT_FOUND, RequestStatus.fromHttpStatus(404))
        assertEquals(RequestStatus.AUTH_FAILED, RequestStatus.fromHttpStatus(401))
        assertEquals(RequestStatus.AUTH_FAILED, RequestStatus.fromHttpStatus(403))
    }

    /**
     * Test the statuses nothing specific is shown for.
     * Test will not pass if a server error isn't read as an unknown error.
     */
    @Test
    fun `other HTTP statuses are unknown errors`() {
        assertEquals(RequestStatus.UNKNOWN_ERROR, RequestStatus.fromHttpStatus(500))
        assertEquals(RequestStatus.UNKNOWN_ERROR, RequestStatus.fromHttpStatus(400))
    }
}
