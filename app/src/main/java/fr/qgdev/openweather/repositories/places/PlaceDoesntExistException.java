/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.repositories.places;

/**
 * PlaceDoesntExistException
 * <p>
 * A simple class to identify exception due to a place doesn't exist in storage
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Exception
 */
public class PlaceDoesntExistException extends Exception {
	
	/**
	 * PlaceDoesntExistException Constructor
	 * <p>
	 * Just the constructor of PlaceDoesntExistException class
	 * </p>
	 */
	public PlaceDoesntExistException() {
		super("Cannot save a place data that isn't present in storage");
	}
}