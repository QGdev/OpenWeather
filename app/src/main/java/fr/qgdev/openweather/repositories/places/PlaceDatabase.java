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

import androidx.annotation.NonNull;
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

@Database(version = 4,
        entities = {Geolocation.class,
                Properties.class,
                AirQuality.class,
                CurrentWeather.class,
                DailyWeatherForecast.class,
                HourlyWeatherForecast.class,
                MinutelyWeatherForecast.class,
                WeatherAlert.class}, exportSchema = false)
@TypeConverters({StringListTypeConverter.class})

@Dao
public abstract class PlaceDatabase extends RoomDatabase {
    
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final AtomicReference<PlaceDatabase> instance = new AtomicReference<>(null);
    
    
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
    
    
    public static PlaceDatabase getDatabase(final Context context) {
        instance.compareAndSet(null,
                Room.databaseBuilder(context.getApplicationContext(),
                                PlaceDatabase.class, "appDB")
                        .addMigrations(migration1_2, migration2_3, migration3_4)
                        .build());
        
        return instance.get();
    }
    
    public abstract PlaceDAO placeDAO();
    
    public abstract PropertiesDAO propertiesDAO();
    
    public abstract GeolocationDAO geolocationDAO();
    
    public abstract AirQualityDAO airQualityDAO();
    
    public abstract CurrentWeatherDAO currentWeatherDAO();
    
    public abstract MinutelyWeatherForecastDAO minutelyWeatherForecastDAO();
    
    public abstract HourlyWeatherForecastDAO hourlyWeatherForecastDAO();
    
    public abstract DailyWeatherForecastDAO dailyWeatherForecastDAO();
    
    public abstract WeatherAlertDAO weatherAlertDAO();
    
    public LiveData<List<Place>> getAllPlacesLiveData() {
        MutableLiveData<List<Place>> mutableLiveData = new MutableLiveData<>(null);
        Runnable runnable = () -> {
            List<Properties> properties = propertiesDAO().getProperties();
            databaseWriteExecutor.execute(() -> runInTransaction(() -> {
                List<Place> placeList = new ArrayList<>();
                Place place;
                for (Properties p : properties) {
                    int id = p.getPlaceId();
                    place = new Place(geolocationDAO().getFromPlaceID(id),
                            p,
                            currentWeatherDAO().getFromPlaceID(id),
                            airQualityDAO().getFromPlaceID(id),
                            minutelyWeatherForecastDAO().getFromPlaceID(id),
                            hourlyWeatherForecastDAO().getFromPlaceID(id),
                            dailyWeatherForecastDAO().getFromPlaceID(id),
                            weatherAlertDAO().getFromPlaceID(id));
                    placeList.add(place);
                }
                mutableLiveData.postValue(placeList);
            }));
        };
        new Thread(runnable).start();
        return mutableLiveData;
    }
    
