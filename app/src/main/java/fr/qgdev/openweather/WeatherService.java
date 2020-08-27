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

import java.util.Date;

import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;
import fr.qgdev.openweather.weather.MinutelyWeatherForecast;

public class WeatherService{

    private static final String TAG = WeatherService.class.getSimpleName();

    private static final String WEATHER_SERVICE_TAG = "WEATHER_SERVICE";
    private static String API_KEY;

    private DataPlaces dataPlaces;

    private Context context;
    private RequestQueue queue;

    public WeatherService(final Activity activity, @NonNull String apiKey, @NonNull DataPlaces dataPlaces){
        context = activity.getApplicationContext();
        this.dataPlaces = dataPlaces;

        queue = Volley.newRequestQueue(context);
        API_KEY = apiKey;
    }

    public WeatherService(final Context context, @NonNull String apiKey, @NonNull DataPlaces dataPlaces){
        this.context = context;
        this.dataPlaces = dataPlaces;

        queue = Volley.newRequestQueue(context);
        API_KEY = apiKey;
    }

    public interface WeatherCallback{
        void onWeatherData(final Place place, DataPlaces dataPlaces);

        void onError(Exception exception, Place place, DataPlaces dataPlaces);

        void onConnectionError(VolleyError noConnectionError, Place place, DataPlaces dataPlaces);
    }

