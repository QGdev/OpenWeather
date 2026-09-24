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

import fr.qgdev.openweather.data.models.HourlyForecast
import org.json.JSONObject

/**
 * HourlyForecastMapper
 * <p>
 * Maps an hourly forecast of an OpenWeatherMap response to a HourlyForecast protobuf message.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class HourlyForecastMapper private constructor() : Mapper<HourlyForecast> {
    companion object : HourlyForecastMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): HourlyForecast {
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
            return HourlyForecast.newBuilder()
                .setDt(jsonObject.optLong("dt") * 1000)
                .setWeather(weather.getString("main"))
                .setWeatherDescription(weather.getString("description"))
                .setWeatherCode(weather.optInt("id"))
                .setTemperature(jsonObject.optDouble("temp", 0.0).toFloat())
                .setTemperatureFeelsLike(jsonObject.optDouble("feels_like", 0.0).toFloat())
                .setPressure(jsonObject.optInt("pressure"))
                .setHumidity(jsonObject.optInt("humidity"))
                .setDewPoint(jsonObject.optDouble("dew_point", 0.0).toFloat())
                .setVisibility(jsonObject.optInt("visibility"))
                .setCloudiness(jsonObject.optInt("clouds"))
                .setUvIndex(jsonObject.optDouble("uvi", 0.0).toInt())
                .setWindSpeed(jsonObject.optDouble("wind_speed", 0.0).toFloat())
                .setWindDirection(jsonObject.optInt("wind_deg"))
                .setWindGustSpeed(jsonObject.optDouble("wind_gust", 0.0).toFloat())
                .setPop(jsonObject.optDouble("pop", 0.0).toFloat())
                .setRain(jsonObject.optJSONObject("rain")?.optDouble("1h", 0.0)?.toFloat() ?: 0f)
                .setSnow(jsonObject.optJSONObject("snow")?.optDouble("1h", 0.0)?.toFloat() ?: 0f)
                .build()
        }
    }
    override fun fromOWMToProto(jsonObject: JSONObject): HourlyForecast {
        return HourlyForecastMapper.fromOWMToProto(jsonObject);
    }
}
