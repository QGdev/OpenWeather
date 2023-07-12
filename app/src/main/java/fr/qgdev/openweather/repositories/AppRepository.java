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

import androidx.annotation.NonNull;
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
 * The type App repository.
 */
public class AppRepository {
    
    private final SettingsManager settingsManager;
    
    private final WeatherService weatherService;
    private final PlaceDatabase placeDatabase;
    private final PlaceDAO mPlaceDao;
    
    private final FormattingService formattingService;
    
    private final MutableLiveData<List<Place>> mutableLiveDataPlaces;
    private RepositoryCallback repositoryCallback;
    
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
        
        mutableLiveDataPlaces = new MutableLiveData<>(null);
        new Thread(() -> mutableLiveDataPlaces.postValue(getPlaces())).start();
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
     * Insert.
     *
     * @param place the place
     */
    public synchronized void insert(Place place) {
        placeDatabase.insertPlace(place, p -> {
            repositoryCallback.onPlaceInsertion(p.getProperties().getOrder());
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.add(p);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
    /**
     * Delete.
     *
     * @param place the place
     */
    public synchronized void delete(Place place) {
        placeDatabase.deletePlace(place, position -> {
            repositoryCallback.onPlaceDeletion(position);
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.remove(place);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
    /**
     * Update.
     *
     * @param place the place
     */
    public synchronized void update(Place place) {
        placeDatabase.updatePlace(place, (Place p) -> {
            repositoryCallback.onPlaceUpdate(p.getProperties().getOrder());
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.set(p.getProperties().getOrder(), p);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
    /**
     * Move place.
     *
     * @param crtOrder the crt order
     * @param newOrder the new order
     */
    public void movePlace(int crtOrder, int newOrder) {
        placeDatabase.movePlace(crtOrder, newOrder);
    }
    
    private int generatePlaceID(List<Integer> ids) {
        Random r = new Random();
        
        int placeID = r.nextInt();
        int attempts = 0;
        
        while (ids.contains(placeID) && attempts < 5) {
            if (r.nextBoolean() && placeID < Integer.MIN_VALUE + 1000) placeID -= r.nextInt(1000);
            else placeID += r.nextInt(1000);
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
                        .getSimilarPlace(placeGeo.getCity(), placeGeo.getCountryCode());
                
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
     * Update all places.
     *
     * @param callback the callback
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
     * Count places live data.
     *
     * @return the live data
     */
    public LiveData<Integer> countPlaces() {
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
     * Attach callbacks.
     *
     * @param repositoryCallback the repository callback
     */
    public void attachCallbacks(@NonNull RepositoryCallback repositoryCallback) {
        this.repositoryCallback = repositoryCallback;
    }
    
    /**
     * Detach callbacks.
     */
    public void detachCallbacks() {
        repositoryCallback = null;
    }
    
    /**
     * The enum Repository action type.
     */
    public enum RepositoryActionType {
        /**
         * Deletion repository action type.
         */
        DELETION,
        /**
         * Insertion repository action type.
         */
        INSERTION,
        /**
         * Update repository action type.
         */
        UPDATE,
        /**
         * Moved repository action type.
         */
        MOVED
    }
    
    /**
     * The interface Repository callback.
     */
    public interface RepositoryCallback {
        /**
         * On place deletion.
         *
         * @param position the position
         */
        void onPlaceDeletion(int position);
        
        /**
         * On place insertion.
         *
         * @param position the position
         */
        void onPlaceInsertion(int position);
        
        /**
         * On place update.
         *
         * @param position the position
         */
        void onPlaceUpdate(int position);
        
        /**
         * On moved place.
         *
         * @param initialPosition the initial position
         * @param finalPosition   the final position
         */
        void onMovedPlace(int initialPosition, int finalPosition);
    }
    
    /**
     * The type Repository action.
     *
     * @param <T> the type parameter
     */
    public static class RepositoryAction<T> {
        private final RepositoryActionType type;
        private final T data;
        
        /**
         * Instantiates a new Repository action.
         *
         * @param type the type
         * @param data the data
         */
        public RepositoryAction(RepositoryActionType type, T data) {
            this.type = type;
            this.data = data;
        }
        
        /**
         * Gets type.
         *
         * @return the type
         */
        public RepositoryActionType getType() {
            return type;
        }
        
        /**
         * Gets data.
         *
         * @return the data
         */
        public T getData() {
            return data;
        }
    }
}
