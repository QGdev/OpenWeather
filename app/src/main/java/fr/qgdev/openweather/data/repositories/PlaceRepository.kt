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

import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
import fr.qgdev.openweather.ui.viewmodel.identityKey
import fr.qgdev.openweather.widgets.WidgetsManager
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

/** Outcome of an attempt to store a place. */
enum class AddPlaceResult {
    ADDED,

    /** A place with the same geolocation is already stored. */
    ALREADY_PRESENT,

    /** The storage already holds [MAX_PLACES] places. */
    STORAGE_FULL
}

/** Upper bound on stored places; ids are allocated modulo this value. */
internal const val MAX_PLACES = 100

private const val TAG = "PlaceRepository"

class PlaceRepository private constructor(context: Context) {
    private val weatherService = WeatherService.getInstance(context)
    private val placeSearchingService = PlaceSearchingService.getInstance(context)
    private val dataStore = PlaceDataStore.getDataStore(context.applicationContext)
    private val applicationContext = context.applicationContext

    //  What stopped the last refresh of every place, when it concerns them all - a refused key, no
    //  network, the quota - for the list to say once in a banner rather than on each card. Held
    //  here, not in a ViewModel, so a refresh run by the periodic worker reports it too.
    private val _refreshProblem = MutableStateFlow<RefreshProblem?>(null)
    val refreshProblem: StateFlow<RefreshProblem?> = _refreshProblem.asStateFlow()

    /** Forgets the last refresh problem - the key it was about has just been changed. */
    fun clearRefreshProblem() {
        _refreshProblem.value = null
    }

    //  How many places a refresh is fetching right now, whoever started it; 0 when none runs.
    private val _placesRefreshing = MutableStateFlow(0)
    val placesRefreshing: StateFlow<Int> = _placesRefreshing.asStateFlow()

    val placesFlow: Flow<List<Place>> = dataStore.data
        .catch {exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading placeStorage.", exception)
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
        //  Every refresh ends here, so this is where the widgets showing this place learn of it:
        //  they change when their data does, whatever path refreshed it.
        WidgetsManager.getInstance(applicationContext)
            .updateWidgetsForPlace(applicationContext, newPlace.identityKey)
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
     * @param onProgress told how many places are done, out of how many, each time one finishes -
     * whether it succeeded or not - so a refresh can be counted down as it runs.
     * @return Pair(successCount, errorCount)
     */
    suspend fun updateAllPlacesFromWeb(
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): Pair<Int, Int> {
        
        val placeStorage = dataStore.data.first()
        val keys = placeStorage.orderedPlaceKeysList.toList()
        
        if (keys.isEmpty()) {
            return Pair(0, 0)
        }

        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        val doneCount = AtomicInteger(0)
        val failures = ConcurrentLinkedQueue<RequestStatus>()
        onProgress(0, keys.size)
        _placesRefreshing.value = keys.size

        // Launch all updates concurrently using async within coroutineScope
        return coroutineScope {
            val jobs = keys.map { placeId ->
                async {
                    
                    //  Only the first outcome of a place counts: a callback arriving after the
                    //  timeout, which already counted an error, must not count it a second time.
                    val deferred = CompletableDeferred<Unit>()
                    try {
                        updatePlaceFromWeb(placeId, object : FetchDataCallback {
                            override suspend fun onSuccess(place: Place) {
                                if (deferred.complete(Unit)) successCount.incrementAndGet()
                            }
                            override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                                if (deferred.complete(Unit)) successCount.incrementAndGet()
                            }
                            override suspend fun onError(requestStatus: RequestStatus) {
                                if (deferred.complete(Unit)) {
                                    failures.add(requestStatus)
                                    errorCount.incrementAndGet()
                                }
                            }
                        })
                        
                        // Wait for callback to complete with 15 second timeout
                        val result = withTimeoutOrNull(15000) {
                            deferred.await()
                        }
                        
                        if (result == null && deferred.complete(Unit)) {
                            Log.e(TAG, "Place $placeId: no answer after 15 seconds")
                            errorCount.incrementAndGet()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update place $placeId: ${e.message}", e)
                        if (deferred.complete(Unit)) errorCount.incrementAndGet()
                    }
                    onProgress(doneCount.incrementAndGet(), keys.size)
                }
            }

            try {
                jobs.awaitAll()
            } finally {
                _placesRefreshing.value = 0
            }
            _refreshProblem.value = RefreshProblem.of(failures, System.currentTimeMillis())
            
            Pair(successCount.get(), errorCount.get())
        }
    }

    //  Remote part, fetch from web
    //  ASYNC TASK
    suspend fun updatePlaceFromWeb(placeId: Int, callback: FetchDataCallback) {
        
        try {
            val place = getPlace(placeId)
            
            if (place == null) {
                callback.onError(RequestStatus.NOT_FOUND)
                return
            }
            
            val innerCallback = object : FetchDataCallback {
                override suspend fun onSuccess(place: Place) {
                    updatePlace(placeId, place)
                    callback.onSuccess(place)
                }

                override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                    updatePlace(placeId, place)
                    callback.onPartialSuccess(place, requestStatus)
                }

                override suspend fun onError(requestStatus: RequestStatus) {
                    callback.onError(requestStatus)
                }
            }

            weatherService.getPlaceDataOWM(place, innerCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch place $placeId: ${e.message}", e)
            callback.onError(RequestStatus.UNKNOWN_ERROR)
        }
    }

    /**
     * Downloads a place chosen from a search and stores it in place of [oldPlace]: same position in
     * the list, and the widgets that showed the old one follow it to the new one.
     *
     * For a town stored twice under nearby coordinates - a boundary and a centre, say - where the
     * user would rather swap the one they have than keep both.
     */
    suspend fun fetchAndReplacePlaceFromWeb(oldPlace: Place, placeGeolocation: Geolocation, callback: FetchDataCallback) {
        val partialPlace = Place.newBuilder()
            .setGeolocation(placeGeolocation)
            .buildPartial()

        val innerCallback = object : FetchDataCallback {
            override suspend fun onSuccess(place: Place) {
                if (replacePlace(oldPlace, place)) callback.onSuccess(place)
                else callback.onError(RequestStatus.UNKNOWN_ERROR)
            }

            override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                if (replacePlace(oldPlace, place)) callback.onPartialSuccess(place, requestStatus)
                else callback.onError(RequestStatus.UNKNOWN_ERROR)
            }

            override suspend fun onError(status: RequestStatus) {
                callback.onError(status)
            }
        }

        weatherService.getPlaceDataOWM(partialPlace, innerCallback)
    }

