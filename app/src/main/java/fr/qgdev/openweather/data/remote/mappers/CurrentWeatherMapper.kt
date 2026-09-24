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

import fr.qgdev.openweather.data.models.CurrentWeather
import org.json.JSONObject

/**
 * CurrentWeatherMapper
 * <p>
 * Maps the current weather of an OpenWeatherMap response to a CurrentWeather protobuf message.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class CurrentWeatherMapper private constructor() : Mapper<CurrentWeather> {
    companion object : CurrentWeatherMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): CurrentWeather {
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
            //  A missing number reads as 0, as it did through Gson; only the wind direction tells
            //  "missing" apart, to flag it unreadable.
            val windDeg = if (jsonObject.isNull("wind_deg")) null else jsonObject.getInt("wind_deg")

            return CurrentWeather.newBuilder()
                .setDt(jsonObject.optLong("dt") * 1000)
                .setWeather(weather.getString("main"))
                .setWeatherDescription(weather.getString("description"))
                .setWeatherCode(weather.optInt("id"))
                .setTemperature(jsonObject.optDouble("temp", 0.0).toFloat())
                .setTemperatureFeelsLike(jsonObject.optDouble("feels_like", 0.0).toFloat())
                .setPressure(jsonObject.optInt("pressure"))
                .setHumidity(jsonObject.optInt("humidity"))
                .setDewPoint(jsonObject.optDouble("dew_point", 0.0).toFloat())
                .setUvIndex(jsonObject.optDouble("uvi", 0.0).toInt())
                .setCloudiness(jsonObject.optInt("clouds"))
                .setVisibility(jsonObject.optInt("visibility"))
                .setSunrise(jsonObject.optLong("sunrise") * 1000)
                .setSunset(jsonObject.optLong("sunset") * 1000)
                .setWindSpeed(jsonObject.optDouble("wind_speed", 0.0).toFloat())
                .setIsWindDirectionReadable(windDeg != null)
                .setWindDirection(windDeg ?: 0)
                .setWindGustSpeed(jsonObject.optDouble("wind_gust", 0.0).toFloat())
                .setRain(jsonObject.optJSONObject("rain")?.optDouble("1h", 0.0)?.toFloat() ?: 0f)
                .setSnow(jsonObject.optJSONObject("snow")?.optDouble("1h", 0.0)?.toFloat() ?: 0f)
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): CurrentWeather {
        return CurrentWeatherMapper.fromOWMToProto(jsonObject)
    }
}
