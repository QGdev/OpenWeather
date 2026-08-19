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
import fr.qgdev.openweather.data.models.AirQuality
import org.json.JSONObject

open class AirQualityMapper private constructor() : Mapper<AirQuality> {
    companion object : AirQualityMapper() {
        override fun fromOWMToProto(jsonObject: JSONObject): AirQuality {
            val result = Gson().fromJson(jsonObject.getJSONArray("list").getJSONObject(0).toString(), AirQualityJson::class.java)
            return AirQuality.newBuilder()
                .setAqi(result.main.aqi)
                .setCo(result.components.co.toFloat())
                .setNo(result.components.no.toFloat())
                .setNo2(result.components.no2.toFloat())
                .setO3(result.components.o3.toFloat())
                .setSo2(result.components.so2.toFloat())
                .setPm25(result.components.pm2_5.toFloat())
                .setPm10(result.components.pm10.toFloat())
                .setNh3(result.components.nh3.toFloat())
                .build()
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): AirQuality {
        return AirQualityMapper.fromOWMToProto(jsonObject);
    }

    private data class AirQualityJson(
        val main: MainJson,
        val components: ComponentsJson
    )

    private data class MainJson(
        @SerializedName("aqi") val aqi: Int
    )

    private data class ComponentsJson(
        @SerializedName("co") val co: Double,
        @SerializedName("no") val no: Double,
        @SerializedName("no2") val no2: Double,
        @SerializedName("o3") val o3: Double,
        @SerializedName("so2") val so2: Double,
        @SerializedName("pm2_5") val pm2_5: Double,
        @SerializedName("pm10") val pm10: Double,
        @SerializedName("nh3") val nh3: Double
    )
}