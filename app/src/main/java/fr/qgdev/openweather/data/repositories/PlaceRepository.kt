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

package fr.qgdev.openweather.data.repositories

import android.content.Context
import android.util.Log
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.PlaceStorage
import fr.qgdev.openweather.data.remote.FetchCallback
import fr.qgdev.openweather.data.remote.FetchDataCallback
import fr.qgdev.openweather.data.remote.PlaceSearchingService
import fr.qgdev.openweather.data.remote.RequestStatus
import fr.qgdev.openweather.data.remote.WeatherService
import fr.qgdev.openweather.data.storage.PlaceDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class PlaceRepository private constructor(context: Context) {
    private val weatherService = WeatherService.getInstance(context)
    private val placeSearchingService = PlaceSearchingService.getInstance(context)
    private val dataStore = PlaceDataStore.getDataStore(context.applicationContext)

    val placesFlow: Flow<List<Place>> = dataStore.data
        .catch {exception ->
            if (exception is IOException) {
                Log.e("PlaceRepository", "Error reading placeStorage.", exception)
                emit(PlaceStorage.getDefaultInstance())
            } else { throw exception }
        }
        .map { placeStorage ->
        placeStorage.orderedPlaceKeysList.mapNotNull { placeKey ->
            placeStorage.placesMap[placeKey]
        }
    }

    companion object {
        @Volatile
        private var instance: PlaceRepository? = null

        fun getInstance(context: Context): PlaceRepository {
            return instance ?: synchronized(this) {
                instance ?: PlaceRepository(context).also { instance = it }
            }
        }
    }

    suspend fun isPlaceStorageEmpty(): Boolean {
        return dataStore.data.single().placesCount == 0
    }

    suspend fun isPlaceStorageFull(): Boolean {
        return dataStore.data.single().placesCount >= 100
    }

    private suspend fun getNextPlaceId(): Int? {
        val placeStorage = dataStore.data.first()

        if (placeStorage.placesCount >= 100) {
            return null // Or Result.failure(...) for a Result-based approach
        }

        return (0..99).firstNotNullOfOrNull { offset ->
            val nextPlaceId = (placeStorage.lastKeyUsed + 1 + offset) % 100
            if (!placeStorage.placesMap.containsKey(nextPlaceId)) {
                nextPlaceId
            } else {
                null
            }
        }
    }

    private suspend fun retrievePlaceKey(place: Place): Int {
        val placeStorage = dataStore.data.first()
        for (placeKey in placeStorage.orderedPlaceKeysList) {
            val storedPlace = placeStorage.getPlacesOrDefault(placeKey, null)
            if (storedPlace != null && storedPlace.geolocation == place.geolocation) {
                return placeKey
            }
        }
        return -1
    }

    suspend fun addPlace(place: Place) {
        val nextPlaceId = getNextPlaceId()
        if (nextPlaceId == null) {
            Log.w("PlaceRepository", "PlaceStorage is full")
            return
        }

        //  Update place creation time to now, since we are adding it now
        val placeProperties = place.properties.toBuilder()
            .setCreationTime(System.currentTimeMillis())
            .build()

        val placeWithCreationTime = place.toBuilder()
            .setProperties(placeProperties).build()

        dataStore.updateData { placeStorage ->
            placeStorage.toBuilder()
                .putPlaces(nextPlaceId, placeWithCreationTime)
                .putPlaceKeys(place.geolocation.hashCode(), nextPlaceId)
                .setLastKeyUsed(nextPlaceId)
                .addOrderedPlaceKeys(nextPlaceId)
                .build()
        }
    }

    suspend fun addPlaceAt(index: Int, place: Place) {
        val nextPlaceId = getNextPlaceId()
        if (nextPlaceId == null) {
            Log.w("PlaceRepository", "PlaceStorage is full")
            return
        }
        dataStore.updateData { placeStorage ->
            val orderedKeys = placeStorage.orderedPlaceKeysList.toMutableList()
            if (index >= 0 && index <= orderedKeys.size) {
                orderedKeys.add(index, nextPlaceId)
            } else {
                orderedKeys.add(nextPlaceId)
            }

            placeStorage.toBuilder()
                .putPlaces(nextPlaceId, place)
                .putPlaceKeys(place.geolocation.hashCode(), nextPlaceId)
                .setLastKeyUsed(nextPlaceId)
                .clearOrderedPlaceKeys()
                .addAllOrderedPlaceKeys(orderedKeys)
                .build()
        }
    }

    suspend fun updatePlace(placeId: Int, newPlace: Place) {
        dataStore.updateData { placeStorage ->
            placeStorage.toBuilder()
                .putPlaces(placeId, newPlace)
                .build()
        }
    }

    suspend fun updatePlace(place: Place) {
        val placeId = retrievePlaceKey(place)
        if (placeId != -1) {
            updatePlace(placeId, place)
        }
    }

    suspend fun deletePlace(placeId: Int) {
        dataStore.updateData { placeStorage ->
            val place = placeStorage.getPlacesOrDefault(placeId, null)
                ?: return@updateData placeStorage

            val orderedPlaceKeys = placeStorage.orderedPlaceKeysList.toMutableList()
            orderedPlaceKeys.remove(placeId)

            placeStorage.toBuilder()
                .removePlaces(placeId)
                .removePlaceKeys(place.geolocation.hashCode())
                .clearOrderedPlaceKeys()
                .addAllOrderedPlaceKeys(orderedPlaceKeys)
                .build()
        }
    }

    suspend fun deletePlace(place: Place) {
        val placeId = retrievePlaceKey(place)
        if (placeId != -1) {
            deletePlace(placeId)
        }
    }

    suspend fun deleteAllPlaces() {
        dataStore.updateData { placeStorage ->
            placeStorage.toBuilder()
                .clear()
                .build()
        }
    }

    suspend fun getPlace(placeId: Int): Place? {
        return dataStore.data.first().placesMap[placeId]
    }

    suspend fun getPlaces(): List<Place> {
        val placeStorage = dataStore.data.first()
        return placeStorage.orderedPlaceKeysList.mapNotNull { placeStorage.placesMap[it] }
    }

    suspend fun getPlaceCount(): Int {
        return dataStore.data.first().placesCount
    }

    /**
     * Updates all stored places from the web concurrently.
     * Uses CompletableDeferred to properly await Volley's async callbacks.
     *
     * @return Pair(successCount, errorCount)
     */
    suspend fun updateAllPlacesFromWeb(): Pair<Int, Int> {
        android.util.Log.d("PlaceRepository", "🔄 [updateAllPlacesFromWeb] Starting...")
        
        val placeStorage = dataStore.data.first()
        val keys = placeStorage.orderedPlaceKeysList.toList()
        
        android.util.Log.d("PlaceRepository", "   Places to update: ${keys.size}")
        if (keys.isEmpty()) {
            android.util.Log.d("PlaceRepository", "   No places to update")
            return Pair(0, 0)
        }

        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        android.util.Log.d("PlaceRepository", "   Launching ${keys.size} coroutines in parallel...")
        
        // Launch all updates concurrently using async within coroutineScope
        return coroutineScope {
            val jobs = keys.map { placeId ->
                async {
                    android.util.Log.d("PlaceRepository", "   📌 Starting update for placeId=$placeId")
                    
                    val deferred = CompletableDeferred<Unit>()
                    try {
                        android.util.Log.d("PlaceRepository", "      Calling updatePlaceFromWeb for placeId=$placeId...")
                        updatePlaceFromWeb(placeId, object : FetchDataCallback {
                            override suspend fun onSuccess(place: Place) {
                                android.util.Log.d("PlaceRepository", "   ✅ Place $placeId: onSuccess CALLBACK RECEIVED")
                                successCount.incrementAndGet()
                                deferred.complete(Unit)
                            }
                            override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                                android.util.Log.d("PlaceRepository", "   ⚠️  Place $placeId: onPartialSuccess CALLBACK RECEIVED")
                                successCount.incrementAndGet()
                                deferred.complete(Unit)
                            }
                            override suspend fun onError(requestStatus: RequestStatus) {
                                android.util.Log.d("PlaceRepository", "   ❌ Place $placeId: onError CALLBACK RECEIVED - $requestStatus")
                                errorCount.incrementAndGet()
                                deferred.complete(Unit)
                            }
                        })
                        
                        // Wait for callback to complete with 15 second timeout
                        android.util.Log.d("PlaceRepository", "      Waiting for callback for placeId=$placeId (timeout: 15s)...")
                        val result = withTimeoutOrNull(15000) {
                            deferred.await()
                        }
                        
                        if (result == null) {
                            android.util.Log.e("PlaceRepository", "   ⏱️  Place $placeId: TIMEOUT after 15 seconds waiting for callback!")
                            errorCount.incrementAndGet()
                        } else {
                            android.util.Log.d("PlaceRepository", "      Place $placeId: Callback completed successfully")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlaceRepository", "   💥 Exception in update for placeId=$placeId: ${e.message}", e)
                        errorCount.incrementAndGet()
                    }
                }
            }

            android.util.Log.d("PlaceRepository", "   Waiting for all coroutines to complete (${jobs.size})...")
            jobs.awaitAll()
            
            android.util.Log.d("PlaceRepository", "   All coroutines completed!")
            android.util.Log.d("PlaceRepository", "✅ [updateAllPlacesFromWeb] Completed: ${successCount.get()} success, ${errorCount.get()} errors")
            
            Pair(successCount.get(), errorCount.get())
        }
    }

    //  Remote part, fetch from web
    //  ASYNC TASK
    suspend fun updatePlaceFromWeb(placeId: Int, callback: FetchDataCallback) {
        android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] Starting for placeId=$placeId")
        
        try {
            android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] About to call getPlace($placeId)...")
            val place = getPlace(placeId)
            android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] getPlace() returned, place=${place != null}")
            
            if (place == null) {
                android.util.Log.e("PlaceRepository", "      [updatePlaceFromWeb] Place not found for placeId=$placeId")
                callback.onError(RequestStatus.NOT_FOUND)
                return
            }
            
            android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] Place found, geolocation set")

            val innerCallback = object : FetchDataCallback {
                override suspend fun onSuccess(place: Place) {
                    android.util.Log.d("PlaceRepository", "      [innerCallback] onSuccess received for placeId=$placeId")
                    updatePlace(placeId, place)
                    callback.onSuccess(place)
                }

                override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                    android.util.Log.d("PlaceRepository", "      [innerCallback] onPartialSuccess received for placeId=$placeId")
                    updatePlace(placeId, place)
                    callback.onPartialSuccess(place, requestStatus)
                }

                override suspend fun onError(requestStatus: RequestStatus) {
                    android.util.Log.e("PlaceRepository", "      [innerCallback] onError received for placeId=$placeId: $requestStatus")
                    callback.onError(requestStatus)
                }
            }

            android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] Calling weatherService.getPlaceDataOWM()...")
            weatherService.getPlaceDataOWM(place, innerCallback)
            android.util.Log.d("PlaceRepository", "      [updatePlaceFromWeb] weatherService.getPlaceDataOWM() returned (async)")
        } catch (e: Exception) {
            android.util.Log.e("PlaceRepository", "      💥 [updatePlaceFromWeb] Exception for placeId=$placeId: ${e.message}", e)
            callback.onError(RequestStatus.UNKNOWN_ERROR)
        }
    }

    suspend fun fetchAndAddNewPlaceFromWeb(placeGeolocation: Geolocation, callback: FetchDataCallback) {
        val partialPlace = Place.newBuilder()
            .setGeolocation(placeGeolocation)
            .buildPartial()

        val innerCallback = object : FetchDataCallback {
            override suspend fun onSuccess(place: Place) {
                addPlace(place)
                callback.onSuccess(place)
            }

            override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                addPlace(place)
                callback.onPartialSuccess(place, requestStatus)
            }

            override suspend fun onError(status: RequestStatus) {
                callback.onError(status)
            }
        }

        weatherService.getPlaceDataOWM(
            partialPlace,
            innerCallback)
    }

    fun fetchLocationDetails(
        query: String,
        callback: FetchCallback<List<Geolocation>>
    ) {
        placeSearchingService.fetchLocationDetails(query, callback)
    }
}