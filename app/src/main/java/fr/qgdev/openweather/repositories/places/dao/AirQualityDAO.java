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
