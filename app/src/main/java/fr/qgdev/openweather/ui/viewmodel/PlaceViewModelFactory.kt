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

package fr.qgdev.openweather.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.qgdev.openweather.data.repositories.PlaceRepository

/**
 * PlaceViewModelFactory
 * <p>
 * Builds a PlaceViewModel with the context it needs in order to reach the repositories, the view
 * models of the main flow being instantiated by hand in MainActivity.
 * </p>
 *
 * @param context                  Context given to the built view model
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see PlaceViewModel
 */
class PlaceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            val placeRepository = PlaceRepository.getInstance(context)
            return PlaceViewModel(placeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
