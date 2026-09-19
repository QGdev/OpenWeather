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

package fr.qgdev.openweather.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ConditionSkyTest {

    private val sun = ConditionFamily.SUN.sky(isDark = true)
    private val night = ConditionFamily.NIGHT.sky(isDark = true)
    private val overcast = ConditionFamily.CLOUD.sky(isDark = true)

    /**
     * Test the sky of a clear condition.
     * Test will not pass if it isn't the sun by day and the night after dark.
     */
    @Test
    fun `a clear sky is the sun by day and the night after dark`() {
        assertEquals(sun, conditionSky(800, isDaytime = true, cloudiness = 0, isDark = true))
        assertEquals(night, conditionSky(800, isDaytime = false, cloudiness = 0, isDark = true))
    }

    /**
     * Test the sky of a fully covered condition.
     * Test will not pass if it isn't the overcast one, by day as by night.
     */
    @Test
    fun `a fully covered sky is the overcast one, day or night`() {
        assertEquals(overcast, conditionSky(804, isDaytime = true, cloudiness = 100, isDark = true))
        assertEquals(
            overcast.copy(halo = night.halo),
            conditionSky(804, isDaytime = false, cloudiness = 100, isDark = true)
        )
    }

    /**
     * Test the glow of an overcast night.
     * Test will not pass if it keeps a sun glow.
     */
    @Test
    fun `an overcast night has no sun glow`() {
        val overcastNight = conditionSky(804, isDaytime = false, cloudiness = 100, isDark = true)
        assertNotEquals(overcast.halo, overcastNight.halo)
    }

    /**
     * Test the skies of few clouds and of broken clouds.
     * Test will not pass if they share the same sky.
     */
    @Test
    fun `few clouds and broken clouds no longer share one sky`() {
        val few = conditionSky(801, isDaytime = true, cloudiness = 15, isDark = true)
        val broken = conditionSky(803, isDaytime = true, cloudiness = 75, isDark = true)

        assertNotEquals(few, broken)
        assertNotEquals(overcast, few)
        assertNotEquals(sun, few)
    }

    /**
     * Test the sky of a partly cloudy night.
     * Test will not pass if it doesn't keep some of the night.
     */
    @Test
    fun `a partly cloudy night keeps some of the night`() {
        val partlyCloudyNight = conditionSky(802, isDaytime = false, cloudiness = 40, isDark = true)
        val partlyCloudyDay = conditionSky(802, isDaytime = true, cloudiness = 40, isDark = true)

        assertNotEquals(partlyCloudyDay, partlyCloudyNight)
    }

    /**
     * Test the skies of precipitation and fog.
     * Test will not pass if the cloud cover changes them.
     */
    @Test
    fun `precipitation and fog ignore the cover`() {
        assertEquals(
            ConditionFamily.RAIN.sky(isDark = true),
            conditionSky(500, isDaytime = true, cloudiness = 20, isDark = true)
        )
        assertEquals(
            ConditionFamily.CLOUD.sky(isDark = true),
            conditionSky(741, isDaytime = true, cloudiness = 0, isDark = true)
        )
    }

    /**
     * Test an out of range cloud cover.
     * Test will not pass if it isn't clamped.
     */
    @Test
    fun `an out of range cover is clamped`() {
        assertEquals(overcast, conditionSky(804, isDaytime = true, cloudiness = 140, isDark = true))
        assertEquals(sun, conditionSky(800, isDaytime = true, cloudiness = -5, isDark = true))
    }
}
