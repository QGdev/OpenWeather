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

package fr.qgdev.openweather.repositories;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.Random;

import fr.qgdev.openweather.repositories.places.Geolocation;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.repositories.places.PlaceDatabase;
import fr.qgdev.openweather.repositories.places.dao.PlaceDAO;
import fr.qgdev.openweather.repositories.settings.SettingsManager;
import fr.qgdev.openweather.repositories.weather.FetchCallback;
import fr.qgdev.openweather.repositories.weather.FetchDataCallback;
import fr.qgdev.openweather.repositories.weather.RequestStatus;
import fr.qgdev.openweather.repositories.weather.WeatherService;

/**
 * AppRepository class
 *
 * <p>
 * AppRepository is the main structure of the app.
 * It contains all the services and the database.
 * Will handle the link between storage and Web data.
 * </p>
 */
public class AppRepository {
	
	private final SettingsManager settingsManager;
	
	private final WeatherService weatherService;
	private final PlaceDatabase placeDatabase;
	private final PlaceDAO mPlaceDao;
	
	private final FormattingService formattingService;
	
	private final MutableLiveData<List<Place>> mutableLiveDataPlaces;
	
	/**
	 * Instantiates a new App repository.
	 *
	 * @param context the context
	 */
	public AppRepository(Context context) {
		settingsManager = new SettingsManager(context);
		weatherService = new WeatherService(context, settingsManager);
		formattingService = new FormattingService(context, settingsManager);
		
		PlaceDatabase db = PlaceDatabase.getDatabase(context);
		placeDatabase = db;
		mPlaceDao = db.placeDAO();
		
		mutableLiveDataPlaces = (MutableLiveData<List<Place>>) placeDatabase.getAllPlacesLiveData();
	}
	
	/**
	 * Gets settings manager.
	 *
	 * @return the settings manager
	 */
	public SettingsManager getSettingsManager() {
		return this.settingsManager;
	}
	
	/**
	 * Gets formatting service.
	 *
	 * @return the formatting service
	 */
	public FormattingService getFormattingService() {
		return formattingService;
	}
	
	/**
	 * Gets places live data.
	 *
	 * @return the places live data
	 */
	public LiveData<List<Place>> getPlacesLiveData() {
		return mutableLiveDataPlaces;
	}
	
	/**
	 * Gets places.
	 *
	 * @return the places
	 */
	public List<Place> getPlaces() {
		return placeDatabase.getAllPlaces();
	}
	
	/**
	 * Gets place live data from place id.
	 *
	 * @param placeId the place id
	 * @return the place live data
	 */
	public LiveData<Place> getPlaceFromPlaceIdLiveData(int placeId) {
		return placeDatabase.getPlaceFromPlaceIdLiveData(placeId);
	}
	
	/**
	 * Gets basic listing of registered places.
	 *
	 * @return the basic listing live data
	 */
	public LiveData<List<Geolocation>> getBasicListingLiveData() {
		return mPlaceDao.getBasicListing();
	}
	
	/**
	 * Insert a new place.
	 *
	 * @param place the place to insert
	 */
	public synchronized void insert(Place place) {
		placeDatabase.insertPlace(place, p -> {
			List<Place> places = mutableLiveDataPlaces.getValue();
			if (places == null) return;
			
			places.add(p);
			mutableLiveDataPlaces.postValue(places);
		});
	}
	
	/**
	 * Delete an existing place.
	 *
	 * @param place the place to delete
	 */
	public synchronized void delete(Place place) {
		placeDatabase.deletePlace(place, position -> {
			List<Place> places = mutableLiveDataPlaces.getValue();
			if (places == null) return;
			
			places.remove(place);
			mutableLiveDataPlaces.postValue(places);
		});
	}
	
	/**
	 * Update an existing place.
	 *
	 * @param place the place to update
	 */
	public synchronized void update(Place place) {
		placeDatabase.updatePlace(place, (Place p) -> {
			List<Place> places = mutableLiveDataPlaces.getValue();
			if (places == null) return;
			if (places.size() <= p.getProperties().getOrder()) return;
			
			places.set(p.getProperties().getOrder(), p);
			mutableLiveDataPlaces.postValue(places);
		});
	}
	
	/**
	 * Move place an existing place from crtOrder to newOrder.
	 *
	 * @param crtOrder the crt order
	 * @param newOrder the new order
	 */
	public void movePlace(int crtOrder, int newOrder) {
		placeDatabase.movePlace(crtOrder, newOrder);
	}
	
	/**
	 * Generate a random place id that is not already used.
	 * Check if the id is not 0 or already contained in the
	 * provided list.
	 *
	 * @param ids The list of ids of existing ids
	 * @return A unique place id
	 */
	private int generatePlaceID(List<Integer> ids) {
		Random r = new Random();
		
		int placeID = r.nextInt();
		int attempts = 0;
		
		while ((ids.contains(placeID) || placeID == 0) && attempts < 5) {
			if (r.nextBoolean() && placeID < Integer.MIN_VALUE + 10) placeID -= r.nextInt(10);
			else placeID += r.nextInt(10);
			attempts++;
		}
		
		return placeID;
	}
	
