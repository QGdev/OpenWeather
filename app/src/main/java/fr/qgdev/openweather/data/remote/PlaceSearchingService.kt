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

import java.util.Locale
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.Geolocation
import org.json.JSONArray
import org.json.JSONObject

class PlaceSearchingService private constructor(
    context: Context,
) {

    private val urlOSMPlaceSearching: String

    private val context: Context
    private val requestQueue: RequestQueue


    companion object {
        @Volatile
        private var INSTANCE: PlaceSearchingService? = null

        private val TAG: String? = PlaceSearchingService::class.simpleName

        //  See WeatherService.getInstance: the second null check inside the lock is what stops two
        //  threads each building a service, and with it a second Volley RequestQueue.
        fun getInstance(
            context: Context
        ): PlaceSearchingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaceSearchingService(context).also { INSTANCE = it }
            }
        }
    }

    init {
        this.context = context.applicationContext
        this.requestQueue = Volley.newRequestQueue(this.context)

        this.urlOSMPlaceSearching = context.getString(R.string.url_osm_place_searching)
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


    fun fetchLocationDetails(
        query: String,
        callback: FetchCallback<List<Geolocation>>
    ) {
        if (query.isEmpty() || query.length < 3) {
            callback.onError(RequestStatus.TOO_SHORT)
            return
        }

        if (!deviceIsConnected()) {
            callback.onError(RequestStatus.NOT_CONNECTED)
            return
        }


        //  The query goes straight into the query string, so it has to be percent-encoded.
        //  Without this, anything containing a space, an accent or an & - "Saint-Étienne",
        //  "New York" - produces a malformed URL.
        val fullFilledURL = urlOSMPlaceSearching.replace("{search}", Uri.encode(query))

        val request = object : JsonArrayRequest(
            Request.Method.GET, fullFilledURL, null,
            { response ->
                val locations = parseLocationResponse(response)
                callback.onSuccess(locations)
            },
            { error: VolleyError ->
                //  no server response (NO INTERNET or SERVER DOWN)
                if (error.networkResponse == null) {
                    callback.onError(RequestStatus.NO_ANSWER)
                    error.printStackTrace()
                } else {
                    when (error.networkResponse.statusCode) {
                        429 -> callback.onError(RequestStatus.TOO_MANY_REQUESTS)
                        404 -> callback.onError(RequestStatus.NOT_FOUND)
                        403 -> callback.onError(RequestStatus.AUTH_FAILED)
                        401 -> callback.onError(RequestStatus.AUTH_FAILED)
                        else -> {
                            callback.onError(RequestStatus.UNKNOWN_ERROR)
                            error.message?.let { Log.w(TAG, it) }
                        }
                    }
                }
            }
        ) {
            // Nominatim usage policy requires a valid User-Agent identifying the application.
            // Without it, requests are rejected with HTTP 403.
            // See: https://operations.osmfoundation.org/policies/nominatim/
            override fun getHeaders(): MutableMap<String, String> {
                val version = runCatching {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
                }.getOrDefault("unknown")
                return hashMapOf(
                    "User-Agent" to "OpenWeather/$version (Android)",
                    //  Names in the device's language: without it Nominatim answers with each
                    //  place's local name - "東京都" for Tokyo, "Venezia" for Venice. English follows
                    //  as a fallback for places with no name in that language.
                    "Accept-Language" to "${Locale.getDefault().toLanguageTag()},en;q=0.5"
                )
            }
        }

        requestQueue.add(request)
    }

    //  Nominatim does not guarantee every field on every result, and this runs inside the Volley
    //  success callback on the main thread, so a hard get* on a missing field would crash the app
    //  rather than fail the search. Results without usable coordinates are skipped; a missing
    //  country code still leaves a usable result.
    private fun parseLocationResponse(response: JSONArray): List<Geolocation> {
        val locations = mutableListOf<SearchCandidate>()
        for (i in 0 until response.length()) {
            val result = response.optJSONObject(i) ?: continue
            val lat = result.optDouble("lat", Double.NaN)
            val lon = result.optDouble("lon", Double.NaN)
            if (lat.isNaN() || lon.isNaN()) continue
            val address = result.optJSONObject("address")
            val city = result.optString("name", "N/A")
            val countryCode = address?.optString("country_code", "") ?: ""
            //  Nominatim names the administrative area differently by country: a French region is
            //  "state", an English county is "county", and some results carry only "region". The
            //  first one present is the one a reader would use to tell two same-named cities apart.
            val region = listOf("state", "region", "county")
                .firstNotNullOfOrNull { key ->
                    address?.optString(key, "")?.takeIf { it.isNotEmpty() }
                }
                .orEmpty()
            val coordinates = Coordinates.newBuilder()
                .setLatitude(lat)
                .setLongitude(lon)
                .build()
            val geolocation = Geolocation.newBuilder()
                .setCity(city)
                .setCountryCode(countryCode)
                .setRegion(region)
                .setCoordinates(coordinates)
                .build()
            //  "place" is OpenStreetMap's class for a town's own point, as opposed to its boundary.
            val isPlacePoint = result.optString("class") == "place"
            locations.add(SearchCandidate(geolocation, isPlacePoint))
        }
        return mergeNearDuplicates(locations)
    }
}