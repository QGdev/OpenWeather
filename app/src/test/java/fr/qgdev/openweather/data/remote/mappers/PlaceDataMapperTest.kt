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

import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [PlaceDataMapper], the entry point that turns a OneCall response into a Place.
 *
 * The fixture in `src/test/resources/onecall_2_5_full.json` is a trimmed but structurally faithful
 * OneCall 2.5 payload. Keeping it checked in means any change to the response shape - including a
 * future move to OneCall 3.0 - shows up here as a failing test rather than as missing data on a
 * device.
 */
class PlaceDataMapperTest {

    private fun fixture(): JSONObject {
        val stream = javaClass.classLoader!!.getResourceAsStream("onecall_2_5_full.json")
            ?: error("onecall_2_5_full.json missing from test resources")
        return JSONObject(stream.bufferedReader().readText())
    }

    // region Full response

    @Test
    fun `full response maps every section`() {
        val place = PlaceDataMapper.fromOWMToProtoBuilder(fixture())!!.buildPartial()

        assertEquals(3, place.minutelyForecastListCount)
        assertEquals(2, place.hourlyForecastListCount)
        assertEquals(2, place.dailyForecastListCount)
        assertEquals(1, place.weatherAlertsListCount)
        assertTrue(place.hasCurrentWeather())
    }

    @Test
    fun `timestamps are converted from seconds to milliseconds`() {
        val place = PlaceDataMapper.fromOWMToProtoBuilder(fixture())!!.buildPartial()

        //  The API reports seconds; the models store milliseconds.
        assertEquals(1700000000L * 1000, place.currentWeather.dt)
        assertEquals(1700000040L * 1000, place.getMinutelyForecastList(0).dt)
        assertEquals(1700002800L * 1000, place.getHourlyForecastList(0).dt)

        //  lastAvailableWeatherDataTime is deliberately NOT converted - it is taken straight from
        //  current.dt in seconds. Pinned here so the inconsistency is a decision, not a surprise.
        assertEquals(1700000000L, place.properties.lastAvailableWeatherDataTime)
    }

    @Test
    fun `alert is mapped with its sender and tags`() {
        val alert = PlaceDataMapper.fromOWMToProtoBuilder(fixture())!!.buildPartial()
            .getWeatherAlertsList(0)

        assertEquals("Météo-France", alert.sender)
        assertEquals("Vent violent", alert.event)
    }

    // endregion

    // region Degraded responses
    //
    //  OneCall omits minutely for locations it does not cover and omits alerts when none are
    //  active, so those two must never be treated as required. hourly and daily are normally
    //  always present, but a truncated response should still degrade to an empty list rather
    //  than throw - before this was fixed, a missing "hourly" or "daily" threw JSONException
    //  out of the Volley success callback.

    @Test
    fun `missing minutely yields an empty list`() {
        val json = fixture().apply { remove("minutely") }
        val place = PlaceDataMapper.fromOWMToProtoBuilder(json)!!.buildPartial()

        assertEquals(0, place.minutelyForecastListCount)
        assertEquals(2, place.hourlyForecastListCount)
    }

    @Test
    fun `missing alerts yields an empty list`() {
        val json = fixture().apply { remove("alerts") }
        val place = PlaceDataMapper.fromOWMToProtoBuilder(json)!!.buildPartial()

        assertEquals(0, place.weatherAlertsListCount)
    }

    @Test
    fun `missing hourly yields an empty list instead of throwing`() {
        val json = fixture().apply { remove("hourly") }
        val place = PlaceDataMapper.fromOWMToProtoBuilder(json)!!.buildPartial()

        assertEquals(0, place.hourlyForecastListCount)
        assertEquals(2, place.dailyForecastListCount)
    }

    @Test
    fun `missing daily yields an empty list instead of throwing`() {
        val json = fixture().apply { remove("daily") }
        val place = PlaceDataMapper.fromOWMToProtoBuilder(json)!!.buildPartial()

        assertEquals(0, place.dailyForecastListCount)
        assertEquals(2, place.hourlyForecastListCount)
    }

    @Test
    fun `every optional section missing at once still maps`() {
        val json = fixture().apply {
            remove("minutely"); remove("hourly"); remove("daily"); remove("alerts")
        }
        val place = PlaceDataMapper.fromOWMToProtoBuilder(json)!!.buildPartial()

        assertTrue(place.hasCurrentWeather())
        assertEquals(0, place.minutelyForecastListCount)
        assertEquals(0, place.hourlyForecastListCount)
        assertEquals(0, place.dailyForecastListCount)
        assertEquals(0, place.weatherAlertsListCount)
    }

    // endregion

    /**
     * A response with no `current` is unusable, so this one is expected to throw. That is the line
     * between "degraded" and "unusable" - do not soften it without deciding what the UI shows for a
     * place that has no current weather at all.
     */
    @Test(expected = JSONException::class)
    fun `missing current weather is an error`() {
        PlaceDataMapper.fromOWMToProtoBuilder(fixture().apply { remove("current") })
    }
}
