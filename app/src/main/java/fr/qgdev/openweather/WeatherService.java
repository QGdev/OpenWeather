package fr.qgdev.openweather;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;
import fr.qgdev.openweather.weather.MinutelyWeatherForecast;
import fr.qgdev.openweather.weather.WeatherAlert;

public class WeatherService {

    private static final String TAG = WeatherService.class.getSimpleName();

    private static final String WEATHER_SERVICE_TAG = "WEATHER_SERVICE";
    private static String apiKey, language;

    private final DataPlaces dataPlaces;

    private final Context context;
    private final RequestQueue queue;


    public WeatherService(final Context context, @NonNull String apiKey, @NonNull String language, @NonNull DataPlaces dataPlaces) {
        this.context = context;
        this.dataPlaces = dataPlaces;

        queue = Volley.newRequestQueue(context);
        WeatherService.apiKey = apiKey;
        WeatherService.language = language;
    }

    public interface WeatherCallback{
        void onWeatherData(final Place place, DataPlaces dataPlaces);

        void onError(Exception exception, Place place, DataPlaces dataPlaces);

        void onConnectionError(VolleyError noConnectionError, Place place, DataPlaces dataPlaces);
    }


    @WorkerThread
    public void getWeatherDataOWM(Place place, WeatherCallback callback) {

        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_weatherdata), place.getLatitude(), place.getLongitude(), apiKey, language);

        JsonObjectRequest weatherRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        response -> {
                            place.setErrorDuringDataAcquisition(false);
                            place.setErrorCode(200);

                            try {

                                //  Current Weather
                                //________________________________________________________________
                                //

                                JSONObject currentWeatherJSON = response.getJSONObject("current");

                                //  The time of this update
                                place.setLastUpdate(currentWeatherJSON.getLong("dt") * 1000);
                                place.setLastUpdateDate(new Date(place.getLastUpdate()));

                                CurrentWeather currentWeather_tmp = new CurrentWeather();
                                currentWeather_tmp.fillWithOWMData(currentWeatherJSON);
                                place.setCurrentWeather(currentWeather_tmp);


                                //  Minutely Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray minutelyWeatherForecastJSON = response.getJSONArray("minutely");
                                ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList_tmp = new ArrayList<>();
                                MinutelyWeatherForecast minutelyWeatherForecast_tmp = new MinutelyWeatherForecast();

                                for (int i = 0; i < minutelyWeatherForecastJSON.length(); i++) {
                                    minutelyWeatherForecast_tmp.fillWithOWMData(minutelyWeatherForecastJSON.getJSONObject(i));
                                    minutelyWeatherForecastArrayList_tmp.add(i, minutelyWeatherForecast_tmp.clone());
                                }

                                place.setMinutelyWeatherForecastArrayList(minutelyWeatherForecastArrayList_tmp);


                                //  Hourly Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray hourlyForecastWeatherJSON = response.getJSONArray("hourly");

                                HourlyWeatherForecast hourlyWeatherForecast_tmp = new HourlyWeatherForecast();
                                ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList_tmp = new ArrayList<>();

                                for (int i = 0; i < hourlyForecastWeatherJSON.length(); i++) {
                                    hourlyWeatherForecast_tmp.fillWithOWMData(hourlyForecastWeatherJSON.getJSONObject(i));
                                    hourlyWeatherForecastArrayList_tmp.add(i, hourlyWeatherForecast_tmp.clone());
                                }
                                place.setHourlyWeatherForecastArrayList(hourlyWeatherForecastArrayList_tmp);


                                //  Daily Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray dailyWeatherJSON = response.getJSONArray("daily");

                                DailyWeatherForecast dailyWeatherForecast_tmp = new DailyWeatherForecast();
                                ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList_tmp = new ArrayList<>();

                                for (int i = 0; i < dailyWeatherJSON.length(); i++) {
                                    dailyWeatherForecast_tmp.fillWithOWMData(dailyWeatherJSON.getJSONObject(i));
                                    dailyWeatherForecastArrayList_tmp.add(i, dailyWeatherForecast_tmp.clone());
                                }
                                place.setDailyWeatherForecastArrayList(dailyWeatherForecastArrayList_tmp);


                                //  Weather Alert
                                //________________________________________________________________
                                //

                                ArrayList<WeatherAlert> weatherAlertArrayList_tmp = new ArrayList<>();

                                if (response.has("alerts")) {
                                    JSONArray weatherAlertJSON = response.getJSONArray("alerts");
                                    WeatherAlert weatherAlert_tmp = new WeatherAlert();

                                    for (int i = 0; i < weatherAlertJSON.length(); i++) {
                                        weatherAlert_tmp.fillWithOWMData(weatherAlertJSON.getJSONObject(i));
                                        weatherAlertArrayList_tmp.add(i, weatherAlert_tmp);
                                    }
                                }

                                place.setWeatherAlertsArrayList(weatherAlertArrayList_tmp);

                                Log.d(TAG, "Weather information treatment completed");
                                callback.onWeatherData(place, dataPlaces);

                            } catch (Exception e) {
                                Log.w(TAG, "Weather information treatment failed");
                                callback.onError(e, place, dataPlaces);
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            Log.w(TAG, "Weather information request failed from OWM");
                            callback.onConnectionError(error, place, dataPlaces);
                        });

        Log.d(TAG, "Weather information request");
        queue.add(weatherRequest);
    }

    public void cancel(){
        queue.cancelAll(WEATHER_SERVICE_TAG);
    }
}
