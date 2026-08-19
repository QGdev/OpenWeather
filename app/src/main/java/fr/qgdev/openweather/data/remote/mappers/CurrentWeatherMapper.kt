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
import fr.qgdev.openweather.data.models.CurrentWeather
import org.json.JSONObject

open class CurrentWeatherMapper private constructor() : Mapper<CurrentWeather> {
    companion object : CurrentWeatherMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): CurrentWeather {

            val currentWeatherJson = Gson()
                .fromJson(jsonObject.toString(), CurrentWeatherJson::class.java)

            return CurrentWeather.newBuilder()
                .setDt(currentWeatherJson.dt * 1000)
                .setWeather(currentWeatherJson.weather[0].main)
                .setWeatherDescription(currentWeatherJson.weather[0].description)
                .setWeatherCode(currentWeatherJson.weather[0].id)
                .setTemperature(currentWeatherJson.temp.toFloat())
                .setTemperatureFeelsLike(currentWeatherJson.feelsLike.toFloat())
                .setPressure(currentWeatherJson.pressure)
                .setHumidity(currentWeatherJson.humidity)
                .setDewPoint(currentWeatherJson.dewPoint.toFloat())
                .setUvIndex(currentWeatherJson.uvi.toInt())
                .setCloudiness(currentWeatherJson.clouds)
                .setVisibility(currentWeatherJson.visibility)
                .setSunrise(currentWeatherJson.sunrise * 1000)
                .setSunset(currentWeatherJson.sunset * 1000)
                .setWindSpeed(currentWeatherJson.windSpeed.toFloat())
                .setIsWindDirectionReadable(currentWeatherJson.windDeg != null)
                .setWindDirection(currentWeatherJson.windDeg ?: 0)
                .setWindGustSpeed(currentWeatherJson.windGust?.toFloat() ?: 0f)
                .setRain(currentWeatherJson.rain?.oneHour?.toFloat() ?: 0f)
                .setSnow(currentWeatherJson.snow?.oneHour?.toFloat() ?: 0f)
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): CurrentWeather {
        return CurrentWeatherMapper.fromOWMToProto(jsonObject)
    }

    private data class CurrentWeatherJson(
        @SerializedName("dt") val dt: Long,
        @SerializedName("weather") val weather: List<WeatherJson>,
        @SerializedName("temp") val temp: Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("dew_point") val dewPoint: Double,
        @SerializedName("uvi") val uvi: Float,
        @SerializedName("clouds") val clouds: Int,
        @SerializedName("visibility") val visibility: Int,
        @SerializedName("sunrise") val sunrise: Long,
        @SerializedName("sunset") val sunset: Long,
        @SerializedName("wind_speed") val windSpeed: Double,
        @SerializedName("wind_deg") val windDeg: Int?,
        @SerializedName("wind_gust") val windGust: Double?,
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