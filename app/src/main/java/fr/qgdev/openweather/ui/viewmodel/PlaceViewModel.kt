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
import fr.qgdev.openweather.data.repositories.PlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun fetchAndAddNewPlaceFromWeb(
        placeGeolocation: Geolocation,
        callback: FetchDataCallback
    ) {
        viewModelScope.launch {
            placeRepository.fetchAndAddNewPlaceFromWeb(placeGeolocation, callback)
        }
    }

    fun thereIsNoPlaceRegistered(): Boolean {
        return _placesState.value?.isEmpty() ?: false
    }

    fun refreshAllPlaces() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                placeRepository.updateAllPlacesFromWeb()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}