    /** Stores [newPlace] under [oldPlace]'s key, then points the old place's widgets at it. */
    private suspend fun replacePlace(oldPlace: Place, newPlace: Place): Boolean {
        val placeId = retrievePlaceKey(oldPlace)
        if (placeId == -1) return false

        val stampedPlace = newPlace.toBuilder()
            .setProperties(newPlace.properties.toBuilder().setCreationTime(System.currentTimeMillis()))
            .build()
        dataStore.updateData { placeStorage ->
            placeStorage.toBuilder()
                .putPlaces(placeId, stampedPlace)
                .build()
        }

        val widgetsManager = WidgetsManager.getInstance(applicationContext)
        widgetsManager.repointWidgets(applicationContext, oldPlace.identityKey, stampedPlace.identityKey)
        widgetsManager.updateWidgetsForPlace(applicationContext, stampedPlace.identityKey)
        return true
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

/**
 * A refresh failure that concerns every place rather than one: worth a banner over the list.
 *
 * @property status the most serious of the failures, by the banners' priority.
 * @property at when the refresh that met it ended.
 */
data class RefreshProblem(val status: RequestStatus, val at: Long) {
    companion object {
        //  A refused key first: nothing works until it is fixed. Then no network, then the quota,
        //  which both pass by themselves. Other failures belong to the places they hit.
        private val BY_PRIORITY = listOf(
            RequestStatus.AUTH_FAILED,
            RequestStatus.NOT_CONNECTED,
            RequestStatus.TOO_MANY_REQUESTS
        )

        /** The problem among [failures] worth a banner, if any: none means the refresh went through. */
        fun of(failures: Collection<RequestStatus>, at: Long): RefreshProblem? =
            BY_PRIORITY.firstOrNull { it in failures }?.let { RefreshProblem(it, at) }
    }
}
