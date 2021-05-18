package fr.qgdev.openweather;

import android.app.Activity;
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


    public WeatherService(final Activity activity, @NonNull String apiKey, @NonNull String language, @NonNull DataPlaces dataPlaces) {
        context = activity.getApplicationContext();
        this.dataPlaces = dataPlaces;

        queue = Volley.newRequestQueue(context);
        WeatherService.apiKey = apiKey;
        WeatherService.language = language;
    }

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

                                CurrentWeather currentWeather_tmp = new CurrentWeather();

                                //  The time of this update
                                place.setLastUpdate(currentWeatherJSON.getLong("dt") * 1000);
                                place.setLastUpdateDate(new Date(place.getLastUpdate()));
                                currentWeather_tmp.dt = currentWeatherJSON.getLong("dt") * 1000;

                                //    Temperatures
                                currentWeather_tmp.temperature = currentWeatherJSON.getDouble("temp");
                                currentWeather_tmp.temperatureFeelsLike = currentWeatherJSON.getDouble("feels_like");

                                //    Pressure, Humidity, dewPoint, uvIndex
                                currentWeather_tmp.pressure = currentWeatherJSON.getInt("pressure");
                                currentWeather_tmp.humidity = currentWeatherJSON.getInt("humidity");
                                currentWeather_tmp.dewPoint = currentWeatherJSON.getDouble("dew_point");

                                if (currentWeatherJSON.has("uvi")) {
                                    currentWeather_tmp.uvIndex = currentWeatherJSON.getInt("uvi");
                                } else {
                                    currentWeather_tmp.uvIndex = 0;
                                }

                                //    Sky informations
                                currentWeather_tmp.cloudiness = currentWeatherJSON.getInt("clouds");
                                currentWeather_tmp.visibility = currentWeatherJSON.getInt("visibility");
                                currentWeather_tmp.sunrise = currentWeatherJSON.getLong("sunrise") * 1000;
                                currentWeather_tmp.sunset = currentWeatherJSON.getLong("sunset") * 1000;

                                //    Wind informations
                                currentWeather_tmp.windSpeed = currentWeatherJSON.getDouble("wind_speed");

                                ////  Enough wind for a viable wind direction information
                                currentWeather_tmp.isWindDirectionReadable = currentWeatherJSON.has("wind_deg");
                                if (currentWeather_tmp.isWindDirectionReadable) {
                                    currentWeather_tmp.windDirection = currentWeatherJSON.getInt("wind_deg");
                                }
                                ////    Wind Gusts
                                if (currentWeatherJSON.has("wind_gust")) {
                                    currentWeather_tmp.windGustSpeed = currentWeatherJSON.getDouble("wind_gust");
                                } else {
                                    currentWeather_tmp.windGustSpeed = 0;
                                }

                                //  Precipitations
                                ////    Rain
                                if (currentWeatherJSON.has("rain") && currentWeatherJSON.getJSONObject("rain").has("1h")) {
                                    currentWeather_tmp.rain = currentWeatherJSON.getJSONObject("rain").getDouble("1h");
                                } else {
                                    currentWeather_tmp.rain = 0;
                                }
                                ////    Snow
                                if (currentWeatherJSON.has("snow") && currentWeatherJSON.getJSONObject("snow").has("1h")) {
                                    currentWeather_tmp.snow = currentWeatherJSON.getJSONObject("snow").getDouble("1h");
                                } else {
                                    currentWeather_tmp.snow = 0;
                                }

                                //    Weather descriptions
                                JSONObject currentWeatherDescriptionsJSON = currentWeatherJSON.getJSONArray("weather").getJSONObject(0);    //  Get only the first station
                                currentWeather_tmp.weather = currentWeatherDescriptionsJSON.getString("main");
                                currentWeather_tmp.weatherDescription = currentWeatherDescriptionsJSON.getString("description");
                                currentWeather_tmp.weatherCode = currentWeatherDescriptionsJSON.getInt("id");

                                place.setCurrentWeather(currentWeather_tmp);


                                //  Minutely Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray minutelyForecastWeatherJSON = response.getJSONArray("minutely");
                                JSONObject minutelyForecastWeatherJSON_tmp;

                                ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList_tmp = new ArrayList<MinutelyWeatherForecast>();

                                for (int i = 0; i < minutelyForecastWeatherJSON.length(); i++) {

                                    minutelyForecastWeatherJSON_tmp = minutelyForecastWeatherJSON.getJSONObject(i);
                                    minutelyWeatherForecastArrayList_tmp.add(i, new MinutelyWeatherForecast(minutelyForecastWeatherJSON_tmp.getLong("dt") * 1000, minutelyForecastWeatherJSON_tmp.getDouble("precipitation")));
                                }

                                place.setMinutelyWeatherForecastArrayList(minutelyWeatherForecastArrayList_tmp);


                                //  Hourly Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray hourlyForecastWeatherJSON = response.getJSONArray("hourly");
                                JSONObject hourlyForecastWeatherJSON_tmp;

                                HourlyWeatherForecast hourlyWeatherForecast_tmp;
                                ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList_tmp = new ArrayList<HourlyWeatherForecast>();

                                for (int i = 0; i < hourlyForecastWeatherJSON.length(); i++) {

                                    hourlyForecastWeatherJSON_tmp = hourlyForecastWeatherJSON.getJSONObject(i);
                                    hourlyWeatherForecast_tmp = new HourlyWeatherForecast();

                                    //  Time
                                    hourlyWeatherForecast_tmp.dt = hourlyForecastWeatherJSON_tmp.getLong("dt") * 1000;

                                    //  Temperatures
                                    hourlyWeatherForecast_tmp.temperature = hourlyForecastWeatherJSON_tmp.getDouble("temp");
                                    hourlyWeatherForecast_tmp.temperatureFeelsLike = hourlyForecastWeatherJSON_tmp.getDouble("feels_like");

                                    //  Pressure, Humidity, Visibility, cloudiness, dewPoint
                                    hourlyWeatherForecast_tmp.pressure = hourlyForecastWeatherJSON_tmp.getInt("pressure");
                                    hourlyWeatherForecast_tmp.humidity = hourlyForecastWeatherJSON_tmp.getInt("humidity");
                                    hourlyWeatherForecast_tmp.visibility = hourlyForecastWeatherJSON_tmp.getInt("visibility");
                                    hourlyWeatherForecast_tmp.cloudiness = hourlyForecastWeatherJSON_tmp.getInt("clouds");
                                    hourlyWeatherForecast_tmp.dewPoint = hourlyForecastWeatherJSON_tmp.getDouble("dew_point");

                                    //  Wind
                                    hourlyWeatherForecast_tmp.windSpeed = hourlyForecastWeatherJSON_tmp.getDouble("wind_speed");
                                    hourlyWeatherForecast_tmp.windDirection = hourlyForecastWeatherJSON_tmp.getInt("wind_deg");
                                    ////    Wind Gusts
                                    if (hourlyForecastWeatherJSON_tmp.has("wind_gust")) {
                                        hourlyWeatherForecast_tmp.windGustSpeed = hourlyForecastWeatherJSON_tmp.getDouble("wind_gust");
                                    } else {
                                        hourlyWeatherForecast_tmp.windGustSpeed = 0;
                                    }

                                    //  Precipitations
                                    ////    Rain
                                    if (hourlyForecastWeatherJSON_tmp.has("rain") && hourlyForecastWeatherJSON_tmp.getJSONObject("rain").has("1h")) {
                                        hourlyWeatherForecast_tmp.rain = hourlyForecastWeatherJSON_tmp.getJSONObject("rain").getDouble("1h");
                                    } else {
                                        hourlyWeatherForecast_tmp.rain = 0;
                                    }
                                    ////    Snow
                                    if (hourlyForecastWeatherJSON_tmp.has("snow") && hourlyForecastWeatherJSON_tmp.getJSONObject("snow").has("1h")) {
                                        hourlyWeatherForecast_tmp.snow = hourlyForecastWeatherJSON_tmp.getJSONObject("snow").getDouble("1h");
                                    } else {
                                        hourlyWeatherForecast_tmp.snow = 0;
                                    }
                                    ////    PoP -   Probability of Precipitations
                                    hourlyWeatherForecast_tmp.pop = hourlyForecastWeatherJSON_tmp.getDouble("pop");

                                    //    Weather descriptions
                                    JSONObject hourlyForecastWeatherDescriptionsJSON = hourlyForecastWeatherJSON_tmp.getJSONArray("weather").getJSONObject(0);
                                    hourlyWeatherForecast_tmp.weather = hourlyForecastWeatherDescriptionsJSON.getString("main");
                                    hourlyWeatherForecast_tmp.weatherDescription = hourlyForecastWeatherDescriptionsJSON.getString("description");
                                    hourlyWeatherForecast_tmp.weatherCode = hourlyForecastWeatherDescriptionsJSON.getInt("id");

                                    hourlyWeatherForecastArrayList_tmp.add(i, hourlyWeatherForecast_tmp);
                                }
                                place.setHourlyWeatherForecastArrayList(hourlyWeatherForecastArrayList_tmp);


                                //  Daily Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray dailyWeatherJSON = response.getJSONArray("daily");
                                JSONObject dailyWeatherJSONtmp;

                                DailyWeatherForecast dailyWeatherForecast_tmp;
                                ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList_tmp = new ArrayList<DailyWeatherForecast>();

                                for (int i = 0; i < dailyWeatherJSON.length(); i++) {

                                    dailyWeatherJSONtmp = dailyWeatherJSON.getJSONObject(i);
                                    dailyWeatherForecast_tmp = new DailyWeatherForecast();

                                    //  Time
                                    dailyWeatherForecast_tmp.dt = dailyWeatherJSONtmp.getLong("dt") * 1000;

                                    //  Temperatures
                                    JSONObject dailyWeatherTemperaturesJSON = dailyWeatherJSONtmp.getJSONObject("temp");
                                    dailyWeatherForecast_tmp.temperatureMorning = dailyWeatherTemperaturesJSON.getDouble("morn");
                                    dailyWeatherForecast_tmp.temperatureDay = dailyWeatherTemperaturesJSON.getDouble("day");
                                    dailyWeatherForecast_tmp.temperatureEvening = dailyWeatherTemperaturesJSON.getDouble("eve");
                                    dailyWeatherForecast_tmp.temperatureNight = dailyWeatherTemperaturesJSON.getDouble("night");
                                    dailyWeatherForecast_tmp.temperatureMinimum = dailyWeatherTemperaturesJSON.getDouble("min");
                                    dailyWeatherForecast_tmp.temperatureMaximum = dailyWeatherTemperaturesJSON.getDouble("max");

                                    //  Feels Like Temperatures
                                    JSONObject dailyWeatherTemperaturesFeelsLikeJSON = dailyWeatherJSONtmp.getJSONObject("feels_like");
                                    dailyWeatherForecast_tmp.temperatureMorningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("morn");
                                    dailyWeatherForecast_tmp.temperatureDayFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("day");
                                    dailyWeatherForecast_tmp.temperatureEveningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("eve");
                                    dailyWeatherForecast_tmp.temperatureNightFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("night");

                                    //  Pressure, Humidity, dewPoint
                                    dailyWeatherForecast_tmp.pressure = dailyWeatherJSONtmp.getInt("pressure");
                                    dailyWeatherForecast_tmp.humidity = dailyWeatherJSONtmp.getInt("humidity");
                                    dailyWeatherForecast_tmp.dewPoint = dailyWeatherJSONtmp.getDouble("dew_point");

                                    //  Sky
                                    dailyWeatherForecast_tmp.cloudiness = dailyWeatherJSONtmp.getInt("clouds");
                                    dailyWeatherForecast_tmp.sunrise = dailyWeatherJSONtmp.getLong("sunrise") * 1000;
                                    dailyWeatherForecast_tmp.sunset = dailyWeatherJSONtmp.getLong("sunset") * 1000;

                                    //  Wind
                                    dailyWeatherForecast_tmp.windSpeed = dailyWeatherJSONtmp.getDouble("wind_speed");
                                    dailyWeatherForecast_tmp.windDirection = dailyWeatherJSONtmp.getInt("wind_deg");
                                    ////    Wind Gusts
                                    if (dailyWeatherJSONtmp.has("wind_gust")) {
                                        dailyWeatherForecast_tmp.windGustSpeed = dailyWeatherJSONtmp.getDouble("wind_gust");
                                    } else {
                                        dailyWeatherForecast_tmp.windGustSpeed = 0;
                                    }

                                    //  Precipitations
                                    ////    Rain
                                    if (dailyWeatherJSONtmp.has("rain")) {
                                        dailyWeatherForecast_tmp.rain = dailyWeatherJSONtmp.getDouble("rain");
                                    } else {
                                        dailyWeatherForecast_tmp.rain = 0;
                                    }
                                    ////    Snow
                                    if (dailyWeatherJSONtmp.has("snow")) {
                                        dailyWeatherForecast_tmp.snow = dailyWeatherJSONtmp.getDouble("snow");
                                    } else {
                                        dailyWeatherForecast_tmp.snow = 0;
                                    }
                                    ////    PoP -   Probability of Precipitations
                                    dailyWeatherForecast_tmp.pop = dailyWeatherJSONtmp.getDouble("pop");

                                    //    Weather descriptions
                                    JSONObject dailyWeatherDescriptionsJSON = dailyWeatherJSONtmp.getJSONArray("weather").getJSONObject(0);
                                    dailyWeatherForecast_tmp.weather = dailyWeatherDescriptionsJSON.getString("main");
                                    dailyWeatherForecast_tmp.weatherDescription = dailyWeatherDescriptionsJSON.getString("description");
                                    dailyWeatherForecast_tmp.weatherCode = dailyWeatherDescriptionsJSON.getInt("id");

                                    dailyWeatherForecastArrayList_tmp.add(i, dailyWeatherForecast_tmp);
                                }
                                place.setDailyWeatherForecastArrayList(dailyWeatherForecastArrayList_tmp);


                                //  Weather Alert
                                //________________________________________________________________
                                //

                                ArrayList<WeatherAlert> weatherAlertArrayList_tmp = new ArrayList<WeatherAlert>();

                                if (response.has("alerts")) {
                                    JSONArray weatherAlertJSON = response.getJSONArray("alerts");
                                    JSONObject weatherAlertJSON_tmp;

                                    for (int i = 0; i < weatherAlertJSON.length(); i++) {
                                        weatherAlertJSON_tmp = weatherAlertJSON.getJSONObject(i);
                                        weatherAlertArrayList_tmp.add(i, new WeatherAlert(weatherAlertJSON_tmp.getString("sender_name"), weatherAlertJSON_tmp.getString("event"), weatherAlertJSON_tmp.getLong("start") * 1000, weatherAlertJSON_tmp.getLong("end") * 1000, weatherAlertJSON_tmp.getString("description")));
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
