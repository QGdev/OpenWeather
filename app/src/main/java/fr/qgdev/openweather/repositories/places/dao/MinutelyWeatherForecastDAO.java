package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.metrics.MinutelyWeatherForecast;

@Dao
public interface MinutelyWeatherForecastDAO {
	
	@Query("SELECT * FROM minutely_weather_forecasts WHERE placeId = :id ORDER BY dt ASC")
	List<MinutelyWeatherForecast> getFromPlaceID(int id);
	
	@Query("DELETE FROM minutely_weather_forecasts WHERE placeId = :id")
	void deleteFromPlaceID(int id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(MinutelyWeatherForecast minutelyWeatherForecast);
	
	@Delete
	void delete(MinutelyWeatherForecast minutelyWeatherForecast);
	
	@Update()
	void update(MinutelyWeatherForecast minutelyWeatherForecast);
}
