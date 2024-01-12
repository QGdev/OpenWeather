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

import java.util.List;

import fr.qgdev.openweather.metrics.MinutelyWeatherForecast;

/**
 * MinutelyWeatherForecastDAO
 * <p>
 *    DAO for the MinutelyWeatherForecast class.
 *    It's used to access the database.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Dao
public interface MinutelyWeatherForecastDAO {
	
	@Query("SELECT * FROM minutely_weather_forecasts WHERE placeId = :id ORDER BY dt ASC")
	List<MinutelyWeatherForecast> getFromPlaceID(String id);
	
	@Query("DELETE FROM minutely_weather_forecasts WHERE placeId = :id")
	void deleteFromPlaceID(String id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(MinutelyWeatherForecast minutelyWeatherForecast);
	
	@Delete
	void delete(MinutelyWeatherForecast minutelyWeatherForecast);
	
	@Update()
	void update(MinutelyWeatherForecast minutelyWeatherForecast);
}
