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

package fr.qgdev.openweather.data.remote.mappers

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [CurrentWeatherMapper].
 *
 * The interesting behaviour here is the optional fields: OneCall omits `wind_gust` in calm
 * conditions, `rain` and `snow` when there is no precipitation, and `wind_deg` when the direction
 * is not meaningful. Each has to degrade to a usable value rather than fail.
 */
class CurrentWeatherMapperTest {

    private val delta = 0.0001f

    private fun currentOf(vararg overrides: Pair<String, Any?>): JSONObject {
        val stream = javaClass.classLoader!!.getResourceAsStream("onecall_2_5_full.json")
            ?: error("onecall_2_5_full.json missing from test resources")
        val current = JSONObject(stream.bufferedReader().readText()).getJSONObject("current")
        overrides.forEach { (key, value) ->
            if (value == null) current.remove(key) else current.put(key, value)
        }
        return current
    }

    @Test
    fun `maps the scalar fields`() {
        val weather = CurrentWeatherMapper.fromOWMToProto(currentOf())

        assertEquals(285.42f, weather.temperature, delta)
        assertEquals(284.71f, weather.temperatureFeelsLike, delta)
        assertEquals(1013, weather.pressure)
        assertEquals(81, weather.humidity)
        assertEquals(75, weather.cloudiness)
        assertEquals(10000, weather.visibility)
    }

    @Test
    fun `takes the first entry of the weather array`() {
        val weather = CurrentWeatherMapper.fromOWMToProto(currentOf())

        assertEquals("Rain", weather.weather)
        assertEquals("légère pluie", weather.weatherDescription)
        assertEquals(500, weather.weatherCode)
    }

    @Test
    fun `converts dt sunrise and sunset to milliseconds`() {
        val weather = CurrentWeatherMapper.fromOWMToProto(currentOf())

        assertEquals(1700000000L * 1000, weather.dt)
        assertEquals(1699948800L * 1000, weather.sunrise)
        assertEquals(1699984800L * 1000, weather.sunset)
    }

    @Test
    fun `uv index is truncated to an integer`() {
        //  uvi is 1.74 in the fixture; the model stores it as an Int.
        assertEquals(1, CurrentWeatherMapper.fromOWMToProto(currentOf()).uvIndex)
    }

    @Test
    fun `rain is read from the 1h key and defaults to zero when absent`() {
        assertEquals(0.42f, CurrentWeatherMapper.fromOWMToProto(currentOf()).rain, delta)
        assertEquals(
            0f,
            CurrentWeatherMapper.fromOWMToProto(currentOf("rain" to null)).rain,
            delta
        )
    }

    @Test
    fun `snow defaults to zero when absent`() {
        //  The fixture has no snow at all, which is the common case.
        assertEquals(0f, CurrentWeatherMapper.fromOWMToProto(currentOf()).snow, delta)
    }

    @Test
    fun `wind gust defaults to zero when absent`() {
        assertEquals(8.75f, CurrentWeatherMapper.fromOWMToProto(currentOf()).windGustSpeed, delta)
        assertEquals(
            0f,
            CurrentWeatherMapper.fromOWMToProto(currentOf("wind_gust" to null)).windGustSpeed,
            delta
        )
    }

    @Test
    fun `missing wind direction is flagged as unreadable rather than reported as north`() {
        val withDirection = CurrentWeatherMapper.fromOWMToProto(currentOf())
        assertTrue(withDirection.isWindDirectionReadable)
        assertEquals(230, withDirection.windDirection)

        //  Without the flag, a missing wind_deg would fall back to 0 and be drawn as due north.
        val without = CurrentWeatherMapper.fromOWMToProto(currentOf("wind_deg" to null))
        assertFalse(without.isWindDirectionReadable)
        assertEquals(0, without.windDirection)
    }
}
