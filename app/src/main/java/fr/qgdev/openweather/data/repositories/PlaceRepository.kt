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

/**
 * A place's identity, stable across refreshes and reorderings.
 *
 * Matches the duplicate check in `PlaceRepository`, which compares name, country and coordinates.
 * The region is deliberately left out of both: it arrived later, and places stored before it exists
 * carry none. The country code is upper-cased, as a place found by a search and the same place
 * stored by another version do not always spell it the same way.
 */
val Geolocation.identityKey: String
    get() = "$city|${countryCode.uppercase()}|${coordinates.latitude}|${coordinates.longitude}"

/** The same identity, reached through the place that holds it. */
val Place.identityKey: String
    get() = geolocation.identityKey

/** Outcome of an attempt to store a place. */
enum class AddPlaceResult {
    ADDED,

    /** A place with the same geolocation is already stored. */
    ALREADY_PRESENT,

    /** The storage already holds [MAX_PLACES] places. */
    STORAGE_FULL
}

/** Upper bound on stored places; ids are allocated modulo this value. */
private const val MAX_PLACES = 100

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

    //  first(), not single(): DataStore's flow never completes, so single() waits forever for a
    //  completion that never arrives. Neither of these was called, so the hang was latent.
    suspend fun isPlaceStorageEmpty(): Boolean {
        return dataStore.data.first().placesCount == 0
    }

    suspend fun isPlaceStorageFull(): Boolean {
        return dataStore.data.first().placesCount >= MAX_PLACES
    }

    /**
     * The next free slot, or null when storage is full.
     *
     * Deliberately an extension on the storage rather than a suspending read of its own, so callers
     * resolve it *inside* an [DataStore.updateData] block. Reading the storage, returning an id and
     * then opening a transaction let two concurrent adds pick the same id, and the second silently
     * overwrote the first.
     */
    private fun PlaceStorage.nextFreeId(): Int? =
        if (placesCount >= MAX_PLACES) null
        else (0 until MAX_PLACES).firstNotNullOfOrNull { offset ->
            ((lastKeyUsed + 1 + offset) % MAX_PLACES).takeIf { !placesMap.containsKey(it) }
        }

    /**
     * Whether two search results or stored places are the same location.
     *
     * Compares what identifies a place - its name, its country and its coordinates - rather than
     * the whole message. The region arrived later and is absent from everything stored before it,
     * so comparing the message itself would let the same city be added twice.
     */
    private fun Geolocation.sameLocationAs(other: Geolocation): Boolean =
        city == other.city &&
                countryCode == other.countryCode &&
                coordinates == other.coordinates

    private fun PlaceStorage.holds(geolocation: Geolocation): Boolean =
        placesMap.values.any { it.geolocation.sameLocationAs(geolocation) }

    private suspend fun retrievePlaceKey(place: Place): Int {
        val placeStorage = dataStore.data.first()
        for (placeKey in placeStorage.orderedPlaceKeysList) {
            val storedPlace = placeStorage.getPlacesOrDefault(placeKey, null)
            if (storedPlace != null && storedPlace.geolocation.sameLocationAs(place.geolocation)) {
                return placeKey
            }
        }
        return -1
    }

    /**
     * Stores [place], at [index] in the display order when given and at the end otherwise.
     *
     * One function rather than two so the creation-time stamp cannot go missing from one of them,
     * which is what left restored places with `creationTime = 0`.
     *
     * Everything happens inside the [DataStore.updateData] block: the duplicate check, the slot
     * search and the write. updateData is serialised per store and retries on conflict, so two
     * concurrent adds can no longer read the same state and choose the same id.
     */
    suspend fun addPlaceAt(index: Int?, place: Place): AddPlaceResult {
        var result = AddPlaceResult.ADDED

        dataStore.updateData { placeStorage ->
            if (placeStorage.holds(place.geolocation)) {
                result = AddPlaceResult.ALREADY_PRESENT
                return@updateData placeStorage
            }

            val placeId = placeStorage.nextFreeId()
            if (placeId == null) {
                result = AddPlaceResult.STORAGE_FULL
                return@updateData placeStorage
            }

            result = AddPlaceResult.ADDED

            //  Stamped here rather than by the caller, so it is recorded when the place is really
            //  stored and not when the fetch that produced it started.
            val stampedPlace = place.toBuilder()
                .setProperties(
                    place.properties.toBuilder()
                        .setCreationTime(System.currentTimeMillis())
                )
                .build()

            val orderedKeys = placeStorage.orderedPlaceKeysList.toMutableList()
            if (index != null && index in 0..orderedKeys.size) {
                orderedKeys.add(index, placeId)
            } else {
                orderedKeys.add(placeId)
            }

            //  placeKeys is not written: nothing reads it - retrievePlaceKey scans the places map
            //  instead - and keying it on Geolocation.hashCode() would collide silently.
            placeStorage.toBuilder()
                .putPlaces(placeId, stampedPlace)
                .setLastKeyUsed(placeId)
                .clearOrderedPlaceKeys()
                .addAllOrderedPlaceKeys(orderedKeys)
                .build()
        }

        if (result != AddPlaceResult.ADDED) {
            Log.w("PlaceRepository", "Place not added: $result")
        }
        return result
    }

    suspend fun addPlace(place: Place): AddPlaceResult = addPlaceAt(null, place)

    /**
     * Moves the place at [fromIndex] to [toIndex] in the display order.
     *
     * Only the ordered key list is rewritten; the places themselves are untouched. Indices outside
     * the list are ignored rather than throwing, because they can arrive from a drag that races a
     * concurrent delete.
     */
    suspend fun movePlace(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        dataStore.updateData { placeStorage ->
            val orderedKeys = placeStorage.orderedPlaceKeysList.toMutableList()
            if (fromIndex !in orderedKeys.indices || toIndex !in orderedKeys.indices) {
                return@updateData placeStorage
            }

            orderedKeys.add(toIndex, orderedKeys.removeAt(fromIndex))

            placeStorage.toBuilder()
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

        //  A refused add is reported to the caller rather than swallowed. Adding a city that was
        //  already in the list used to appear to succeed while storing nothing, and the dialog
        //  closed as if it had worked.
        val innerCallback = object : FetchDataCallback {
            override suspend fun onSuccess(place: Place) {
                when (addPlace(place)) {
                    AddPlaceResult.ADDED -> callback.onSuccess(place)
                    AddPlaceResult.ALREADY_PRESENT -> callback.onError(RequestStatus.ALREADY_PRESENT)
                    AddPlaceResult.STORAGE_FULL -> callback.onError(RequestStatus.UNKNOWN_ERROR)
                }
            }

            override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                when (addPlace(place)) {
                    AddPlaceResult.ADDED -> callback.onPartialSuccess(place, requestStatus)
                    AddPlaceResult.ALREADY_PRESENT -> callback.onError(RequestStatus.ALREADY_PRESENT)
                    AddPlaceResult.STORAGE_FULL -> callback.onError(RequestStatus.UNKNOWN_ERROR)
                }
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