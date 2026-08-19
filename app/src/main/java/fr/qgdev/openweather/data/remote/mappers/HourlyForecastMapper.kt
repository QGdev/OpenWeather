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
import fr.qgdev.openweather.data.models.HourlyForecast
import org.json.JSONObject

open class HourlyForecastMapper private constructor() : Mapper<HourlyForecast> {
    companion object : HourlyForecastMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): HourlyForecast {
            val result = Gson().fromJson(jsonObject.toString(), HourlyWeatherJson::class.java)

            return HourlyForecast.newBuilder()
                .setDt(result.dt * 1000)
                .setWeather(result.weather[0].main)
                .setWeatherDescription(result.weather[0].description)
                .setWeatherCode(result.weather[0].id)
                .setTemperature(result.temp.toFloat())
                .setTemperatureFeelsLike(result.feelsLike.toFloat())
                .setPressure(result.pressure)
                .setHumidity(result.humidity)
                .setDewPoint(result.dewPoint.toFloat())
                .setVisibility(result.visibility)
                .setCloudiness(result.clouds)
                .setUvIndex(result.uvi.toInt())
                .setWindSpeed(result.windSpeed.toFloat())
                .setWindDirection(result.windDeg)
                .setWindGustSpeed(result.windGust?.toFloat() ?: 0f)
                .setPop(result.pop.toFloat())
                .setRain(result.rain?.oneHour?.toFloat() ?: 0f)
                .setSnow(result.snow?.oneHour?.toFloat() ?: 0f)
                .build()
        }
    }
    override fun fromOWMToProto(jsonObject: JSONObject): HourlyForecast {
        return HourlyForecastMapper.fromOWMToProto(jsonObject);
    }

    private data class HourlyWeatherJson(
        @SerializedName("dt") val dt: Long,
        @SerializedName("weather") val weather: List<WeatherJson>,
        @SerializedName("temp") val temp: Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("dew_point") val dewPoint: Double,
        @SerializedName("visibility") val visibility: Int,
        @SerializedName("clouds") val clouds: Int,
        @SerializedName("uvi") val uvi: Double,
        @SerializedName("wind_speed") val windSpeed: Double,
        @SerializedName("wind_deg") val windDeg: Int,
        @SerializedName("wind_gust") val windGust: Double?,
        @SerializedName("pop") val pop: Double,
        @SerializedName("rain") val rain: RainJson?,
        @SerializedName("snow") val snow: SnowJson?
    )

    private data class WeatherJson(
        @SerializedName("main") val main: String,
        @SerializedName("description") val description: String,
        @SerializedName("id") val id: Int
    )

    private data class RainJson(@SerializedName("1h") val oneHour: Double)
    private data class SnowJson(@SerializedName("1h") val oneHour: Double)
}