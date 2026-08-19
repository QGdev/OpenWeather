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

package fr.qgdev.openweather.data.remote

import fr.qgdev.openweather.data.models.Coordinates


/**
 * SearchPlaceCallback
 * <p>
 *     A callback to handle the result of the place search request.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
interface SearchPlaceCallback {

    /**
     * On success of the place search request
     *
     * @param coordinates The coordinates of the found place
     */
    fun onPlaceFound(coordinates: Coordinates?)

    /**
     * On failure of the place search request
     *
     * @param requestStatus The error cause of the place search request
     */
    fun onError(requestStatus: RequestStatus?)
}