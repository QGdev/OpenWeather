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
import fr.qgdev.openweather.data.models.WeatherAlert
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
            val result = Gson().fromJson(jsonObject.toString(), WeatherAlertJson::class.java)
            return WeatherAlert.newBuilder()
                .setSender(result.sender)
                .setEvent(result.event)
                .setStartDt(result.start * 1000)
                .setEndDt(result.end * 1000)
                .setDescription(result.description)
                .addAllTags(result.tags)
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): WeatherAlert {
        return WeatherAlertMapper.fromOWMToProto(jsonObject)
    }

    private data class WeatherAlertJson(
        @SerializedName("sender_name") val sender: String,
        @SerializedName("event") val event: String,
        @SerializedName("start") val start: Long,
        @SerializedName("end") val end: Long,
        @SerializedName("description") val description: String,
        @SerializedName("tags") val tags: List<String>
    )
}

