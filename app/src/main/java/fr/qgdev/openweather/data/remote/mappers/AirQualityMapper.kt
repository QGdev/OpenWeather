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

import fr.qgdev.openweather.data.models.AirQuality
import org.json.JSONObject

/**
 * AirQualityMapper
 * <p>
 * Maps the first entry of the air pollution list of an OpenWeatherMap response to a AirQuality protobuf message.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class AirQualityMapper private constructor() : Mapper<AirQuality> {
    companion object : AirQualityMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): AirQuality {
            val entry = jsonObject.getJSONArray("list").getJSONObject(0)
            val components = entry.getJSONObject("components")
            return AirQuality.newBuilder()
                .setAqi(entry.getJSONObject("main").optInt("aqi"))
                .setCo(components.optDouble("co", 0.0).toFloat())
                .setNo(components.optDouble("no", 0.0).toFloat())
                .setNo2(components.optDouble("no2", 0.0).toFloat())
                .setO3(components.optDouble("o3", 0.0).toFloat())
                .setSo2(components.optDouble("so2", 0.0).toFloat())
                .setPm25(components.optDouble("pm2_5", 0.0).toFloat())
                .setPm10(components.optDouble("pm10", 0.0).toFloat())
                .setNh3(components.optDouble("nh3", 0.0).toFloat())
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): AirQuality {
        return AirQualityMapper.fromOWMToProto(jsonObject);
    }
}
