/*
 *  Copyright (c) 2019 - 2024
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

package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import fr.qgdev.openweather.metrics.CurrentWeather;

/**
 * CurrentWeatherDAO
 * <p>
 *    DAO for the CurrentWeather class.
 *    It's used to access the database.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Dao
public interface CurrentWeatherDAO {
	
	@Query("SELECT * FROM current_weather WHERE placeId = :id")
	CurrentWeather getFromPlaceID(String id);
	
	@Query("DELETE FROM current_weather WHERE placeId = :id")
	void deleteFromPlaceID(String id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(CurrentWeather currentWeather);
	
	@Delete
	void delete(CurrentWeather currentWeather);
	
	@Update()
	void update(CurrentWeather currentWeather);
}
