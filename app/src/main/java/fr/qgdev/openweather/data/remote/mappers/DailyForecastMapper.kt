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
import fr.qgdev.openweather.data.models.DailyForecast
import org.json.JSONObject

open class DailyForecastMapper private constructor() : Mapper<DailyForecast> {
    companion object : DailyForecastMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): DailyForecast {
            val result = Gson().fromJson(jsonObject.toString(), DailyWeatherJson::class.java)

            return DailyForecast.newBuilder()
                .setDt(result.dt * 1000)
                .setWeather(result.weather[0].main)
                .setWeatherDescription(result.weather[0].description)
                .setWeatherCode(result.weather[0].id)
                .setTemperatureMorning(result.temp.morning.toFloat())
                .setTemperatureDay(result.temp.day.toFloat())
                .setTemperatureEvening(result.temp.evening.toFloat())
                .setTemperatureNight(result.temp.night.toFloat())
                .setTemperatureMinimum(result.temp.min.toFloat())
                .setTemperatureMaximum(result.temp.max.toFloat())
                .setTemperatureMorningFeelsLike(result.feelsLike.morning.toFloat())
                .setTemperatureDayFeelsLike(result.feelsLike.day.toFloat())
                .setTemperatureEveningFeelsLike(result.feelsLike.evening.toFloat())
                .setTemperatureNightFeelsLike(result.feelsLike.night.toFloat())
                .setPressure(result.pressure)
                .setHumidity(result.humidity)
                .setDewPoint(result.dewPoint.toFloat())
                .setCloudiness(result.clouds)
                .setSunriseDt(result.sunrise * 1000)
                .setSunsetDt(result.sunset * 1000)
                .setUvIndex(result.uvi.toInt())
                .setMoonriseDt(result.moonrise * 1000)
                .setMoonsetDt(result.moonset * 1000)
                .setMoonPhase(result.moonPhase.toFloat())
                .setWindSpeed(result.windSpeed.toFloat())
                .setWindDirection(result.windDeg)
                .setWindGustSpeed(result.windGust?.toFloat() ?: 0f)
                .setPop(result.pop.toFloat())
                .setRain(result.rain?.toFloat() ?: 0f)
                .setSnow(result.snow?.toFloat() ?: 0f)
                .build()
        }
    }
    override fun fromOWMToProto(jsonObject: JSONObject): DailyForecast {
        return DailyForecastMapper.fromOWMToProto(jsonObject)
    }

    private data class DailyWeatherJson(
        @SerializedName("dt") val dt: Long,
        @SerializedName("weather") val weather: List<WeatherJson>,
        @SerializedName("temp") val temp: TemperatureJson,
        @SerializedName("feels_like") val feelsLike: FeelsLikeJson,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int,
        @SerializedName("dew_point") val dewPoint: Double,
        @SerializedName("clouds") val clouds: Int,
        @SerializedName("sunrise") val sunrise: Long,
        @SerializedName("sunset") val sunset: Long,
        @SerializedName("uvi") val uvi: Double,
        @SerializedName("moonrise") val moonrise: Long,
        @SerializedName("moonset") val moonset: Long,
        @SerializedName("moon_phase") val moonPhase: Double,
        @SerializedName("wind_speed") val windSpeed: Double,
        @SerializedName("wind_deg") val windDeg: Int,
        @SerializedName("wind_gust") val windGust: Double?,
        @SerializedName("pop") val pop: Double,
        @SerializedName("rain") val rain: Double?,
        @SerializedName("snow") val snow: Double?
    )

    private data class WeatherJson(
        @SerializedName("main") val main: String,
        @SerializedName("description") val description: String,
        @SerializedName("id") val id: Int
    )

    private data class TemperatureJson(
        @SerializedName("morn") val morning: Double,
        @SerializedName("day") val day: Double,
        @SerializedName("eve") val evening: Double,
        @SerializedName("night") val night: Double,
        @SerializedName("min") val min: Double,
        @SerializedName("max") val max: Double
    )

    private data class FeelsLikeJson(
        @SerializedName("morn") val morning: Double,
        @SerializedName("day") val day: Double,
        @SerializedName("eve") val evening: Double,
        @SerializedName("night") val night: Double
    )
}