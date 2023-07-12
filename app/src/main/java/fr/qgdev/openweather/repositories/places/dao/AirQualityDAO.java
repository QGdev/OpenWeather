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

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import fr.qgdev.openweather.metrics.AirQuality;

@Dao
public interface AirQualityDAO {
    
    @Query("SELECT * FROM air_quality WHERE placeId = :id")
    AirQuality getFromPlaceID(int id);
    
    @Query("DELETE FROM air_quality WHERE placeId = :id")
    void deleteFromPlaceID(int id);
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(AirQuality airQuality);
    
    @Delete
    void delete(AirQuality airQuality);
    
    @Update()
    void update(AirQuality airQuality);
}
