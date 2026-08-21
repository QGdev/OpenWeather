/*
 *  Copyright (c) 2019 - 2026
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
 * A setting whose stored representation is declared on the constant rather than derived from it.
 *
 * The strings are not free to change. They are the values 0.9.4 wrote to the preferences and the
 * ones declared as `entryValues` in `res/values/array.xml`, so the app has to keep reading and
 * writing exactly those or an upgrade silently resets the user's units.
 *
 * Deriving the string from the constant name is what broke `setPressureSetting` and
 * `setTimeSetting`: they wrote `name.lowercase()` and `name.split("_").first()` while the getters
 * matched the shipped spellings, so three of the four pressure units and the 12-hour clock could
 * never be saved. Carrying the value on the constant makes that mismatch impossible - there is one
 * string per constant and it sits next to it.
 */
interface StoredSetting {
    val wireValue: String
}

/**
 * Resolves a stored string back to its constant, falling back to [default] when the preference is
 * absent or holds a value this version does not recognise.
 */
inline fun <reified T> storedSettingOf(stored: String?, default: T): T
        where T : Enum<T>, T : StoredSetting =
    enumValues<T>().firstOrNull { it.wireValue == stored } ?: default
