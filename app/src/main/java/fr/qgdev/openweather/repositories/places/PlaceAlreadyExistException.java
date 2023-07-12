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

package fr.qgdev.openweather.repositories.places;

/**
 * PlaceAlreadyExistException
 * <p>
 * A simple class to identify exception due to a place already present in storage
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Exception
 */
public class PlaceAlreadyExistException extends Exception {
	
	/**
	 * PlaceAlreadyExistException Constructor
	 * <p>
	 * Just the constructor of PlaceAlreadyExistException class
	 * </p>
	 */
	public PlaceAlreadyExistException() {
		super("Cannot add a place that already present in storage");
	}
}
