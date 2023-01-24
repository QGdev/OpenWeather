package fr.qgdev.openweather.fragment.places;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Place;

public class PlacesViewModel extends ViewModel {
	
	private final MutableLiveData<List<Place>> places;
	private final HashMap<Integer, PlaceRecyclerViewAdapter.ViewType> placesViewType;
	private final Queue<AppRepository.RepositoryAction> repositoryActions;
	private boolean dataHasAlreadyBeenUpdated;
	
	
	public PlacesViewModel() {
		dataHasAlreadyBeenUpdated = false;
		places = new MutableLiveData<List<Place>>(new ArrayList<Place>());
		placesViewType = new HashMap<Integer, PlaceRecyclerViewAdapter.ViewType>();
		repositoryActions = new ConcurrentLinkedQueue<>();
	}
	
	public boolean hasDataAlreadyBeenUpdated() {
		return dataHasAlreadyBeenUpdated;
	}
	
	public void dataHasBeenUpdated() {
		dataHasAlreadyBeenUpdated = true;
	}
	
	public LiveData<List<Place>> getPlacesLiveData() {
		return this.places;
	}
	
	public List<Place> getPlaces() {
		return places.getValue();
	}
	
	public void setPlaces(List<Place> placeList) {
		
		//  Nothing to check
		if (placeList == null) return;
		
		//  Remove old values
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
	
	public PlaceRecyclerViewAdapter.ViewType getPlaceViewType(Integer placeID) {
		return placesViewType.getOrDefault(placeID, null);
	}
	
	public PlaceRecyclerViewAdapter.ViewType getPlaceViewTypeFromPosition(Integer position) {
		if (places.getValue() == null) return null;
		int placeID = places.getValue().get(position).getProperties().getPlaceId();
		return placesViewType.getOrDefault(placeID, null);
	}
	
	public void setPlaceViewType(Integer placeID, PlaceRecyclerViewAdapter.ViewType newViewType) {
		if (placesViewType.getOrDefault(placeID, null) == null) {
			placesViewType.put(placeID, newViewType);
		} else {
			placesViewType.replace(placeID, newViewType);
		}
	}
	
	public synchronized void addRepositoryPlaceAction(@NonNull AppRepository.RepositoryAction action) {
		repositoryActions.add(action);
	}
	
	public synchronized AppRepository.RepositoryAction pollRepositoryPlaceAction() {
		return repositoryActions.poll();
	}
	
	public boolean isRepositoryPlaceActionEmpty() {
		return repositoryActions.isEmpty();
	}
}