/*
 *  Copyright (c) 2019 - 2025
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

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import fr.qgdev.openweather.data.models.MinutelyForecast
import org.json.JSONObject

open class MinutelyForecastMapper private constructor() : Mapper<MinutelyForecast> {
    companion object : MinutelyForecastMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): MinutelyForecast {
            val result = Gson().fromJson(jsonObject.toString(), MinutelyWeatherJson::class.java)
            return MinutelyForecast.newBuilder()
                .setDt(result.dt * 1000)
                .setPrecipitation(result.precipitation.toFloat())
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): MinutelyForecast {
        return MinutelyForecastMapper.fromOWMToProto(jsonObject);
    }

    private data class MinutelyWeatherJson(
        @SerializedName("dt") val dt: Long,
        @SerializedName("precipitation") val precipitation: Double
    )
}