package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import fr.qgdev.openweather.metrics.CurrentWeather;

@Dao
public interface CurrentWeatherDAO {
    
    @Query("SELECT * FROM current_weather WHERE placeId = :id")
    CurrentWeather getFromPlaceID(int id);
    
    @Query("DELETE FROM current_weather WHERE placeId = :id")
    void deleteFromPlaceID(int id);
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(CurrentWeather currentWeather);
    
    @Delete
    void delete(CurrentWeather currentWeather);
    
    @Update()
    void update(CurrentWeather currentWeather);
}
