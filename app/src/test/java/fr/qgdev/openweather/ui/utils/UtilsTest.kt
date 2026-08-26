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

package fr.qgdev.openweather.ui.utils

import fr.qgdev.openweather.data.models.CurrentWeather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Covers the three presentation rules that were wrong, and stayed wrong because nothing checked
 * them: an impossible daylight test, a probability read as if it were already a percentage, and a
 * country shown as the raw code the search returned.
 */
class UtilsTest {

    private fun weatherAt(dt: Long, sunrise: Long, sunset: Long): CurrentWeather =
        CurrentWeather.newBuilder()
            .setDt(dt)
            .setSunrise(sunrise)
            .setSunset(sunset)
            .build()

    //  A September day in Nantes, in epoch milliseconds.
    private val sunrise = 1_758_000_000_000L
    private val sunset = 1_758_043_200_000L   // twelve hours later

    @Test
    fun `midday is daytime`() {
        assertTrue(weatherAt(sunrise + 21_600_000L, sunrise, sunset).isDaytime())
    }

    @Test
    fun `an instant before sunrise is not daytime`() {
        assertFalse(weatherAt(sunrise - 1L, sunrise, sunset).isDaytime())
    }

    @Test
    fun `an instant after sunset is not daytime`() {
        assertFalse(weatherAt(sunset + 1L, sunrise, sunset).isDaytime())
    }

    @Test
    fun `sunrise and sunset themselves count as daytime`() {
        assertTrue(weatherAt(sunrise, sunrise, sunset).isDaytime())
        assertTrue(weatherAt(sunset, sunrise, sunset).isDaytime())
    }

    @Test
    fun `a probability of one half reads as fifty percent`() {
        assertEquals(50f, 0.5f.toPercentage(), 0.001f)
    }

    @Test
    fun `the extremes of the probability range are preserved`() {
        assertEquals(0f, 0f.toPercentage(), 0.001f)
        assertEquals(100f, 1f.toPercentage(), 0.001f)
    }

    @Test
    fun `a probability that used to truncate to zero no longer does`() {
        //  0.65 handed to an integer formatter became "0 %", which is what every forecast showed.
        assertEquals(65, 0.65f.toPercentage().toInt())
    }

    @Test
    fun `a lowercase country code becomes a country name`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.ENGLISH)
            assertEquals("France", countryNameFromCode("fr"))
            assertEquals("Italy", countryNameFromCode("it"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `the country name follows the user's language`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.FRENCH)
            assertEquals("Italie", countryNameFromCode("it"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `an unknown code falls back to its uppercase form`() {
        assertEquals("ZZ", countryNameFromCode("zz"))
    }

    @Test
    fun `a missing country code yields nothing to display`() {
        assertEquals("", countryNameFromCode(""))
    }
}
