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

package fr.qgdev.openweather.data.repositories

import android.content.Context
import fr.qgdev.openweather.data.storage.PlaceDataStore

/**
 * WidgetRepository
 * <p>
 * Holds the settings of every home screen widget and tells which widgets are bound to a place, in
 * order to let the application redraw them when the data of that place is written.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
class WidgetRepository private constructor(context: Context) {
    private val dataStore = PlaceDataStore.getDataStore(context)

    companion object {
        @Volatile
        private var instance: WidgetRepository? = null

        fun getInstance(context: Context): WidgetRepository {
            return instance ?: synchronized(this) {
                instance ?: WidgetRepository(context).also { instance = it }
            }
        }
    }
}