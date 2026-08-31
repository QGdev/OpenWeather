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

import fr.qgdev.openweather.data.models.AirQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class AirQualityScalesTest {

    @Test
    fun dominantPollutantIsTheOneOnTheHighestLevel() {
        //  CO is far along a scale a hundred times wider than the others' but still on level 1,
        //  while SO2 is on level 2: SO2 sets the index.
        val airQuality = AirQuality.newBuilder().setCo(4300f).setSo2(20f).build()

        assertEquals(Pollutant.SO2, dominantPollutant(airQuality))
    }

    @Test
    fun dominantPollutantOnTheSameLevelIsTheFurthestAlongItsScale() {
        val airQuality = AirQuality.newBuilder().setSo2(70f).setO3(99f).build()

        assertEquals(Pollutant.O3, dominantPollutant(airQuality))
    }
}
