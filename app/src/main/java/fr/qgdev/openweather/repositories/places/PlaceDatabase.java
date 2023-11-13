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

package fr.qgdev.openweather.repositories.places;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import fr.qgdev.openweather.metrics.AirQuality;
import fr.qgdev.openweather.metrics.CurrentWeather;
import fr.qgdev.openweather.metrics.DailyWeatherForecast;
import fr.qgdev.openweather.metrics.HourlyWeatherForecast;
import fr.qgdev.openweather.metrics.MinutelyWeatherForecast;
import fr.qgdev.openweather.metrics.WeatherAlert;
import fr.qgdev.openweather.repositories.places.dao.AirQualityDAO;
import fr.qgdev.openweather.repositories.places.dao.CurrentWeatherDAO;
import fr.qgdev.openweather.repositories.places.dao.DailyWeatherForecastDAO;
import fr.qgdev.openweather.repositories.places.dao.GeolocationDAO;
import fr.qgdev.openweather.repositories.places.dao.HourlyWeatherForecastDAO;
import fr.qgdev.openweather.repositories.places.dao.MinutelyWeatherForecastDAO;
import fr.qgdev.openweather.repositories.places.dao.PlaceDAO;
import fr.qgdev.openweather.repositories.places.dao.PropertiesDAO;
import fr.qgdev.openweather.repositories.places.dao.WeatherAlertDAO;
import fr.qgdev.openweather.utils.ParameterizedRunnable;

@Database(version = 5,
		  entities = {Geolocation.class,
					 Properties.class,
					 AirQuality.class,
					 CurrentWeather.class,
					 DailyWeatherForecast.class,
					 HourlyWeatherForecast.class,
					 MinutelyWeatherForecast.class,
					 WeatherAlert.class}, exportSchema = false)
@TypeConverters({StringListTypeConverter.class})

/**
 * PlaceDatabase is the main class of the database.
 * Used to create the database and to get the DAOs.
 * Stores all the registered places by the user.
 */
@Dao
public abstract class PlaceDatabase extends RoomDatabase {
	
	private static final int NUMBER_OF_THREADS = 4;
	public static final ExecutorService databaseExecutor =
			  Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	private static final AtomicReference<PlaceDatabase> instance = new AtomicReference<>(null);
	
