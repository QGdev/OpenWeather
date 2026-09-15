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

package fr.qgdev.openweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.remote.FetchCallback
import fr.qgdev.openweather.data.remote.FetchDataCallback
import fr.qgdev.openweather.data.remote.RequestStatus
import fr.qgdev.openweather.data.repositories.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaceViewModel(
    private val placeRepository: PlaceRepository
    ) : ViewModel() {

    private val _placesState = MutableStateFlow<List<Place>?>(null)
    val placesState: StateFlow<List<Place>?> = _placesState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    //  The place being downloaded after a search, so the list can show it arriving.
    private val _pendingPlaceName = MutableStateFlow<String?>(null)
    val pendingPlaceName: StateFlow<String?> = _pendingPlaceName.asStateFlow()

    private val _addPlaceFailure = MutableStateFlow<RequestStatus?>(null)
    val addPlaceFailure: StateFlow<RequestStatus?> = _addPlaceFailure.asStateFlow()

    //  A refresh used to end in silence whatever happened: updateAllPlacesFromWeb already counted
    //  its successes and failures, and refreshAllPlaces threw the pair away. A place whose update
    //  failed kept showing its old values with nothing to say so.
    private val _lastRefreshOutcome = MutableStateFlow<RefreshOutcome?>(null)
    val lastRefreshOutcome: StateFlow<RefreshOutcome?> = _lastRefreshOutcome.asStateFlow()

    /** Clears the outcome once the UI has shown it. */
    fun acknowledgeRefreshOutcome() {
        _lastRefreshOutcome.value = null
    }

    //  Which place the detail screen is showing, held as the place's own identity rather than its
    //  position: the list can be reordered or refreshed while the detail screen is open, and an
    //  index would then point at a different city.
    private val _selectedPlaceKey = MutableStateFlow<String?>(null)

    /**
     * The selected place, re-read from the list on every update, so the detail screen follows a
     * refresh instead of showing the snapshot taken when it opened.
     */
    val selectedPlace: StateFlow<Place?> =
        combine(_placesState, _selectedPlaceKey) { places, key ->
            if (key == null) null else places?.firstOrNull { it.identityKey == key }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun selectPlace(place: Place) {
        _selectedPlaceKey.value = place.identityKey
    }

    fun clearSelectedPlace() {
        _selectedPlaceKey.value = null
    }

    init {
        viewModelScope.launch {
            placeRepository.placesFlow.collect { places ->
                _placesState.value = places
            }
        }
    }

    fun seachPlaces(
        query: String,
        callback: FetchCallback<List<Geolocation>>
    ) {
        viewModelScope.launch {
            placeRepository.fetchLocationDetails(query, callback)
        }
    }

    fun addPlace(place: Place) {
        viewModelScope.launch {
            placeRepository.addPlace(place)
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.deletePlace(place)
        }
    }

    fun updatePlace(place: Place) {
        viewModelScope.launch {
            placeRepository.updatePlace(place)
        }
    }

    fun restorePlace(place: Place, index: Int) {
        viewModelScope.launch {
            placeRepository.addPlaceAt(index, place)
        }
    }

    /**
     * Persists a drag-to-reorder.
     *
     * The list reorders optimistically while the finger is down; this is called once on drop, so a
     * single move is written rather than one per crossed item.
     */
    fun movePlace(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            placeRepository.movePlace(fromIndex, toIndex)
        }
    }

    fun fetchAndAddNewPlaceFromWeb(
        placeGeolocation: Geolocation,
        callback: FetchDataCallback
    ) {
        viewModelScope.launch {
            placeRepository.fetchAndAddNewPlaceFromWeb(placeGeolocation, callback)
        }
    }

    /**
     * Adds a place chosen from a search, showing it as pending until its forecast arrives.
     *
     * The dialog closes on the choice, because waiting inside it would hold the user in a modal for
     * a network round trip. That used to mean the choice vanished with nothing to show for it: the
     * place appeared only once its download finished, and never at all when the download failed.
     * The list now carries a pending card, and a failure is reported rather than swallowed.
     */
    fun addPlaceFromSearch(placeGeolocation: Geolocation) {
        _pendingPlaceName.value = placeGeolocation.city

        viewModelScope.launch {
            placeRepository.fetchAndAddNewPlaceFromWeb(placeGeolocation, object : FetchDataCallback {
                override suspend fun onSuccess(place: Place) {
                    _pendingPlaceName.value = null
                }

                //  The weather arrived and only the air quality did not: the place is usable, and
                //  the card will say what is missing.
                override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                    _pendingPlaceName.value = null
                }

                override suspend fun onError(requestStatus: RequestStatus) {
                    _pendingPlaceName.value = null
                    _addPlaceFailure.value = requestStatus
                }
            })
        }
    }

    /**
     * Replaces [oldPlace] with a place chosen from a search, showing the new one as pending while
     * it downloads, as an addition does.
     */
    fun replacePlaceFromSearch(oldPlace: Place, placeGeolocation: Geolocation) {
        _pendingPlaceName.value = placeGeolocation.city

        viewModelScope.launch {
            placeRepository.fetchAndReplacePlaceFromWeb(oldPlace, placeGeolocation, object : FetchDataCallback {
                override suspend fun onSuccess(place: Place) {
                    _pendingPlaceName.value = null
                }

                override suspend fun onPartialSuccess(place: Place, requestStatus: RequestStatus) {
                    _pendingPlaceName.value = null
                }

                override suspend fun onError(requestStatus: RequestStatus) {
                    _pendingPlaceName.value = null
                    _addPlaceFailure.value = requestStatus
                }
            })
        }
    }

    fun acknowledgeAddPlaceFailure() {
        _addPlaceFailure.value = null
    }

    fun thereIsNoPlaceRegistered(): Boolean {
        return _placesState.value?.isEmpty() ?: false
    }

    fun refreshAllPlaces() {
        //  Set before launching, not inside it. Inside, the flag only flips once the coroutine is
        //  dispatched - a frame or more after the gesture is released - and in that gap the
        //  pull-to-refresh indicator has already begun animating back to rest, so the drop is seen
        //  rolling back before the spinner replaces it.
        if (_isRefreshing.value) return
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                val (succeeded, failed) = placeRepository.updateAllPlacesFromWeb()
                _lastRefreshOutcome.value = RefreshOutcome(
                    succeeded = succeeded,
                    failed = failed,
                    finishedAt = System.currentTimeMillis()
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

/**
 * A place's identity, stable across refreshes and reorderings.
 *
 * Matches the duplicate check in `PlaceRepository`, which compares name, country and coordinates.
 * The region is deliberately left out of both: it arrived later, and places stored before it exists
 * carry none.
 */
val Geolocation.identityKey: String
    get() = "$city|$countryCode|${coordinates.latitude}|${coordinates.longitude}"

/** The same identity, reached through the place that holds it. */
val Place.identityKey: String
    get() = geolocation.identityKey

/**
 * How the last refresh went, for the UI to report.
 *
 * [failed] counts places whose weather could not be fetched at all. A place whose weather arrived
 * but whose air quality did not counts as succeeded: it has something new to show.
 */
data class RefreshOutcome(
    val succeeded: Int,
    val failed: Int,
    val finishedAt: Long
)