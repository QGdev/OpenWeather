package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.metrics.WeatherAlert;

@Dao
public interface WeatherAlertDAO {
	
	@Query("SELECT * FROM weather_alerts WHERE placeId = :id ORDER BY start_dt ASC")
	List<WeatherAlert> getFromPlaceID(int id);
	
	@Query("DELETE FROM weather_alerts WHERE placeId = :id")
	void deleteFromPlaceID(int id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(WeatherAlert weatherAlert);
	
	@Delete
	void delete(WeatherAlert weatherAlert);
	
	@Update()
	void update(WeatherAlert weatherAlert);
}