    @WorkerThread
    public void getWeatherDataOWM(Place place, WeatherCallback callback) {

        //  Setting up important variables and objects for weather data request
        String url = String.format(context.getString(R.string.url_owm_weatherdata), place.getLatitude(), place.getLongitude(), API_KEY);

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

                                CurrentWeather currentWeather = new CurrentWeather();

                                //  The time of this update
                                place.setLastUpdate(currentWeatherJSON.getLong("dt") * 1000);
                                place.setLastUpdateDate(new Date(place.getLastUpdate()));
                                currentWeather.dt = currentWeatherJSON.getLong("dt") * 1000;
                                //    Temperatures
                                currentWeather.temperature = currentWeatherJSON.getDouble("temp");
                                currentWeather.temperatureFeelsLike = currentWeatherJSON.getDouble("feels_like");
                                //    Pressure, Humidity, dewPoint, uvIndex
                                currentWeather.pressure = currentWeatherJSON.getInt("pressure");
                                currentWeather.humidity = currentWeatherJSON.getInt("humidity");
                                currentWeather.dewPoint = currentWeatherJSON.getDouble("dew_point");
                                currentWeather.uvIndex = currentWeatherJSON.getInt("uvi");
                                //    Sky informations
                                currentWeather.cloudiness = currentWeatherJSON.getInt("clouds");
                                currentWeather.visibility = currentWeatherJSON.getInt("visibility");
                                currentWeather.sunrise = currentWeatherJSON.getLong("sunrise") * 1000;
                                currentWeather.sunset = currentWeatherJSON.getLong("sunset") * 1000;
                                //    Wind informations
                                currentWeather.windSpeed = currentWeatherJSON.getDouble("wind_speed");
                                ////  Enough wind for a viable wind direction information
                                currentWeather.isWindDirectionReadable = currentWeatherJSON.has("wind_deg");
                                if (currentWeather.isWindDirectionReadable) {
                                    currentWeather.windDirection = currentWeatherJSON.getInt("wind_deg");
                                }
                                ////    Wind Gusts
                                if (currentWeatherJSON.has("wind_gust")) {
                                    currentWeather.windGustSpeed = currentWeatherJSON.getDouble("wind_gust");
                                } else {
                                    currentWeather.windGustSpeed = 0;
                                }
                                //  Precipitations
                                ////    Rain
                                if (currentWeatherJSON.has("rain") && currentWeatherJSON.getJSONObject("rain").has("1h")) {
                                    currentWeather.rain = currentWeatherJSON.getJSONObject("rain").getDouble("1h");
                                } else {
                                    currentWeather.rain = 0;
                                }
                                ////    Snow
                                if (currentWeatherJSON.has("snow") && currentWeatherJSON.getJSONObject("snow").has("1h")) {
                                    currentWeather.snow = currentWeatherJSON.getJSONObject("snow").getDouble("1h");
                                } else {
                                    currentWeather.snow = 0;
                                }
                                //    Weather descriptions
                                JSONObject currentWeatherDescriptionsJSON = currentWeatherJSON.getJSONArray("weather").getJSONObject(0);
                                currentWeather.weather = currentWeatherDescriptionsJSON.getString("main");
                                currentWeather.weatherDescription = currentWeatherDescriptionsJSON.getString("description");

                                place.setCurrentWeather(currentWeather);


                                //  Minutely Weather Forecast
                                //________________________________________________________________
                                //      ONLY FOR UNITED-STATES NOW

                                if (place.getCountryCode().equals("US")) {

                                    JSONArray minutelyForecastWeatherJSON = response.getJSONArray("minutely");
                                    JSONObject minutelyForecastWeatherJSONtmp;

                                    MinutelyWeatherForecast minutelyWeatherForecasttmp;

                                    for (int i = 0; i <= 60; i++) {

                                        minutelyForecastWeatherJSONtmp = minutelyForecastWeatherJSON.getJSONObject(i);
                                        minutelyWeatherForecasttmp = new MinutelyWeatherForecast();

                                        minutelyWeatherForecasttmp.dt = minutelyForecastWeatherJSONtmp.getLong("dt") * 1000;
                                        minutelyWeatherForecasttmp.precipitation = minutelyForecastWeatherJSONtmp.getInt("precipitation");


                                        if (place.getMinutelyWeatherForecastArrayList().size() == 61) {
                                            place.setMinutelyWeatherForecast(i, minutelyWeatherForecasttmp);
                                        } else {
                                            place.addMinutelyWeatherForecast(i, minutelyWeatherForecasttmp);
                                        }
                                    }
                                }


                                //  Hourly Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray hourlyForecastWeatherJSON = response.getJSONArray("hourly");
                                JSONObject hourlyForecastWeatherJSONtmp;

                                HourlyWeatherForecast hourlyWeatherForecasttmp;

                                for (int i = 0; i <= 47; i++) {

                                    hourlyForecastWeatherJSONtmp = hourlyForecastWeatherJSON.getJSONObject(i);
                                    hourlyWeatherForecasttmp = new HourlyWeatherForecast();

                                    //  Time
                                    hourlyWeatherForecasttmp.dt = hourlyForecastWeatherJSONtmp.getLong("dt") * 1000;
                                    //  Temperatures
                                    hourlyWeatherForecasttmp.temperature = hourlyForecastWeatherJSONtmp.getDouble("temp");
                                    hourlyWeatherForecasttmp.temperatureFeelsLike = hourlyForecastWeatherJSONtmp.getDouble("feels_like");
                                    //  Pressure, Humidity, Visibility, cloudiness, dewPoint
                                    hourlyWeatherForecasttmp.pressure = hourlyForecastWeatherJSONtmp.getInt("pressure");
                                    hourlyWeatherForecasttmp.humidity = hourlyForecastWeatherJSONtmp.getInt("humidity");
                                    hourlyWeatherForecasttmp.visibility = hourlyForecastWeatherJSONtmp.getInt("visibility");
                                    hourlyWeatherForecasttmp.cloudiness = hourlyForecastWeatherJSONtmp.getInt("clouds");
                                    hourlyWeatherForecasttmp.dewPoint = hourlyForecastWeatherJSONtmp.getDouble("dew_point");
                                    //  Wind
                                    hourlyWeatherForecasttmp.windSpeed = hourlyForecastWeatherJSONtmp.getDouble("wind_speed");
                                    hourlyWeatherForecasttmp.windDirection = hourlyForecastWeatherJSONtmp.getInt("wind_deg");
                                    ////    Wind Gusts
                                    if (hourlyForecastWeatherJSONtmp.has("wind_gust")) {
                                        hourlyWeatherForecasttmp.windGustSpeed = hourlyForecastWeatherJSONtmp.getDouble("wind_gust");
                                    } else {
                                        hourlyWeatherForecasttmp.windGustSpeed = 0;
                                    }
                                    //  Precipitations
                                    ////    Rain
                                    if (hourlyForecastWeatherJSONtmp.has("rain") && hourlyForecastWeatherJSONtmp.getJSONObject("rain").has("1h")) {
                                        hourlyWeatherForecasttmp.rain = hourlyForecastWeatherJSONtmp.getJSONObject("rain").getDouble("1h");
                                    } else {
                                        hourlyWeatherForecasttmp.rain = 0;
                                    }
                                    ////    Snow
                                    if (hourlyForecastWeatherJSONtmp.has("snow") && hourlyForecastWeatherJSONtmp.getJSONObject("snow").has("1h")) {
                                        hourlyWeatherForecasttmp.snow = hourlyForecastWeatherJSONtmp.getJSONObject("snow").getDouble("1h");
                                    } else {
                                        hourlyWeatherForecasttmp.snow = 0;
                                    }
                                    ////    PoP -   Probability of Precipitations
                                    hourlyWeatherForecasttmp.pop = hourlyForecastWeatherJSONtmp.getDouble("pop");
                                    //    Weather descriptions
                                    JSONObject hourlyForecastWeatherDescriptionsJSON = hourlyForecastWeatherJSONtmp.getJSONArray("weather").getJSONObject(0);
                                    hourlyWeatherForecasttmp.weather = hourlyForecastWeatherDescriptionsJSON.getString("main");
                                    hourlyWeatherForecasttmp.weatherDescription = hourlyForecastWeatherDescriptionsJSON.getString("description");


                                    if (place.getHourlyWeatherForecastArrayList().size() == 48) {
                                        place.setHourlyWeatherForecast(i, hourlyWeatherForecasttmp);
                                    } else {
                                        place.addHourlyWeatherForecast(i, hourlyWeatherForecasttmp);
                                    }

                                }


                                //  Daily Weather Forecast
                                //________________________________________________________________
                                //

                                JSONArray dailyWeatherJSON = response.getJSONArray("daily");
                                JSONObject dailyWeatherJSONtmp;

                                DailyWeatherForecast dailyWeatherForecasttmp;

                                for (int i = 0; i <= 7; i++) {

                                    dailyWeatherJSONtmp = dailyWeatherJSON.getJSONObject(i);
                                    dailyWeatherForecasttmp = new DailyWeatherForecast();

                                    //  Time
                                    dailyWeatherForecasttmp.dt = dailyWeatherJSONtmp.getLong("dt") * 1000;
                                    //  Temperatures
                                    JSONObject dailyWeatherTemperaturesJSON = dailyWeatherJSONtmp.getJSONObject("temp");
                                    dailyWeatherForecasttmp.temperatureMorning = dailyWeatherTemperaturesJSON.getDouble("morn");
                                    dailyWeatherForecasttmp.temperatureDay = dailyWeatherTemperaturesJSON.getDouble("day");
                                    dailyWeatherForecasttmp.temperatureEvening = dailyWeatherTemperaturesJSON.getDouble("eve");
                                    dailyWeatherForecasttmp.temperatureNight = dailyWeatherTemperaturesJSON.getDouble("night");
                                    dailyWeatherForecasttmp.temperatureMinimum = dailyWeatherTemperaturesJSON.getDouble("min");
                                    dailyWeatherForecasttmp.temperatureMaximum = dailyWeatherTemperaturesJSON.getDouble("max");
                                    //  Feels Like Temperatures
                                    JSONObject dailyWeatherTemperaturesFeelsLikeJSON = dailyWeatherJSONtmp.getJSONObject("feels_like");
                                    dailyWeatherForecasttmp.temperatureMorningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("morn");
                                    dailyWeatherForecasttmp.temperatureDayFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("day");
                                    dailyWeatherForecasttmp.temperatureEveningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("eve");
                                    dailyWeatherForecasttmp.temperatureNightFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("night");
                                    //  Pressure, Humidity, dewPoint
                                    dailyWeatherForecasttmp.pressure = dailyWeatherJSONtmp.getInt("pressure");
                                    dailyWeatherForecasttmp.humidity = dailyWeatherJSONtmp.getInt("humidity");
                                    dailyWeatherForecasttmp.dewPoint = dailyWeatherJSONtmp.getDouble("dew_point");
                                    //  Sky
                                    dailyWeatherForecasttmp.cloudiness = dailyWeatherJSONtmp.getInt("clouds");
                                    dailyWeatherForecasttmp.sunrise = dailyWeatherJSONtmp.getLong("sunrise") * 1000;
                                    dailyWeatherForecasttmp.sunset = dailyWeatherJSONtmp.getLong("sunset") * 1000;
                                    //  Wind
                                    dailyWeatherForecasttmp.windSpeed = dailyWeatherJSONtmp.getDouble("wind_speed");
                                    dailyWeatherForecasttmp.windDirection = dailyWeatherJSONtmp.getInt("wind_deg");
                                    ////    Wind Gusts
                                    if (dailyWeatherJSONtmp.has("wind_gust")) {
                                        dailyWeatherForecasttmp.windGustSpeed = dailyWeatherJSONtmp.getDouble("wind_gust");
                                    } else {
                                        dailyWeatherForecasttmp.windGustSpeed = 0;
                                    }
                                    //  Precipitations
                                    ////    Rain
                                    if (dailyWeatherJSONtmp.has("rain")) {
                                        dailyWeatherForecasttmp.rain = dailyWeatherJSONtmp.getDouble("rain");
                                    } else {
                                        dailyWeatherForecasttmp.rain = 0;
                                    }
                                    ////    Snow
                                    if (dailyWeatherJSONtmp.has("snow")) {
                                        dailyWeatherForecasttmp.snow = dailyWeatherJSONtmp.getDouble("snow");
                                    } else {
                                        dailyWeatherForecasttmp.snow = 0;
                                    }
                                    ////    PoP -   Probability of Precipitations
                                    dailyWeatherForecasttmp.pop = dailyWeatherJSONtmp.getDouble("pop");
                                    //    Weather descriptions
                                    JSONObject dailyWeatherDescriptionsJSON = dailyWeatherJSONtmp.getJSONArray("weather").getJSONObject(0);
                                    dailyWeatherForecasttmp.weather = dailyWeatherDescriptionsJSON.getString("main");
                                    dailyWeatherForecasttmp.weatherDescription = dailyWeatherDescriptionsJSON.getString("description");


                                    if (place.getDailyWeatherForecastArrayList().size() == 8) {
                                        place.setDailyWeatherForecast(i, dailyWeatherForecasttmp);
                                    } else {
                                        place.addDailyWeatherForecast(i, dailyWeatherForecasttmp);
                                    }
                                }

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
