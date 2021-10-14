package fr.qgdev.openweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import fr.qgdev.openweather.dataplaces.DataPlaces;
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


	public WeatherService(final Context context, @NonNull String apiKey, @NonNull String language, @NonNull final DataPlaces dataPlaces) {
		this.context = context;
		this.dataPlaces = dataPlaces;

		this.queue = Volley.newRequestQueue(context);
		WeatherService.apiKey = apiKey;
		WeatherService.language = language;
	}

	@WorkerThread
	public void getCoordinatesOWM(Place place, WeatherCallbackGetCoordinates callback) {

		//  Setting up important variables and objects for weather data request
		String url = String.format(context.getString(R.string.url_owm_coordinates), place.getCity(), place.getCountryCode(), apiKey);

		//  Before launching request, we must have to verify that if the device is connected to a network
		//  The device is connected to an INTERNET capable network
		if (this.deviceIsConnected()) {
			JsonObjectRequest weatherRequest = new JsonObjectRequest
					(Request.Method.GET, url, null,
							response -> {
								try {
									JSONObject coordinatesJSON = response.getJSONObject("coord");
									place.setLongitude(coordinatesJSON.getDouble("lon"));
									place.setLatitude(coordinatesJSON.getDouble("lat"));

									Log.d(TAG, "Place information treatment completed");
									callback.onPlaceFound(place, dataPlaces);
								} catch (JSONException e) {
									Log.w(TAG, "Place information treatment failed due to a JSONException");
									e.printStackTrace();
									callback.onTreatmentError();
								} finally {
									callback.onTheEndOfTheRequest();
								}
							},
							error -> {
								//  no server response (NO INTERNET or SERVER DOWN)
								if (error.networkResponse == null) {
									callback.onNoResponseError();
									Log.w(TAG, "Place information request failed - NO RESPONSE");
								}
								//  Server response
								else {
									switch (error.networkResponse.statusCode) {
										case 429:   //  Too many requests
											callback.onTooManyRequestsError();
											Log.w(TAG, "Place information request failed - TOO MANY REQUESTS");
											break;
										case 404:   //  Place not found
											callback.onPlaceNotFoundError();
											Log.w(TAG, "Place information request failed - PLACE NOT FOUND");
											break;
										case 401:   //  Unknown or wrong API key
											callback.onWrongOrUnknownApiKeyError();
											Log.w(TAG, "Place information request failed - API KEY PROBLEM");
											break;
										default:    //  Unknown error
											callback.onUnknownError();
											Log.w(TAG, "Place information request failed - UNKNOWN ERROR");
											error.printStackTrace();
											break;
									}
								}
								callback.onTheEndOfTheRequest();
							});

			queue.add(weatherRequest);
		}

		//  The device isn't connected to an INTERNET capable network
		else {
			Log.w(TAG, "Place information request failed from OWM");
			callback.onDeviceNotConnected();
		}
	}

	@WorkerThread
	public void getWeatherDataOWM(Place place, DataPlaces dataPlaces, WeatherCallbackGetData callback) {

		//  Setting up important variables and objects for weather data request
		String url = String.format(context.getString(R.string.url_owm_weatherdata), place.getLatitude(), place.getLongitude(), apiKey, language);

		//  Before launching request, we must have to verify that if the device is connected to a network
		//  The device is connected to an INTERNET capable network
		if (this.deviceIsConnected()) {
			JsonObjectRequest weatherRequest = new JsonObjectRequest
					(Request.Method.GET, url, null,
							response -> {
								try {
									//  TimeOffSet
									//________________________________________________________________
									//
									place.setTimeZoneOffset(response.getInt("timezone_offset"));

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
									//  The minutely arrayList in place object will remain empty
									if (response.has("minutely")) {
										JSONArray minutelyWeatherForecastJSON = response.getJSONArray("minutely");
										ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList_tmp = new ArrayList<>();
										MinutelyWeatherForecast minutelyWeatherForecast_tmp = new MinutelyWeatherForecast();

										for (int i = 0; i < minutelyWeatherForecastJSON.length(); i++) {
											minutelyWeatherForecast_tmp.fillWithOWMData(minutelyWeatherForecastJSON.getJSONObject(i));
											minutelyWeatherForecastArrayList_tmp.add(i, minutelyWeatherForecast_tmp.clone());
										}

										place.setMinutelyWeatherForecastArrayList(minutelyWeatherForecastArrayList_tmp);
									}


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
											weatherAlertArrayList_tmp.add(weatherAlert_tmp.clone());
										}
									}

									place.setWeatherAlertsArrayList(weatherAlertArrayList_tmp);


									Log.d(TAG, "Weather information treatment completed");
									callback.onWeatherData(place, dataPlaces);

								} catch (JSONException e) {
									Log.w(TAG, "Weather information treatment failed due to a JSONException");
									e.printStackTrace();
									callback.onTreatmentError();
								} finally {
									callback.onTheEndOfTheRequest();
								}
							},
							error -> {
								//  no server response (NO INTERNET or SERVER DOWN)
								if (error.networkResponse == null) {
									callback.onNoResponseError();
									Log.w(TAG, "Weather information request failed - NO RESPONSE");
								}
								//  Server response
								else {
									switch (error.networkResponse.statusCode) {
										case 429:   //  Too many requests
											callback.onTooManyRequestsError();
											Log.w(TAG, "Weather information request failed - TOO MANY REQUESTS");
											break;
										case 404:   //  Place not found
											callback.onPlaceNotFoundError();
											Log.w(TAG, "Weather information request failed - PLACE NOT FOUND");
											break;
										case 401:   //  Unknown or wrong API key
											callback.onWrongOrUnknownApiKeyError();
											Log.w(TAG, "Weather information request failed - API KEY PROBLEM");
											break;
										default:    //  Unknown error
											callback.onUnknownError();
											Log.w(TAG, "Weather information request failed - UNKNOWN ERROR");
											error.printStackTrace();
											break;
									}
								}
								callback.onTheEndOfTheRequest();
							});

			queue.add(weatherRequest);
		}

		//  The device isn't connected to an INTERNET capable network
		else {
			Log.w(TAG, "Weather information request failed from OWM");
			callback.onDeviceNotConnected();
		}
	}

	public boolean deviceIsConnected() {
		ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
		Network[] networks = connectivityManager.getAllNetworks();
		NetworkCapabilities networkCapabilities;
		boolean deviceIsConnected = false;

		for (Network network : networks) {
			networkCapabilities = connectivityManager.getNetworkCapabilities(network);
			if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
				deviceIsConnected = true;
				break;
			}
		}
		return deviceIsConnected;
	}

	public void cancel() {
		queue.cancelAll(WEATHER_SERVICE_TAG);
	}


	public interface WeatherCallbackGetData {
		void onWeatherData(final Place place, DataPlaces dataPlaces);

		void onTreatmentError();

		void onNoResponseError();

		void onTooManyRequestsError();

		void onPlaceNotFoundError();

		void onWrongOrUnknownApiKeyError();

		void onUnknownError();

		void onDeviceNotConnected();

		void onTheEndOfTheRequest();
	}

	public interface WeatherCallbackGetCoordinates {
		void onPlaceFound(final Place place, DataPlaces dataPlaces);

		void onTreatmentError();

		void onNoResponseError();

		void onTooManyRequestsError();

		void onPlaceNotFoundError();

		void onWrongOrUnknownApiKeyError();

		void onUnknownError();

		void onDeviceNotConnected();

		void onTheEndOfTheRequest();
	}
}