	/**
	 * Find place and add.
	 *
	 * @param cityName      the city name
	 * @param countryCode   the country code
	 * @param fetchCallback the fetch callback
	 */
	public void findPlaceAndAdd(String cityName, String countryCode, FetchDataCallback fetchCallback) {
		
		FetchDataCallback fetchCallbackInternal = new FetchDataCallback() {
			@Override
			public void onSuccess(Place place) {
				
				Geolocation placeGeo = place.getGeolocation();
				
				LiveData<List<Geolocation>> result = placeDatabase.geolocationDAO()
						  .getSimilarPlaces(placeGeo.getCity(), placeGeo.getCountryCode());
				
				result.observeForever(new Observer<>() {
					@Override
					public void onChanged(List<Geolocation> geolocations) {
						
						if (geolocations == null) return;
						
						//TODO: Implementation of a coordinates checker in order to know if this place is not registered under an other city/country
						if (!geolocations.isEmpty()) {
							fetchCallback.onError(RequestStatus.ALREADY_PRESENT);
							return;
						}
						result.removeObserver(this);
						PlaceDatabase.databaseWriteExecutor.execute(() -> insert(place));
						fetchCallback.onSuccess(place);
					}
				});
			}
			
			@Override
			public void onPartialSuccess(Place place, RequestStatus requestStatus) {
				PlaceDatabase.databaseWriteExecutor.execute(() -> insert(place));
				fetchCallback.onPartialSuccess(place, requestStatus);
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				fetchCallback.onError(requestStatus);
			}
		};
		
		Runnable runnable = () -> {
			List<Integer> ids = placeDatabase.propertiesDAO().getAllIDs();
			
			int placeID = generatePlaceID(ids);
			
			if (ids.contains(placeID)) {
				fetchCallback.onError(RequestStatus.UNKNOWN_ERROR);
			}
			weatherService.searchAndBuildPlace(placeID, cityName, countryCode, fetchCallbackInternal);
		};
		
		new Thread(runnable).start();
	}
	
	/**
	 * Update all registered places.
	 *
	 * @param callback the callback that will be called when the update is finished
	 */
	public void updateAllPlaces(FetchCallback callback) {
		
		FetchDataCallback fetchDataCallback = new FetchDataCallback() {
			@Override
			public void onSuccess(Place place) {
				PlaceDatabase.databaseWriteExecutor.execute(() -> update(place));
				callback.onSuccess();
			}
			
			@Override
			public void onPartialSuccess(Place place, RequestStatus requestStatus) {
				PlaceDatabase.databaseWriteExecutor.execute(() -> update(place));
				callback.onSuccess();
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				callback.onError(requestStatus);
			}
		};
		
		Observer<List<Place>> observerUpdater = new Observer<>() {
			@Override
			public void onChanged(List<Place> places) {
				if (places == null) return;
				
				for (Place place : places) {
					weatherService.getPlaceDataOWM(place, fetchDataCallback);
				}
				placeDatabase.getAllPlacesLiveData().removeObserver(this);
			}
			
		};
		
		placeDatabase.getAllPlacesLiveData().observeForever(observerUpdater);
	}
	
	/**
	 * Update all registered places.
	 * Acts like updateAllPlaces but the update is done in a synchronized way.
	 * This means that this function using some functions that will block the thread
	 * but the update will be done asynchronously.
	 * Useful when the execution context does not allow to use the observer pattern.
	 *
	 * @param callback the callback
	 */
	public void updateAllPlacesSynchronized(FetchCallback callback) {
		
		FetchDataCallback fetchDataCallback = new FetchDataCallback() {
			@Override
			public void onSuccess(Place place) {
				PlaceDatabase.databaseWriteExecutor.execute(() -> update(place));
				callback.onSuccess();
			}
			
			@Override
			public void onPartialSuccess(Place place, RequestStatus requestStatus) {
				PlaceDatabase.databaseWriteExecutor.execute(() -> update(place));
				callback.onSuccess();
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				callback.onError(requestStatus);
			}
		};
		
		List<Place> places = placeDatabase.getAllPlaces();
		
		for (Place place : places) {
			weatherService.getPlaceDataOWM(place, fetchDataCallback);
		}
	}
	
	/**
	 * Count places registered.
	 *
	 * @return the number of places registered
	 */
	public int countPlaces() {
		return mPlaceDao.getPlacesCount();
	}
	
	
	/**
	 * Count places live data.
	 *
	 * @return the live data
	 */
	public LiveData<Integer> countPlacesLiceData() {
		return mPlaceDao.getPlacesCountLiveData();
	}
	
	/**
	 * Is api key registered boolean.
	 *
	 * @return the boolean
	 */
	public boolean isApiKeyRegistered() {
		return weatherService.isApiKeyRegistered();
	}
	
	/**
	 * Is api key valid boolean.
	 *
	 * @return the boolean
	 */
	public boolean isApiKeyValid() {
		return weatherService.isApiKeyValid();
	}
	
	/**
	 * Will send a broadcast to all widgets to update them.
	 *
	 * @param context the context used to send the broadcast
	 */
	public void updateWidgets(Context context) {
		Intent updateIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
		updateIntent.setPackage("fr.qgdev.openweather");
		context.sendBroadcast(updateIntent, "fr.qgdev.openweather.permission.UPDATE_WIDGET");
	}
}
