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

import fr.qgdev.openweather.data.models.DailyForecast
import org.json.JSONObject

/**
 * DailyForecastMapper
 * <p>
 * Maps a daily forecast of an OpenWeatherMap response to a DailyForecast protobuf message.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class DailyForecastMapper private constructor() : Mapper<DailyForecast> {
    companion object : DailyForecastMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): DailyForecast {
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
            val temp = jsonObject.getJSONObject("temp")
            val feelsLike = jsonObject.getJSONObject("feels_like")

            return DailyForecast.newBuilder()
                .setDt(jsonObject.optLong("dt") * 1000)
                .setWeather(weather.getString("main"))
                .setWeatherDescription(weather.getString("description"))
                .setWeatherCode(weather.optInt("id"))
                .setTemperatureMorning(temp.optDouble("morn", 0.0).toFloat())
                .setTemperatureDay(temp.optDouble("day", 0.0).toFloat())
                .setTemperatureEvening(temp.optDouble("eve", 0.0).toFloat())
                .setTemperatureNight(temp.optDouble("night", 0.0).toFloat())
                .setTemperatureMinimum(temp.optDouble("min", 0.0).toFloat())
                .setTemperatureMaximum(temp.optDouble("max", 0.0).toFloat())
                .setTemperatureMorningFeelsLike(feelsLike.optDouble("morn", 0.0).toFloat())
                .setTemperatureDayFeelsLike(feelsLike.optDouble("day", 0.0).toFloat())
                .setTemperatureEveningFeelsLike(feelsLike.optDouble("eve", 0.0).toFloat())
                .setTemperatureNightFeelsLike(feelsLike.optDouble("night", 0.0).toFloat())
                .setPressure(jsonObject.optInt("pressure"))
                .setHumidity(jsonObject.optInt("humidity"))
                .setDewPoint(jsonObject.optDouble("dew_point", 0.0).toFloat())
                .setCloudiness(jsonObject.optInt("clouds"))
                .setSunriseDt(jsonObject.optLong("sunrise") * 1000)
                .setSunsetDt(jsonObject.optLong("sunset") * 1000)
                .setUvIndex(jsonObject.optDouble("uvi", 0.0).toInt())
                .setMoonriseDt(jsonObject.optLong("moonrise") * 1000)
                .setMoonsetDt(jsonObject.optLong("moonset") * 1000)
                .setMoonPhase(jsonObject.optDouble("moon_phase", 0.0).toFloat())
                .setWindSpeed(jsonObject.optDouble("wind_speed", 0.0).toFloat())
                .setWindDirection(jsonObject.optInt("wind_deg"))
                .setWindGustSpeed(jsonObject.optDouble("wind_gust", 0.0).toFloat())
                .setPop(jsonObject.optDouble("pop", 0.0).toFloat())
                .setRain(jsonObject.optDouble("rain", 0.0).toFloat())
                .setSnow(jsonObject.optDouble("snow", 0.0).toFloat())
                .build()
        }
    }
    override fun fromOWMToProto(jsonObject: JSONObject): DailyForecast {
        return DailyForecastMapper.fromOWMToProto(jsonObject)
    }
}
