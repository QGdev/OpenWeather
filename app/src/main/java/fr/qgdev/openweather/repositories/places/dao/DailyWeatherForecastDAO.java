package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.metrics.DailyWeatherForecast;

@Dao
public interface DailyWeatherForecastDAO {
	
	@Query("SELECT * FROM daily_weather_forecast WHERE placeId = :id ORDER BY dt ASC")
	List<DailyWeatherForecast> getFromPlaceID(int id);
	
	@Query("DELETE FROM daily_weather_forecast WHERE placeId = :id")
	void deleteFromPlaceID(int id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(DailyWeatherForecast dailyWeatherForecast);
	
	@Delete
	void delete(DailyWeatherForecast dailyWeatherForecast);
	
	@Update()
	void update(DailyWeatherForecast dailyWeatherForecast);
}
