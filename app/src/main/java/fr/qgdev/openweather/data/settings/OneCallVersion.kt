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

package fr.qgdev.openweather.data.settings

/**
 * The One Call API version the weather is fetched with. Both answer in the same format; what
 * differs is which keys they accept: 2.5 was closed to keys created from June 2024, which need the
 * "One Call by Call" subscription to 3.0.
 *
 * [wireValue] is both the stored value and the version segment of the request URL.
 */
enum class OneCallVersion(override val wireValue: String) : StoredSetting {
    V3_0("3.0"),
    V2_5("2.5");
}