    public List<Place> getAllPlaces() {
    
        List<Properties> properties = propertiesDAO().getProperties();
        List<Place> placeList = new ArrayList<>();
    
        if (!properties.isEmpty()) {
            for (Properties p : properties) {
                int id = p.getPlaceId();
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
    
    public LiveData<Place> getPlaceFromOrderLiveData(int order) {
        MutableLiveData<Place> mutableLiveData = new MutableLiveData<>(null);
        Runnable runnable = () -> {
            int id = propertiesDAO().getIDFromPlaceOrder(order);
            Place place = new Place(geolocationDAO().getFromPlaceID(id),
                    propertiesDAO().getFromPlaceId(id),
                    currentWeatherDAO().getFromPlaceID(id),
                    airQualityDAO().getFromPlaceID(id),
                    minutelyWeatherForecastDAO().getFromPlaceID(id),
                    hourlyWeatherForecastDAO().getFromPlaceID(id),
                    dailyWeatherForecastDAO().getFromPlaceID(id),
                    weatherAlertDAO().getFromPlaceID(id));
            mutableLiveData.postValue(place);
        };
        new Thread(runnable).start();
        return mutableLiveData;
    }
    
    public LiveData<Place> getPlaceFromPlaceIdLiveData(int id) {
        MutableLiveData<Place> mutableLiveData = new MutableLiveData<>(null);
        Runnable runnable = () -> {
            Place place = new Place(geolocationDAO().getFromPlaceID(id),
                    propertiesDAO().getFromPlaceId(id),
                    currentWeatherDAO().getFromPlaceID(id),
                    airQualityDAO().getFromPlaceID(id),
                    minutelyWeatherForecastDAO().getFromPlaceID(id),
                    hourlyWeatherForecastDAO().getFromPlaceID(id),
                    dailyWeatherForecastDAO().getFromPlaceID(id),
                    weatherAlertDAO().getFromPlaceID(id));
            mutableLiveData.postValue(place);
        };
        new Thread(runnable).start();
        return mutableLiveData;
    }
    
    
    private void insertMinutelyWeatherForecasts(List<MinutelyWeatherForecast> minutelyWeatherForecasts) {
        for (MinutelyWeatherForecast minutelyWeatherForecast : minutelyWeatherForecasts) {
            minutelyWeatherForecastDAO().insert(minutelyWeatherForecast);
        }
    }
    
    private void insertHourlyWeatherForecasts(List<HourlyWeatherForecast> hourlyWeatherForecasts) {
        for (HourlyWeatherForecast hourlyWeatherForecast : hourlyWeatherForecasts) {
            hourlyWeatherForecastDAO().insert(hourlyWeatherForecast);
        }
    }
    
    private void insertDailyWeatherForecasts(List<DailyWeatherForecast> dailyWeatherForecasts) {
        for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecasts) {
            dailyWeatherForecastDAO().insert(dailyWeatherForecast);
        }
    }
    
    private void insertWeatherAlerts(List<WeatherAlert> weatherAlerts) {
        for (WeatherAlert weatherAlert : weatherAlerts) {
            weatherAlertDAO().insert(weatherAlert);
        }
    }
    
    public void insertPlace(Place place, ParameterizedRunnable<Place> callback) {
        databaseWriteExecutor.execute(() -> runInTransaction(() -> {
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
    
    public void updatePlace(Place place, ParameterizedRunnable<Place> callback) {
        databaseWriteExecutor.execute(() -> runInTransaction(() -> {
            propertiesDAO().update(place.getProperties());
            geolocationDAO().update(place.getGeolocation());
            currentWeatherDAO().update(place.getCurrentWeather());
            airQualityDAO().update(place.getAirQuality());
            
            int placeID = place.getProperties().getPlaceId();
            
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
    
    public synchronized void deletePlace(int order, ParameterizedRunnable<Integer> callback) {
        databaseWriteExecutor.execute(() -> {
            int placeID = propertiesDAO().getIDFromPlaceOrder(order);
            
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
        });
    }
    
    public void deletePlace(Place place, ParameterizedRunnable<Integer> callback) {
        deletePlace(place.getProperties().getOrder(), callback);
    }
    
    public synchronized void movePlace(int crtOrder, int newOrder) {
        databaseWriteExecutor.submit(() -> runInTransaction(() -> {
            int nbOfPlaces = placeDAO().getPlacesCount();
            if (crtOrder >= nbOfPlaces)
                throw new IllegalArgumentException("Given current order number doesn't exist");
            if (newOrder >= nbOfPlaces)
                throw new IllegalArgumentException("Given new order number doesn't exist");
            
            int placeID = propertiesDAO().getIDFromPlaceOrder(crtOrder);
            
            //  Nothing to be done in this case
            if (crtOrder == newOrder) return;
            
            if (crtOrder < newOrder) propertiesDAO().updatePlaceOrdersMvEnd(crtOrder, newOrder);
            else propertiesDAO().updatePlaceOrdersMvBgn(crtOrder, newOrder);
            
            propertiesDAO().updateOrderFromPlaceID(placeID, newOrder);
        }));
    }
    
    public void swapPlaces(int placeOrderA, int placeOrderB) {
        runInTransaction(() -> {
            int placeAId = propertiesDAO().getIDFromPlaceOrder(placeOrderA);
            int placeBId = propertiesDAO().getIDFromPlaceOrder(placeOrderB);
            
            propertiesDAO().updateOrderFromPlaceID(placeAId, placeOrderB);
            propertiesDAO().updateOrderFromPlaceID(placeBId, placeOrderA);
        });
    }
}
