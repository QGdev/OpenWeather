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

import fr.qgdev.openweather.data.models.WeatherAlert
import org.json.JSONArray
import org.json.JSONObject

/**
 * WeatherAlertMapper
 * <p>
 * Maps a weather alert of an OpenWeatherMap response to a WeatherAlert protobuf message.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class WeatherAlertMapper private constructor() : Mapper<WeatherAlert> {
    companion object : WeatherAlertMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): WeatherAlert {
            val tags = jsonObject.optJSONArray("tags") ?: JSONArray()
            return WeatherAlert.newBuilder()
                .setSender(jsonObject.getString("sender_name"))
                .setEvent(jsonObject.getString("event"))
                .setStartDt(jsonObject.optLong("start") * 1000)
                .setEndDt(jsonObject.optLong("end") * 1000)
                .setDescription(jsonObject.getString("description"))
                .addAllTags((0 until tags.length()).map { tags.getString(it) })
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): WeatherAlert {
        return WeatherAlertMapper.fromOWMToProto(jsonObject)
    }
}
