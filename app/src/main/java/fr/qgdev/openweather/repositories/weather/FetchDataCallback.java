/*
 *  Copyright (c) 2019 - 2023
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

package fr.qgdev.openweather.repositories.weather;

import fr.qgdev.openweather.repositories.places.Place;

public interface FetchDataCallback {
	
	/**
	 * On success of both requests (weather and air quality)
	 *
	 * @param place The resulting place with new data
	 */
	void onSuccess(Place place);
	
	/**
	 * On success of weather request but failure of air quality request
	 *
	 * @param place         The resulting place with new data
	 * @param requestStatus The error cause of the air quality request
	 */
	void onPartialSuccess(Place place, RequestStatus requestStatus);
	
	/**
	 * On failure of weather request
	 *
	 * @param requestStatus The error cause of the weather request
	 */
	void onError(RequestStatus requestStatus);
}
