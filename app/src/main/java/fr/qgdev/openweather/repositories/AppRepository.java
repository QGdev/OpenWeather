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

public class AppRepository {
    
    private final SettingsManager settingsManager;
    
    private final WeatherService weatherService;
    private final PlaceDatabase placeDatabase;
    private final PlaceDAO mPlaceDao;
    
    private final FormattingService formattingService;
    
    private final MutableLiveData<List<Place>> mutableLiveDataPlaces;
    private RepositoryCallback repositoryCallback;
    
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
    
    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }
    
    public FormattingService getFormattingService() {
        return formattingService;
    }
    
    public LiveData<List<Place>> getPlacesLiveData() {
        return mutableLiveDataPlaces;
    }
    
    public List<Place> getPlaces() {
        return placeDatabase.getAllPlaces();
    }
    
    public synchronized void insert(Place place) {
        placeDatabase.insertPlace(place, p -> {
            repositoryCallback.onPlaceInsertion(p.getProperties().getOrder());
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.add(p);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
    public synchronized void delete(Place place) {
        placeDatabase.deletePlace(place, position -> {
            repositoryCallback.onPlaceDeletion(position);
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.remove(place);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
    public synchronized void update(Place place) {
        placeDatabase.updatePlace(place, (Place p) -> {
            repositoryCallback.onPlaceUpdate(p.getProperties().getOrder());
            List<Place> places = mutableLiveDataPlaces.getValue();
            if (places == null) return;
            
            places.set(p.getProperties().getOrder(), p);
            mutableLiveDataPlaces.postValue(places);
        });
    }
    
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
    
    public void findPlaceAndAdd(String cityName, String countryCode, FetchDataCallback fetchCallback) {
        
        FetchDataCallback fetchCallbackInternal = new FetchDataCallback() {
            @Override
            public void onSuccess(Place place) {
                
                Geolocation placeGeo = place.getGeolocation();
                
                LiveData<List<Geolocation>> result = placeDatabase.geolocationDAO()
                        .getSimilarPlace(placeGeo.getCity(), placeGeo.getCountryCode());
                
                result.observeForever(new Observer<List<Geolocation>>() {
                    @Override
                    public void onChanged(List<Geolocation> geolocations) {
                        
                        if (geolocations == null) return;
                        
                        //TODO: Implementation of a coordinates checker in order to know if this place is not registered under an other city/country
                        if (!geolocations.isEmpty()) {
                            fetchCallback.onError(RequestStatus.ALREADY_PRESENT);
                            return;
                        }
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
        
        Observer<List<Place>> observerUpdater = new Observer<List<Place>>() {
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
    
    public LiveData<Integer> countPlaces() {
        return mPlaceDao.getPlacesCountLiveData();
    }
    
    public boolean isApiKeyValid() {
        return weatherService.isApiKeyValid();
    }
    
    public boolean isAPIKeyRegistered() {
        return weatherService.isApiKeyRegistered();
    }
    
    public boolean isAPIKeyValid() {
        return weatherService.isApiKeyValid();
    }
    
    public void attachCallbacks(@NonNull RepositoryCallback repositoryCallback) {
        this.repositoryCallback = repositoryCallback;
    }
    
    public void detachCallbacks() {
        repositoryCallback = null;
    }
    
    public enum RepositoryActionType {
        DELETION,
        INSERTION,
        UPDATE,
        MOVED
    }
    
    public interface RepositoryCallback {
        void onPlaceDeletion(int position);
        
        void onPlaceInsertion(int position);
        
        void onPlaceUpdate(int position);
        
        void onMovedPlace(int initialPosition, int finalPosition);
    }
    
    public static class RepositoryAction<T> {
        private final RepositoryActionType type;
        private final T data;
        
        public RepositoryAction(RepositoryActionType type, T data) {
            this.type = type;
            this.data = data;
        }
        
        public RepositoryActionType getType() {
            return type;
        }
        
        public T getData() {
            return data;
        }
    }
}
