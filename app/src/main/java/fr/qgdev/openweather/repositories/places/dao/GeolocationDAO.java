package fr.qgdev.openweather.repositories.places.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.repositories.places.Geolocation;

@Dao
public interface GeolocationDAO {
    
    @Query("SELECT * FROM geolocation WHERE placeId = :id")
    Geolocation getFromPlaceID(int id);
    
    @Query("SELECT * FROM geolocation WHERE city LIKE :city AND countryCode = :countryCode")
    LiveData<List<Geolocation>> getSimilarPlace(String city, String countryCode);
    
    @Query("DELETE FROM geolocation WHERE placeId = :id")
    void deleteFromPlaceID(int id);
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Geolocation geolocation);
    
    @Delete
    void delete(Geolocation geolocation);
    
    @Update()
    void update(Geolocation geolocation);
}
