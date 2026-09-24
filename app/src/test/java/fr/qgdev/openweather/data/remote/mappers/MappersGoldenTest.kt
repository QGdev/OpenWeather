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
import org.junit.Test

/**
 * Pins every field every mapper writes, against `src/test/resources/mappers_golden.txt`.
 *
 * The golden file was produced by the mappers when they still parsed through Gson, which left a
 * missing number at 0 and a missing object at null. It keeps any rewrite of the mappers honest
 * about those defaults, which the counts in [PlaceDataMapperTest] do not see.
 */
class MappersGoldenTest {

    private fun resource(name: String): String =
        javaClass.classLoader!!.getResourceAsStream(name)?.bufferedReader()?.readText()
            ?: error("$name missing from test resources")

    /** Every mapper's output, one section per mapped object, in a stable order. */
    private fun render(): String {
        val full = JSONObject(resource("onecall_2_5_full.json"))
        val sections = mutableListOf<Pair<String, Any>>()

        sections += "current" to CurrentWeatherMapper.fromOWMToProto(full.getJSONObject("current"))
        sections += "minutely 0" to MinutelyForecastMapper.fromOWMToProto(full.getJSONArray("minutely").getJSONObject(0))
        for (i in 0 until full.getJSONArray("hourly").length())
            sections += "hourly $i" to HourlyForecastMapper.fromOWMToProto(full.getJSONArray("hourly").getJSONObject(i))
        for (i in 0 until full.getJSONArray("daily").length())
            sections += "daily $i" to DailyForecastMapper.fromOWMToProto(full.getJSONArray("daily").getJSONObject(i))
        sections += "alert 0" to WeatherAlertMapper.fromOWMToProto(full.getJSONArray("alerts").getJSONObject(0))

        //  Sparse: no visibility, a null wind direction, no gust, no rain nor snow.
        sections += "current sparse" to CurrentWeatherMapper.fromOWMToProto(JSONObject(
            """{"dt":1700000000,"weather":[{"id":800,"main":"Clear","description":"ciel dégagé"}],
               "temp":12.5,"feels_like":11.0,"pressure":1015,"humidity":60,"dew_point":4.8,"uvi":0.4,
               "clouds":0,"sunrise":1699990000,"sunset":1700030000,"wind_speed":3.1,"wind_deg":null}"""
        ))
        sections += "air quality" to AirQualityMapper.fromOWMToProto(JSONObject(
            """{"list":[{"main":{"aqi":2},"components":{"co":230.31,"no":0.02,"no2":12.5,"o3":61.1,
               "so2":1.4,"pm2_5":4.87,"pm10":7.3,"nh3":0.56}}]}"""
        ))

        //  The first line of a lite message's toString is its identity hash, not its content.
        return sections.joinToString("") { (name, proto) ->
            "== $name\n" + proto.toString().lines().drop(1).joinToString("\n") + "\n"
        }
    }

    @Test
    fun `every mapper writes the same fields as when it parsed through Gson`() {
        assertEquals(resource("mappers_golden.txt"), render())
    }
}
