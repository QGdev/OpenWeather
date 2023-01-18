package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.metrics.HourlyWeatherForecast;

@Dao
public interface HourlyWeatherForecastDAO {
	
	@Query("SELECT * FROM hourly_weather_forecasts WHERE placeId = :id ORDER BY dt ASC")
	List<HourlyWeatherForecast> getFromPlaceID(int id);
	
	@Query("DELETE FROM hourly_weather_forecasts WHERE placeId = :id")
	void deleteFromPlaceID(int id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(HourlyWeatherForecast hourlyWeatherForecast);
	
	@Delete
	void delete(HourlyWeatherForecast hourlyWeatherForecast);
	
	@Update()
	void update(HourlyWeatherForecast hourlyWeatherForecast);
}
