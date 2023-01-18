package fr.qgdev.openweather.repositories.places;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

@Database(version = 1,
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
    private static volatile PlaceDatabase INSTANCE;
    
    public static PlaceDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PlaceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlaceDatabase.class, "appDB").build();
                }
            }
        }
        return INSTANCE;
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
                    placeList.add(p.getOrder(), place);
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
        databaseWriteExecutor.execute(() -> {
            
            if (properties.size() > 0) {
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
                    placeList.add(p.getOrder(), place);
                }
            }
        });
        return placeList;
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
            
            if (crtOrder < newOrder) propertiesDAO().updatePlaceOrdersMvBgn(crtOrder, newOrder);
            else propertiesDAO().updatePlaceOrdersMvBgn(crtOrder, newOrder);
            
            propertiesDAO().updateOrderFromPlaceID(placeID, newOrder);
        }));
    }
    
    public void swapPlaces(int placeOrderA, int placeOrderB) {
        runInTransaction(() -> {
            int placeA_ID = propertiesDAO().getIDFromPlaceOrder(placeOrderA);
            int placeB_ID = propertiesDAO().getIDFromPlaceOrder(placeOrderB);
            
            propertiesDAO().updateOrderFromPlaceID(placeA_ID, placeOrderB);
            propertiesDAO().updateOrderFromPlaceID(placeB_ID, placeOrderA);
        });
    }
}
