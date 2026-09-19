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

import org.json.JSONObject

/**
 * Mapper
 * <p>
 * Contract of every mapper turning an OpenWeatherMap JSON response into a protobuf message.
 * </p>
 *
 * @param O Type of the protobuf message built by the mapper
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
interface Mapper<O> {
    /**
     * Builds a protobuf message from the JSON object of an OpenWeatherMap response.
     *
     * @param jsonObject JSON object to read the values from
     * @return The built protobuf message
     */
    fun fromOWMToProto(jsonObject: JSONObject): O
}