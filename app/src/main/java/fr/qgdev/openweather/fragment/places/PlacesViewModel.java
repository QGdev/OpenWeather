/*
 *  Copyright (c) 2019 - 2023
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

package fr.qgdev.openweather.fragment.places;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * The type Places view model.
 */
public class PlacesViewModel extends ViewModel {
	
	private final MutableLiveData<List<Place>> places;
	private final HashMap<Integer, PlaceRecyclerViewAdapter.ViewType> placesViewType;
	private boolean dataHasAlreadyBeenUpdated;
	
	
	/**
	 * Instantiates a new Places view model.
	 */
	public PlacesViewModel() {
		dataHasAlreadyBeenUpdated = false;
		places = new MutableLiveData<>(null);
		placesViewType = new HashMap<>();
	}
	
	/**
	 * Has data already been updated boolean.
	 *
	 * @return the boolean related to the update state of the places
	 */
	public boolean hasDataAlreadyBeenUpdated() {
		return dataHasAlreadyBeenUpdated;
	}
	
	/**
	 * Data has been updated.
	 */
	public void dataHasBeenUpdated() {
		dataHasAlreadyBeenUpdated = true;
	}
	
	/**
	 * Gets places live data.
	 *
	 * @return the places live data
	 */
	public LiveData<List<Place>> getPlacesLiveData() {
		return this.places;
	}
	
	/**
	 * Gets places.
	 *
	 * @return the places
	 */
	public synchronized List<Place> getPlaces() {
		return places.getValue();
	}
	
	/**
	 * Sets places.
	 *
	 * @param placeList the place list
	 */
	public synchronized void setPlaces(List<Place> placeList) {
		
		//		Nothing to check
		if (placeList == null) return;
		
		//		Remove old values
		//		Remove old ViewStates from removed places
		//		Add new ViewStates from added places
		if (!placesViewType.isEmpty()) {
			List<Integer> placeIds = placeList.stream().map(place -> place.getProperties().getPlaceId()).collect(Collectors.toList());
			Integer[] contained = placesViewType.keySet().toArray(new Integer[0]);
			
			for (Integer element : contained) {
				if (!placeIds.contains(element)) {
					placesViewType.remove(element);
				}
			}
		}
		
		if (!placeList.isEmpty()) {
			for (Place place : placeList) {
				placesViewType.putIfAbsent(place.getProperties().getPlaceId(),
						  PlaceRecyclerViewAdapter.ViewType.COMPACT);
			}
		}
		
		places.postValue(placeList);
	}
	
	/**
	 * Gets place view type.
	 *
	 * @param placeID the place id
	 * @return the place view type
	 */
	public PlaceRecyclerViewAdapter.ViewType getPlaceViewType(Integer placeID) {
		return placesViewType.getOrDefault(placeID, PlaceRecyclerViewAdapter.ViewType.UNDEFINED);
	}
	
	/**
	 * Gets place view type from position.
	 *
	 * @param position the position
	 * @return the place view type from position
	 */
	public PlaceRecyclerViewAdapter.ViewType getPlaceViewTypeFromPosition(Integer position) {
		if (places.getValue() == null) return null;
		int placeID = places.getValue().get(position).getProperties().getPlaceId();
		return placesViewType.getOrDefault(placeID, PlaceRecyclerViewAdapter.ViewType.UNDEFINED);
	}
	
	/**
	 * Sets place view type.
	 *
	 * @param placeID     the place id
	 * @param newViewType the new view type
	 */
	public void setPlaceViewType(Integer placeID, PlaceRecyclerViewAdapter.ViewType newViewType) {
		if (placesViewType.getOrDefault(placeID, null) == null) {
			placesViewType.put(placeID, newViewType);
		} else {
			placesViewType.replace(placeID, newViewType);
		}
	}
}