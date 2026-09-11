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

package fr.qgdev.openweather.ui.place.detail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.SimpleTimeZone

class DayMarkerTest {

    private val hour = 60 * 60 * 1000L
    //  2026-09-22 23:00 UTC.
    private val elevenPm = 1_790_118_000_000L
    private val utc = SimpleTimeZone(0, "UTC")

    @Test
    fun `midnight in the place's zone starts a new day`() {
        assertTrue(startsNewDay(elevenPm, elevenPm + hour, utc))
    }

    @Test
    fun `two hours of the same day do not`() {
        assertFalse(startsNewDay(elevenPm - hour, elevenPm, utc))
    }

    @Test
    fun `the day is the place's, not the phone's`() {
        //  23:00 and 00:00 UTC are 01:00 and 02:00 in UTC+2: the same day there.
        assertFalse(startsNewDay(elevenPm, elevenPm + hour, SimpleTimeZone(2 * 3_600_000, "UTC+2")))
    }

    /**
     * Test an hour before the first sunrise of the strip.
     * Test will not pass if the early morning hours are shown as daytime.
     */
    @Test
    fun `hours before the first sunrise are night`() {
        assertFalse(isDaytimeAt(3 * hour, listOf(6 * hour, 30 * hour), listOf(20 * hour, 44 * hour)))
    }

    /**
     * Test an hour before the first sunset when the strip starts in daylight.
     * Test will not pass if it is shown as night.
     */
    @Test
    fun `hours before the first sunset are day when no sunrise precedes`() {
        assertTrue(isDaytimeAt(10 * hour, listOf(30 * hour), listOf(20 * hour, 44 * hour)))
    }
}
