/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.data.remote

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.WorkerThread
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.remote.mappers.AirQualityMapper
import fr.qgdev.openweather.data.remote.mappers.PlaceDataMapper
import fr.qgdev.openweather.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


/**
 * WeatherService
 * <p>
 * A class to manage the weather data requests.
 * It uses the Volley library to make HTTP requests to the OpenWeatherMap API.
 * It also uses the SettingsManager to get the API key and the default locale.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
class WeatherService private constructor(
    context: Context,
    settingsRepository: SettingsRepository = SettingsRepository.getInstance(context)
) {

    private val urlOWMCoordinatesProperties: String
    private val urlOWMPropertiesName: String
    private val urlOWMWeatherData: String
    private val urlOWMAirQualityData: String

    private val context: Context
    private val requestQueue: RequestQueue
    private val settingsRepository: SettingsRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    companion object {
        @Volatile
        private var INSTANCE: WeatherService? = null

        private val TAG: String? = WeatherService::class.simpleName

        //  The null check is repeated inside the lock on purpose. Without it two threads that both
        //  see a null INSTANCE each construct a service, and the second overwrites the first -
        //  leaving two Volley RequestQueues alive, with callers holding whichever they got.
        fun getInstance(
            context: Context
        ): WeatherService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherService(context).also { INSTANCE = it }
            }
        }

        fun getInstance(
            application: Application,
            settingsRepository: SettingsRepository
        ): WeatherService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherService(application, settingsRepository).also { INSTANCE = it }
            }
        }
    }

    init {
        this.context = context.applicationContext
        this.requestQueue = Volley.newRequestQueue(this.context)
        this.settingsRepository = settingsRepository

        this.urlOWMCoordinatesProperties = context.getString(R.string.url_owm_properties_coordinates)
        this.urlOWMPropertiesName = context.getString(R.string.url_owm_properties_name)
        this.urlOWMWeatherData = context.getString(R.string.url_owm_weather_data)
        this.urlOWMAirQualityData = context.getString(R.string.url_owm_airquality_data)
    }


    private fun deviceIsConnected(): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities != null
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }


    @WorkerThread
    suspend fun getPlaceDataOWM(place: Place, callback: FetchDataCallback) {
        if (! this.deviceIsConnected()) {
            callback.onError(RequestStatus.NOT_CONNECTED)
            return
        }

        //  Locale.ROOT is required: the %f conversions below would otherwise use the device
        //  locale, and a comma decimal separator (fr, de, es...) produces "lat=48,856614",
        //  which is not a valid coordinate.
        val url = String.format(
            Locale.ROOT,
            this.urlOWMWeatherData,
            settingsRepository.getOneCallVersion().wireValue,
            place.geolocation.coordinates.latitude,
            place.geolocation.coordinates.longitude,
            settingsRepository.getApiKey(),
            settingsRepository.getDefaultLocale().language
        )

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject? ->
                try {
                    //  Mapping the JSON response to the Place object
                    val mappedPlaceBuilder = response?.let { PlaceDataMapper.fromOWMToProtoBuilder(it) }

                    if (mappedPlaceBuilder == null) {
                        serviceScope.launch {
                            callback.onError(RequestStatus.UNKNOWN_ERROR)
                        }
                        return@JsonObjectRequest
                    }

                    //  Rebuild properties
                    val currentTime = System.currentTimeMillis()

                    //  Every forecast list is a timestamped snapshot, so each response replaces
                    //  the stored one instead of extending it. addAll appends in protobuf, so
                    //  without these clears the lists grow by a full response on every refresh.
                    //  Clearing is unconditional on purpose: a stale entry is worse than a missing
                    //  one here. One Call omits minutely for locations it does not cover and omits
                    //  alerts when none are active, and in both cases the correct result is an
                    //  empty list - an expired alert must disappear, and an hour-old minute-by-
                    //  minute nowcast is misleading rather than merely old.
                    val newPlace = place.toBuilder()
                        .setCurrentWeather(mappedPlaceBuilder.currentWeather)
                        .clearMinutelyForecastList()
                        .addAllMinutelyForecastList(mappedPlaceBuilder.minutelyForecastListList)
                        .clearHourlyForecastList()
                        .addAllHourlyForecastList(mappedPlaceBuilder.hourlyForecastListList)
                        .clearDailyForecastList()
                        .addAllDailyForecastList(mappedPlaceBuilder.dailyForecastListList)
                        .clearWeatherAlertsList()
                        .addAllWeatherAlertsList(mappedPlaceBuilder.weatherAlertsListList)

                    //  The stored properties are kept - they hold the creation time and the air
                    //  quality timestamps, which this response knows nothing about - but the time
                    //  offset comes from the response and can legitimately change (daylight saving),
                    //  so it is carried over rather than left at its stored value.
                    val properties = place.properties
                        .toBuilder()
                        .setLastWeatherUpdateAttemptTime(currentTime)
                        .setLastSuccessfulWeatherUpdateTime(currentTime)
                        .setTimeOffset(mappedPlaceBuilder.properties.timeOffset)
                        .build()

                    newPlace
                        .setProperties(properties)

                    getAirQualityDataOWM(newPlace.build(), callback)
                } catch (error: JSONException) {
                    error.message?.let { Log.w(TAG, it) }
                    serviceScope.launch {
                        callback.onError(RequestStatus.UNKNOWN_ERROR)
                    }
                }
            },
            { error: VolleyError ->
                serviceScope.launch {
                    val status = RequestStatus.fromHttpStatus(error.networkResponse?.statusCode)
                    if (status == RequestStatus.UNKNOWN_ERROR) error.message?.let { Log.w(TAG, it) }
                    callback.onError(status)
                }
            }
        )

        requestQueue.add(request)
    }


    @WorkerThread
    private fun getAirQualityDataOWM(place: Place, callback: FetchDataCallback) {
        //  Let's rebuild the above code snippet
        if (! this.deviceIsConnected()) {
            serviceScope.launch {
                callback.onPartialSuccess(place, RequestStatus.NOT_CONNECTED)
            }
            return
        }

        //  Locale.ROOT for the same reason as in getPlaceDataOWM above.
        val url = String.format(
            Locale.ROOT,
            urlOWMAirQualityData,
            place.geolocation.coordinates.latitude,
            place.geolocation.coordinates.longitude,
            settingsRepository.getApiKey()
        )

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject? ->
                serviceScope.launch {
                    try {
                        if (response == null) {
                            callback.onPartialSuccess(place, RequestStatus.UNKNOWN_ERROR)
                            return@launch
                        }
                        //  Mapping the JSON response to the Place object
                        val airQuality = AirQualityMapper.fromOWMToProto(response)
                        val newPlace = place.toBuilder()
                            .setAirQuality(airQuality)
                            .setProperties(
                                place.properties.toBuilder()
                                    .setLastSuccessfulAirQualityUpdateTime(System.currentTimeMillis())
                                    .setLastAirQualityUpdateAttemptTime(System.currentTimeMillis())
                                    .build()
                            )
                            .build()
                        callback.onSuccess(newPlace)
                    } catch (error: JSONException) {
                        error.message?.let { Log.w(TAG, it) }
                        callback.onPartialSuccess(place, RequestStatus.UNKNOWN_ERROR)
                    }
                }

            },
            { error: VolleyError ->
                serviceScope.launch {
                    val status = RequestStatus.fromHttpStatus(error.networkResponse?.statusCode)
                    if (status == RequestStatus.UNKNOWN_ERROR) error.message?.let { Log.w(TAG, it) }
                    callback.onPartialSuccess(place, status)
                }
            }
        )

        requestQueue.add(request)
    }
}