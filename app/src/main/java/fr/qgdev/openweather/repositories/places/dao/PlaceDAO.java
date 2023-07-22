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

package fr.qgdev.openweather.repositories.places.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import fr.qgdev.openweather.repositories.places.Geolocation;

@Dao
public abstract class PlaceDAO {
	
	@Query("SELECT COUNT(*) FROM properties")
	public abstract LiveData<Integer> getPlacesCountLiveData();
	
	@Query("SELECT COUNT(*) FROM properties")
	public abstract Integer getPlacesCount();
	
	@Query("SELECT * FROM geolocation NATURAL JOIN properties")
	public abstract LiveData<List<Geolocation>> getBasicListing();
}