	private static final MutableLiveData<List<Place>> allPlacesMutableLiveData = new MutableLiveData<>(null);
	
	
	/**
	 * Piece of code used to migrate from version 1 to version 2
	 * - Renaming start_dt and end_dt to startDt and endDt in weather_alerts table
	 */
	private static final Migration migration1_2 = new Migration(1, 2) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE weather_alerts RENAME COLUMN start_dt TO startDt");
			database.execSQL("ALTER TABLE weather_alerts RENAME COLUMN end_dt TO endDt");
		}
	};
	
	/**
	 * Piece of code used to migrate from version 2 to version 3
	 * - Renaming sunrise and sunset to sunriseDt and sunsetDt in current_weather table
	 * - Renaming sunrise and sunset to sunriseDt and sunsetDt in daily_weather_forecast table
	 * - Renaming moonrise and moonset to moonriseDt and moonsetDt in daily_weather_forecast table
	 */
	private static final Migration migration2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE current_weather RENAME COLUMN sunrise TO sunriseDt");
			database.execSQL("ALTER TABLE current_weather RENAME COLUMN sunset TO sunsetDt");
			
			database.execSQL("ALTER TABLE daily_weather_forecast RENAME COLUMN sunrise TO sunriseDt");
			database.execSQL("ALTER TABLE daily_weather_forecast RENAME COLUMN sunset TO sunsetDt");
			database.execSQL("ALTER TABLE daily_weather_forecast RENAME COLUMN moonrise TO moonriseDt");
			database.execSQL("ALTER TABLE daily_weather_forecast RENAME COLUMN moonset TO moonsetDt");
		}
	};
	
	/**
	 * Piece of code used to migrate from version 3 to version 4
	 * - Modifying weather_alerts table to pass existing column "sender" as a PRIMARY KEY
	 */
	private static final Migration migration3_4 = new Migration(3, 4) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			// Rename old table
			database.execSQL("ALTER TABLE weather_alerts RENAME TO weather_alerts_old");
			// Create new table
			database.execSQL("CREATE TABLE IF NOT EXISTS `weather_alerts` (`sender` TEXT NOT NULL, `event` TEXT NOT NULL, `startDt` INTEGER NOT NULL, `endDt` INTEGER NOT NULL, `description` TEXT, `tags` TEXT, `placeId` INTEGER NOT NULL, PRIMARY KEY(`placeId`, `sender`, `startDt`, `event`))");
			// Copy the data
			database.execSQL("INSERT INTO weather_alerts (sender, event, startDt, endDt, description, tags, placeId) SELECT sender, event, startDt, endDt, description, tags, placeId FROM weather_alerts_old");
			// Remove old table
			database.execSQL("DROP TABLE weather_alerts_old");
		}
	};
	
	/**
	 * Piece of code used to migrate from version 4 to version 5
	 * - Modifying placeId from INTEGER to TEXT in all tables
	 * - Rename daily_weather_forecast table to daily_weather_forecasts
	 */
	private static final Migration migration4_5 = new Migration(4, 5) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			// Rename old table
			database.execSQL("ALTER TABLE geolocation RENAME TO geolocation_old");
			database.execSQL("ALTER TABLE properties RENAME TO properties_old");
			database.execSQL("ALTER TABLE air_quality RENAME TO air_quality_old");
			database.execSQL("ALTER TABLE current_weather RENAME TO current_weather_old");
			database.execSQL("ALTER TABLE daily_weather_forecast RENAME TO daily_weather_forecast_old");
			database.execSQL("ALTER TABLE hourly_weather_forecasts RENAME TO hourly_weather_forecast_old");
			database.execSQL("ALTER TABLE minutely_weather_forecasts RENAME TO minutely_weather_forecast_old");
			database.execSQL("ALTER TABLE weather_alerts RENAME TO weather_alerts_old");
			
			// Create new table
			database.execSQL("CREATE TABLE IF NOT EXISTS `geolocation` (`placeId` TEXT NOT NULL, `city` TEXT, `countryCode` TEXT, `latitude` REAL, `longitude` REAL, PRIMARY KEY(`placeId`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `properties` (`creationTime` INTEGER NOT NULL, `timeOffset` INTEGER NOT NULL, `placeId` TEXT NOT NULL, `lastSuccessfulWeatherUpdateTime` INTEGER NOT NULL, `lastSuccessfulAirQualityUpdateTime` INTEGER NOT NULL, `lastWeatherUpdateAttemptTime` INTEGER NOT NULL, `lastAirQualityUpdateAttemptTime` INTEGER NOT NULL, `lastAvailableWeatherDataTime` INTEGER NOT NULL, `lastAvailableAirQualityDataTime` INTEGER NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`placeId`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `air_quality` (`placeId` TEXT NOT NULL, `aqi` INTEGER NOT NULL, `co` REAL NOT NULL, `no` REAL NOT NULL, `no2` REAL NOT NULL, `o3` REAL NOT NULL, `so2` REAL NOT NULL, `pm2_5` REAL NOT NULL, `pm10` REAL NOT NULL, `nh3` REAL NOT NULL, PRIMARY KEY(`placeId`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `current_weather` (`placeId` TEXT NOT NULL, `dt` INTEGER NOT NULL, `weather` TEXT, `weatherDescription` TEXT, `weatherCode` INTEGER NOT NULL, `temperature` REAL NOT NULL, `temperatureFeelsLike` REAL NOT NULL, `pressure` INTEGER NOT NULL, `humidity` INTEGER NOT NULL, `dewPoint` REAL NOT NULL, `cloudiness` INTEGER NOT NULL, `uvIndex` INTEGER NOT NULL, `visibility` INTEGER NOT NULL, `sunriseDt` INTEGER NOT NULL, `sunsetDt` INTEGER NOT NULL, `windSpeed` REAL NOT NULL, `windGustSpeed` REAL NOT NULL, `isWindDirectionReadable` INTEGER NOT NULL, `windDirection` INTEGER NOT NULL, `rain` REAL NOT NULL, `snow` REAL NOT NULL, PRIMARY KEY(`placeId`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `daily_weather_forecasts` (`placeId` TEXT NOT NULL, `dt` INTEGER NOT NULL, `weather` TEXT, `weatherDescription` TEXT, `weatherCode` INTEGER NOT NULL, `temperatureMorning` REAL NOT NULL, `temperatureDay` REAL NOT NULL, `temperatureEvening` REAL NOT NULL, `temperatureNight` REAL NOT NULL, `temperatureMinimum` REAL NOT NULL, `temperatureMaximum` REAL NOT NULL, `temperatureMorningFeelsLike` REAL NOT NULL, `temperatureDayFeelsLike` REAL NOT NULL, `temperatureEveningFeelsLike` REAL NOT NULL, `temperatureNightFeelsLike` REAL NOT NULL, `pressure` INTEGER NOT NULL, `humidity` INTEGER NOT NULL, `dewPoint` REAL NOT NULL, `cloudiness` INTEGER NOT NULL, `sunriseDt` INTEGER NOT NULL, `sunsetDt` INTEGER NOT NULL, `uvIndex` INTEGER NOT NULL, `moonriseDt` INTEGER NOT NULL, `moonsetDt` INTEGER NOT NULL, `moonPhase` REAL NOT NULL, `windSpeed` REAL NOT NULL, `windGustSpeed` REAL NOT NULL, `windDirection` INTEGER NOT NULL, `pop` REAL NOT NULL, `rain` REAL NOT NULL, `snow` REAL NOT NULL, PRIMARY KEY(`placeId`, `dt`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `hourly_weather_forecasts` (`placeId` TEXT NOT NULL, `dt` INTEGER NOT NULL, `weather` TEXT, `weatherDescription` TEXT, `weatherCode` INTEGER NOT NULL, `temperature` REAL NOT NULL, `temperatureFeelsLike` REAL NOT NULL, `pressure` INTEGER NOT NULL, `humidity` INTEGER NOT NULL, `dewPoint` REAL NOT NULL, `cloudiness` INTEGER NOT NULL, `visibility` INTEGER NOT NULL, `uvIndex` INTEGER NOT NULL, `windSpeed` REAL NOT NULL, `windGustSpeed` REAL NOT NULL, `windDirection` INTEGER NOT NULL, `pop` REAL NOT NULL, `rain` REAL NOT NULL, `snow` REAL NOT NULL, PRIMARY KEY(`placeId`, `dt`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `minutely_weather_forecasts` (`placeId` TEXT NOT NULL, `dt` INTEGER NOT NULL, `precipitation` REAL NOT NULL, PRIMARY KEY(`placeId`, `dt`))");
			database.execSQL("CREATE TABLE IF NOT EXISTS `weather_alerts` (`sender` TEXT NOT NULL, `event` TEXT NOT NULL, `startDt` INTEGER NOT NULL, `endDt` INTEGER NOT NULL, `description` TEXT, `tags` TEXT, `placeId` TEXT NOT NULL, PRIMARY KEY(`placeId`, `sender`, `startDt`, `event`))");
			
			// Create a temporary table that will act as a translation table between old and new placeId
			database.execSQL("CREATE TABLE IF NOT EXISTS `placeId_translation` (`old_placeId` INTEGER NOT NULL, `new_placeId` TEXT NOT NULL, PRIMARY KEY(`old_placeId`))");
			
			// Need to fetch all placeId from old tables
			Cursor cursor = database.query("SELECT placeId FROM geolocation_old");
			
			// Fill the list with all olds placeId
			List<Integer> oldPlaceIds = new ArrayList<>();
			
			if (cursor.moveToFirst()) {
				do {
					oldPlaceIds.add(cursor.getInt(0));
				} while (cursor.moveToNext());
			}
			
			// Now, for each old placeId, we create a new placeId
			List<String> newPlaceIds = new ArrayList<>();
			for (Integer i : oldPlaceIds) {
				String newPlaceId = UUID.randomUUID().toString();
				//	Verify that the new placeId is not already used
				while (newPlaceIds.contains(newPlaceId)) {
					newPlaceId = UUID.randomUUID().toString();
				}
				
				newPlaceIds.add(newPlaceId);
			}
			
			// Now, we can fill the translation table
			for (int i = 0; i < oldPlaceIds.size(); i++) {
				database.execSQL("INSERT INTO placeId_translation (old_placeId, new_placeId) VALUES (" + oldPlaceIds.get(i) + ", '" + newPlaceIds.get(i) + "')");
			}
			
			// Now, we can fill the new tables with the data from the old tables
			// We need to use the translation table to replace the old placeId with the new one
			database.execSQL("INSERT INTO geolocation (placeId, city, countryCode, latitude, longitude) SELECT new_placeId, city, countryCode, latitude, longitude FROM geolocation_old INNER JOIN placeId_translation ON geolocation_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO properties (creationTime, timeOffset, placeId, lastSuccessfulWeatherUpdateTime, lastSuccessfulAirQualityUpdateTime, lastWeatherUpdateAttemptTime, lastAirQualityUpdateAttemptTime, lastAvailableWeatherDataTime, lastAvailableAirQualityDataTime, `order`) SELECT creationTime, timeOffset, new_placeId, lastSuccessfulWeatherUpdateTime, lastSuccessfulAirQualityUpdateTime, lastWeatherUpdateAttemptTime, lastAirQualityUpdateAttemptTime, lastAvailableWeatherDataTime, lastAvailableAirQualityDataTime, `order` FROM properties_old INNER JOIN placeId_translation ON properties_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO air_quality (placeId, aqi, co, `no`, no2, o3, so2, pm2_5, pm10, nh3) SELECT new_placeId, aqi, co, `no`, no2, o3, so2, pm2_5, pm10, nh3 FROM air_quality_old INNER JOIN placeId_translation ON air_quality_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO current_weather (placeId, dt, weather, weatherDescription, weatherCode, temperature, temperatureFeelsLike, pressure, humidity, dewPoint, cloudiness, uvIndex, visibility, sunriseDt, sunsetDt, windSpeed, windGustSpeed, isWindDirectionReadable, windDirection, rain, snow) SELECT new_placeId, dt, weather, weatherDescription, weatherCode, temperature, temperatureFeelsLike, pressure, humidity, dewPoint, cloudiness, uvIndex, visibility, sunriseDt, sunsetDt, windSpeed, windGustSpeed, isWindDirectionReadable, windDirection, rain, snow FROM current_weather_old INNER JOIN placeId_translation ON current_weather_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO daily_weather_forecasts (placeId, dt, weather, weatherDescription, weatherCode, temperatureMorning, temperatureDay, temperatureEvening, temperatureNight, temperatureMinimum, temperatureMaximum, temperatureMorningFeelsLike, temperatureDayFeelsLike, temperatureEveningFeelsLike, temperatureNightFeelsLike, pressure, humidity, dewPoint, cloudiness, sunriseDt, sunsetDt, uvIndex, moonriseDt, moonsetDt, moonPhase, windSpeed, windGustSpeed, windDirection, pop, rain, snow) SELECT new_placeId, dt, weather, weatherDescription, weatherCode, temperatureMorning, temperatureDay, temperatureEvening, temperatureNight, temperatureMinimum, temperatureMaximum, temperatureMorningFeelsLike, temperatureDayFeelsLike, temperatureEveningFeelsLike, temperatureNightFeelsLike, pressure, humidity, dewPoint, cloudiness, sunriseDt, sunsetDt, uvIndex, moonriseDt, moonsetDt, moonPhase, windSpeed, windGustSpeed, windDirection, pop, rain, snow FROM daily_weather_forecast_old INNER JOIN placeId_translation ON daily_weather_forecast_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO hourly_weather_forecasts (placeId, dt, weather, weatherDescription, weatherCode, temperature, temperatureFeelsLike, pressure, humidity, dewPoint, cloudiness, visibility, uvIndex, windSpeed, windGustSpeed, windDirection, pop, rain, snow) SELECT new_placeId, dt, weather, weatherDescription, weatherCode, temperature, temperatureFeelsLike, pressure, humidity, dewPoint, cloudiness, visibility, uvIndex, windSpeed, windGustSpeed, windDirection, pop, rain, snow FROM hourly_weather_forecast_old INNER JOIN placeId_translation ON hourly_weather_forecast_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO minutely_weather_forecasts (placeId, dt, precipitation) SELECT new_placeId, dt, precipitation FROM minutely_weather_forecast_old INNER JOIN placeId_translation ON minutely_weather_forecast_old.placeId = placeId_translation.old_placeId");
			database.execSQL("INSERT INTO weather_alerts (sender, event, startDt, endDt, description, tags, placeId) SELECT sender, event, startDt, endDt, description, tags, new_placeId FROM weather_alerts_old INNER JOIN placeId_translation ON weather_alerts_old.placeId = placeId_translation.old_placeId");
			
			// So, now we can remove the translation table
			database.execSQL("DROP TABLE placeId_translation");
			
			// And finally, we can remove the old tables
			database.execSQL("DROP TABLE geolocation_old");
			database.execSQL("DROP TABLE properties_old");
			database.execSQL("DROP TABLE air_quality_old");
			database.execSQL("DROP TABLE current_weather_old");
			database.execSQL("DROP TABLE daily_weather_forecast_old");
			database.execSQL("DROP TABLE hourly_weather_forecast_old");
			database.execSQL("DROP TABLE minutely_weather_forecast_old");
			database.execSQL("DROP TABLE weather_alerts_old");
		}
	};
	
	/**
	 * Get the database instance
	 *
	 * @param context the context
	 * @return the database instance
	 */
	public static PlaceDatabase getDatabase(final Context context) {
		instance.compareAndSet(null,
				  Room.databaseBuilder(context.getApplicationContext(),
										PlaceDatabase.class, "appDB")
							 .addMigrations(migration1_2, migration2_3, migration3_4, migration4_5)
							 .build());
		
		return instance.get();
	}
	
	/**
	 * Gets the PlaceDAO
	 *
	 * @return the PlaceDAO
	 */
	public abstract PlaceDAO placeDAO();
	
	/**
	 * Gets the PropertiesDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the PropertiesDAO
	 */
	public abstract PropertiesDAO propertiesDAO();
	
	/**
	 * Gets the GeolocationDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the GeolocationDAO
	 */
	public abstract GeolocationDAO geolocationDAO();
	
	/**
	 * Gets the AirQualityDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the AirQualityDAO
	 */
	public abstract AirQualityDAO airQualityDAO();
	
	/**
	 * Gets the CurrentWeatherDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the CurrentWeatherDAO
	 */
	public abstract CurrentWeatherDAO currentWeatherDAO();
	
	/**
	 * Gets the MinutelyWeatherForecastDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the MinutelyWeatherForecastDAO
	 */
	public abstract MinutelyWeatherForecastDAO minutelyWeatherForecastDAO();
	
	/**
	 * Gets the HourlyWeatherForecastDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the HourlyWeatherForecastDAO
	 */
	public abstract HourlyWeatherForecastDAO hourlyWeatherForecastDAO();
	
	/**
	 * Gets the DailyWeatherForecastDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the DailyWeatherForecastDAO
	 */
	public abstract DailyWeatherForecastDAO dailyWeatherForecastDAO();
	
	/**
	 * Gets the WeatherAlertDAO
	 * Will be automatically generated by ROOM
	 *
	 * @return the WeatherAlertDAO
	 */
	public abstract WeatherAlertDAO weatherAlertDAO();
	
	/**
	 * Used to get the list of all places but not as a LiveData.
	 * This method is synchronous and should not be used in the main thread
	 *
	 * @return the list of all places
	 */
	public List<Place> getAllPlaces() {
		List<Properties> properties = propertiesDAO().getProperties();
		List<Place> placeList = new ArrayList<>();
		
		if (!properties.isEmpty()) {
			for (Properties p : properties) {
				String id = p.getPlaceId();
				Place place = new Place(geolocationDAO().getFromPlaceID(id),
						  p,
						  currentWeatherDAO().getFromPlaceID(id),
						  airQualityDAO().getFromPlaceID(id),
						  minutelyWeatherForecastDAO().getFromPlaceID(id),
						  hourlyWeatherForecastDAO().getFromPlaceID(id),
						  dailyWeatherForecastDAO().getFromPlaceID(id),
						  weatherAlertDAO().getFromPlaceID(id));
				placeList.add(place);
			}
		}
		return placeList;
	}
	
	/**
	 * Used to get a LiveData that will be updated when the database is updated
	 *
	 * @return the LiveData that will contains the list of all places
	 */
	public LiveData<List<Place>> getAllPlacesLiveData() {
		databaseExecutor.execute(() -> allPlacesMutableLiveData.postValue(getAllPlaces()));
		return allPlacesMutableLiveData;
	}
	
	/**
	 * Used to get a LiveData of a place with a specific order
	 *
	 * @return the LiveData containing the place
	 * @apiNote The provided livedata will first be set to null and then to the requested place
	 */
	public LiveData<Place> getPlaceFromOrderLiveData(int order) {
		MutableLiveData<Place> mutableLiveData = new MutableLiveData<>(null);
		databaseExecutor.execute(() -> {
			String id = propertiesDAO().getIDFromPlaceOrder(order);
			Place place = new Place(geolocationDAO().getFromPlaceID(id),
					  propertiesDAO().getFromPlaceId(id),
					  currentWeatherDAO().getFromPlaceID(id),
					  airQualityDAO().getFromPlaceID(id),
					  minutelyWeatherForecastDAO().getFromPlaceID(id),
					  hourlyWeatherForecastDAO().getFromPlaceID(id),
					  dailyWeatherForecastDAO().getFromPlaceID(id),
					  weatherAlertDAO().getFromPlaceID(id));
			mutableLiveData.postValue(place);
		});
		return mutableLiveData;
	}
	
	/**
	 * Used to get a LiveData of a place with a specific placeId
	 *
	 * @return the LiveData containing the requested place
	 * @apiNote The provided livedata will first be set to null and then to the requested place
	 */
	public LiveData<Place> getPlaceFromPlaceIdLiveData(String id) {
		MutableLiveData<Place> mutableLiveData = new MutableLiveData<>(null);
		databaseExecutor.execute(() -> {
			Place place = new Place(geolocationDAO().getFromPlaceID(id),
					  propertiesDAO().getFromPlaceId(id),
					  currentWeatherDAO().getFromPlaceID(id),
					  airQualityDAO().getFromPlaceID(id),
					  minutelyWeatherForecastDAO().getFromPlaceID(id),
					  hourlyWeatherForecastDAO().getFromPlaceID(id),
					  dailyWeatherForecastDAO().getFromPlaceID(id),
					  weatherAlertDAO().getFromPlaceID(id));
			mutableLiveData.postValue(place);
		});
		return mutableLiveData;
	}
	
	/**
	 * Insert minutely weather forecasts in the database
	 *
	 * @param minutelyWeatherForecasts the list of minutely weather forecasts to insert
	 */
	private void insertMinutelyWeatherForecasts(@NonNull List<MinutelyWeatherForecast> minutelyWeatherForecasts) {
		for (MinutelyWeatherForecast minutelyWeatherForecast : minutelyWeatherForecasts) {
			minutelyWeatherForecastDAO().insert(minutelyWeatherForecast);
		}
	}
	
	/**
	 * Insert hourly weather forecasts in the database
	 *
	 * @param hourlyWeatherForecasts the list of hourly weather forecasts to insert
	 */
	private void insertHourlyWeatherForecasts(@NonNull List<HourlyWeatherForecast> hourlyWeatherForecasts) {
		for (HourlyWeatherForecast hourlyWeatherForecast : hourlyWeatherForecasts) {
			hourlyWeatherForecastDAO().insert(hourlyWeatherForecast);
		}
	}
	
	/**
	 * Insert daily weather forecasts in the database
	 *
	 * @param dailyWeatherForecasts the list of daily weather forecasts to insert
	 */
	private void insertDailyWeatherForecasts(@NonNull List<DailyWeatherForecast> dailyWeatherForecasts) {
		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecasts) {
			dailyWeatherForecastDAO().insert(dailyWeatherForecast);
		}
	}
	
	/**
	 * Insert weather alerts in the database
	 *
	 * @param weatherAlerts the list of weather alerts to insert
	 */
	private void insertWeatherAlerts(@NonNull List<WeatherAlert> weatherAlerts) {
		for (WeatherAlert weatherAlert : weatherAlerts) {
			weatherAlertDAO().insert(weatherAlert);
		}
	}
	
	/**
	 * Insert a place in the database
	 *
	 * @param place    the place to insert
	 * @param callback the callback to call when the place is inserted
	 */
	public void insertPlace(@NonNull Place place, @Nullable ParameterizedRunnable<Place> callback) {
		databaseExecutor.execute(() -> runInTransaction(() -> {
			int order = placeDAO().getPlacesCount();
			place.getProperties().setOrder(order);
			
			propertiesDAO().insert(place.getProperties());
			geolocationDAO().insert(place.getGeolocation());
			currentWeatherDAO().insert(place.getCurrentWeather());
			airQualityDAO().insert(place.getAirQuality());
			
			insertMinutelyWeatherForecasts(place.getMinutelyWeatherForecastList());
			insertHourlyWeatherForecasts(place.getHourlyWeatherForecastList());
			insertDailyWeatherForecasts(place.getDailyWeatherForecastList());
			insertWeatherAlerts(place.getWeatherAlertsList());
			
			if (callback != null) callback.run(place);
		}));
	}
	
	/**
	 * Update a place in the database
	 *
	 * @param place    the place to update
	 * @param callback the callback to call when the place is updated
	 */
	public void updatePlace(@NonNull Place place, @Nullable ParameterizedRunnable<Place> callback) {
		databaseExecutor.execute(() -> runInTransaction(() -> {
			propertiesDAO().update(place.getProperties());
			geolocationDAO().update(place.getGeolocation());
			currentWeatherDAO().update(place.getCurrentWeather());
			airQualityDAO().update(place.getAirQuality());
			
			String placeID = place.getProperties().getPlaceId();
			
			minutelyWeatherForecastDAO().deleteFromPlaceID(placeID);
			hourlyWeatherForecastDAO().deleteFromPlaceID(placeID);
			dailyWeatherForecastDAO().deleteFromPlaceID(placeID);
			weatherAlertDAO().deleteFromPlaceID(placeID);
			
			insertMinutelyWeatherForecasts(place.getMinutelyWeatherForecastList());
			insertHourlyWeatherForecasts(place.getHourlyWeatherForecastList());
			insertDailyWeatherForecasts(place.getDailyWeatherForecastList());
			insertWeatherAlerts(place.getWeatherAlertsList());
			
			if (callback != null) callback.run(place);
		}));
	}
	
	/**
	 * Delete a place in the database
	 *
	 * @param order    the order of the place to delete
	 * @param callback the callback to call when the place is deleted
	 */
	public void deletePlace(int order, @Nullable ParameterizedRunnable<Integer> callback) {
		databaseExecutor.execute(() -> runInTransaction(() -> {
			String placeID = propertiesDAO().getIDFromPlaceOrder(order);
			
			propertiesDAO().deleteFromPlaceID(placeID);
			geolocationDAO().deleteFromPlaceID(placeID);
			currentWeatherDAO().deleteFromPlaceID(placeID);
			airQualityDAO().deleteFromPlaceID(placeID);
			minutelyWeatherForecastDAO().deleteFromPlaceID(placeID);
			hourlyWeatherForecastDAO().deleteFromPlaceID(placeID);
			dailyWeatherForecastDAO().deleteFromPlaceID(placeID);
			weatherAlertDAO().deleteFromPlaceID(placeID);
			
			propertiesDAO().updatePlaceOrdersAfterDeletion(order);
			
			if (callback != null) callback.run(order);
		}));
	}
	
	/**
	 * Delete a place in the database
	 *
	 * @param place    the place to delete
	 * @param callback the callback to call when the place is deleted
	 */
	public void deletePlace(@NonNull Place place, @Nullable ParameterizedRunnable<Integer> callback) {
		deletePlace(place.getProperties().getOrder(), callback);
	}
	
	/**
	 * Move a place to a new order
	 *
	 * @param crtOrder the current order of the place
	 * @param newOrder the new order of the place
	 */
	public void movePlace(int crtOrder, int newOrder, @Nullable Runnable callback) {
		//  Nothing to be done in this case
		if (crtOrder == newOrder) return;
		
		databaseExecutor.execute(() -> runInTransaction(() -> {
			int nbOfPlaces = placeDAO().getPlacesCount();
			if (crtOrder >= nbOfPlaces)
				throw new IllegalArgumentException("Given current order number doesn't exist");
			if (newOrder >= nbOfPlaces)
				throw new IllegalArgumentException("Given new order number doesn't exist");
			
			String placeID = propertiesDAO().getIDFromPlaceOrder(crtOrder);
			
			if (crtOrder < newOrder) propertiesDAO().updatePlaceOrdersMvEnd(crtOrder, newOrder);
			else propertiesDAO().updatePlaceOrdersMvBgn(crtOrder, newOrder);
			
			propertiesDAO().updateOrderFromPlaceID(placeID, newOrder);
			
			if (callback != null)
				new Thread(callback).start();
		}));
	}
	
	/**
	 * Swap the places of two orders
	 *
	 * @param placeOrderA the order of the first place
	 * @param placeOrderB the order of the second place
	 */
	public void swapPlaces(int placeOrderA, int placeOrderB) {
		databaseExecutor.execute(() -> runInTransaction(() -> {
			String placeAId = propertiesDAO().getIDFromPlaceOrder(placeOrderA);
			String placeBId = propertiesDAO().getIDFromPlaceOrder(placeOrderB);
			
			propertiesDAO().updateOrderFromPlaceID(placeAId, placeOrderB);
			propertiesDAO().updateOrderFromPlaceID(placeBId, placeOrderA);
		}));
	}
}
