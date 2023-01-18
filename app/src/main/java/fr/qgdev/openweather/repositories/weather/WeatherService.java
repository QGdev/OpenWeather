package fr.qgdev.openweather.repositories.weather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Coordinates;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.repositories.settings.SecuredPreferenceDataStore;

public class WeatherService {
    
    private static final String TAG = WeatherService.class.getSimpleName();
    private static String apiKey, language;
    private final Logger logger = Logger.getLogger(TAG);
    private final Context context;
    private final RequestQueue queue;
    
    public WeatherService(final Context context, @NonNull String language, @NonNull final AppRepository appRepository) {
        this.context = context;
        
        this.queue = Volley.newRequestQueue(context);
        
        SecuredPreferenceDataStore securedPreferenceDataStore = appRepository.getSecuredPreferenceDataStore();
        
        WeatherService.apiKey = securedPreferenceDataStore.getString("conf_api_key", null);
        WeatherService.language = language;
        
        securedPreferenceDataStore.getSharedPreferencesChangeLiveData().observeForever(change -> {
            if (change == null) return;
            
            if (change.getKey() == "config_api_key" && change.getValue() instanceof String) {
                WeatherService.apiKey = (String) change.getValue();
            }
        });
    }
    
    
    @WorkerThread
    public void searchAndBuildPlace(int placeID, @NonNull String city, @NonNull String countryCode, @NonNull FetchDataCallback callback) {
        
        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_properties_name),
                city,
                countryCode,
                apiKey,
                language);
        
        searchAndBuildPlace(placeID, url, callback);
    }
    
    @WorkerThread
    public void searchAndBuildPlace(int placeID, @NonNull Coordinates coordinates, @NonNull FetchDataCallback callback) {
        
        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_properties_coordinates),
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                apiKey,
                language);
        
        searchAndBuildPlace(placeID, url, callback);
    }
    
    private void searchAndBuildPlace(int placeID, @NonNull String url, @NonNull FetchDataCallback callback) {
        //  Before launching request, we must have to verify that if the device is connected to a network
        //  The device is connected to an INTERNET capable network
        if (this.deviceIsConnected()) {
            JsonObjectRequest weatherRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null,
                            response -> {
                                try {
                                    Place place = new Place(placeID, -1, response);
                                    getPlaceDataOWM(place, callback);
                                    
                                } catch (JSONException e) {
                                    logger.log(Level.WARNING, e.getMessage());
                                    callback.onError(RequestStatus.UNKNOWN_ERROR);
                                }
                            },
                            error -> {
                                //  no server response (NO INTERNET or SERVER DOWN)
                                if (error.networkResponse == null) {
                                    callback.onError(RequestStatus.NO_ANSWER);
                                }
                                //  Server response
                                else {
                                    switch (error.networkResponse.statusCode) {
                                        case 429:   //  Too many requests
                                            callback.onError(RequestStatus.TOO_MANY_REQUESTS);
                                            break;
                                        case 404:   //  Place not found
                                            callback.onError(RequestStatus.NOT_FOUND);
                                            break;
                                        case 401:   //  Unknown or wrong API key
                                            callback.onError(RequestStatus.AUTH_FAILED);
                                            break;
                                        default:    //  Unknown error
                                            callback.onError(RequestStatus.UNKNOWN_ERROR);
                                            logger.log(Level.WARNING, error.getMessage());
                                            break;
                                    }
                                }
                            });
            
            queue.add(weatherRequest);
        }
        
        //  The device isn't connected to an INTERNET capable network
        else {
            callback.onError(RequestStatus.NOT_CONNECTED);
        }
    }
    
    @WorkerThread
    public void getPlaceDataOWM(Place place, FetchDataCallback callback) {
        
        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_weather_data),
                place.getGeolocation().getCoordinates().getLatitude(),
                place.getGeolocation().getCoordinates().getLongitude(),
                apiKey,
                language);
        
        //  Before launching request, we must have to verify that if the device is connected to a network
        //  The device is connected to an INTERNET capable network
        if (this.deviceIsConnected()) {
            JsonObjectRequest weatherRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null,
                            response -> {
                                try {
                                    place.updateWithOWMWeatherData(response);
                                    getAirQualityDataOWM(place, callback);
                                    
                                } catch (JSONException e) {
                                    logger.log(Level.WARNING, e.getMessage());
                                    callback.onError(RequestStatus.UNKNOWN_ERROR);
                                }
                            },
                            error -> {
                                //  no server response (NO INTERNET or SERVER DOWN)
                                if (error.networkResponse == null) {
                                    callback.onError(RequestStatus.NO_ANSWER);
                                }
                                //  Server response
                                else {
                                    switch (error.networkResponse.statusCode) {
                                        case 429:   //  Too many requests
                                            callback.onError(RequestStatus.TOO_MANY_REQUESTS);
                                            break;
                                        case 404:   //  Place not found
                                            callback.onError(RequestStatus.NOT_FOUND);
                                            break;
                                        case 401:   //  Unknown or wrong API key
                                            callback.onError(RequestStatus.AUTH_FAILED);
                                            break;
                                        default:    //  Unknown error
                                            callback.onError(RequestStatus.UNKNOWN_ERROR);
                                            logger.log(Level.WARNING, error.getMessage());
                                            break;
                                    }
                                }
                            });
            
            queue.add(weatherRequest);
        }
        
        //  The device isn't connected to an INTERNET capable network
        else {
            callback.onError(RequestStatus.NOT_CONNECTED);
        }
    }
    
    
    @WorkerThread
    private void getAirQualityDataOWM(Place place, FetchDataCallback callback) {
        
        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_airquality_data),
                place.getGeolocation().getCoordinates().getLatitude(),
                place.getGeolocation().getCoordinates().getLongitude(),
                apiKey);
        
        //  Before launching request, we must have to verify that if the device is connected to a network
        //  The device is connected to an INTERNET capable network
        if (this.deviceIsConnected()) {
            JsonObjectRequest airQualityRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null,
                            response -> {
                                try {
                                    place.updateWithOWMAirQualityData(response);
                                    callback.onSuccess(place);
                                } catch (JSONException e) {
                                    logger.log(Level.WARNING, e.getMessage());
                                    callback.onPartialSuccess(place, RequestStatus.UNKNOWN_ERROR);
                                }
                            },
                            error -> {
                                //  no server response (NO INTERNET or SERVER DOWN)
                                if (error.networkResponse == null) {
                                    callback.onPartialSuccess(place, RequestStatus.NO_ANSWER);
                                }
                                //  Server response
                                else {
                                    switch (error.networkResponse.statusCode) {
                                        case 429:   //  Too many requests
                                            callback.onPartialSuccess(place, RequestStatus.TOO_MANY_REQUESTS);
                                            break;
                                        case 404:   //  Place not found
                                            callback.onPartialSuccess(place, RequestStatus.NOT_FOUND);
                                            break;
                                        case 401:   //  Unknown or wrong API key
                                            callback.onPartialSuccess(place, RequestStatus.AUTH_FAILED);
                                            break;
                                        default:    //  Unknown error
                                            callback.onPartialSuccess(place, RequestStatus.UNKNOWN_ERROR);
                                            logger.log(Level.WARNING, error.getMessage());
                                            break;
                                    }
                                }
                            });
            queue.add(airQualityRequest);
        }
        //  The device isn't connected to an INTERNET capable network
        else {
            callback.onPartialSuccess(place, RequestStatus.NOT_CONNECTED);
        }
    }
    
    public boolean deviceIsConnected() {
        ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
        Network[] networks = connectivityManager.getAllNetworks();
        NetworkCapabilities networkCapabilities;
        boolean deviceIsConnected = false;
        
        for (Network network : networks) {
            networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) break;
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                deviceIsConnected = true;
                break;
            }
        }
        return deviceIsConnected;
    }
    
    public boolean isApiKeyValid() {
        return apiKey != null && apiKey.length() == 32 && Pattern.compile("[a-zA-Z0-9]").matcher(apiKey).find();
    }
    
    public boolean isApiKeyRegistered() {
        return apiKey != null && !apiKey.equals("");
    }
    
    public void cancel() {
        queue.cancelAll(TAG);
    }
}
