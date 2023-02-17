package fr.qgdev.openweather.fragment.places;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * The type Places view model.
 */
public class PlacesViewModel extends ViewModel {
	
	private final MutableLiveData<List<Place>> places;
	private final HashMap<Integer, PlaceRecyclerViewAdapter.ViewType> placesViewType;
	private final Queue<AppRepository.RepositoryAction> repositoryActions;
	private boolean dataHasAlreadyBeenUpdated;
	
	
	/**
	 * Instantiates a new Places view model.
	 */
	public PlacesViewModel() {
		dataHasAlreadyBeenUpdated = false;
		places = new MutableLiveData<>(null);
		placesViewType = new HashMap<Integer, PlaceRecyclerViewAdapter.ViewType>();
		repositoryActions = new ConcurrentLinkedQueue<>();
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
	public List<Place> getPlaces() {
		return places.getValue();
	}
	
	/**
	 * Sets places.
	 *
	 * @param placeList the place list
	 */
	public void setPlaces(List<Place> placeList) {
		
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
		return placesViewType.getOrDefault(placeID, null);
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
		return placesViewType.getOrDefault(placeID, null);
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
	
	/**
	 * Add repository place action.
	 *
	 * @param action the action
	 */
	public synchronized void addRepositoryPlaceAction(@NonNull AppRepository.RepositoryAction action) {
		repositoryActions.add(action);
	}
	
	/**
	 * Poll repository place action app repository . repository action.
	 *
	 * @return the app repository . repository action
	 */
	public synchronized AppRepository.RepositoryAction pollRepositoryPlaceAction() {
		return repositoryActions.poll();
	}
	
	/**
	 * Is repository place action empty boolean.
	 *
	 * @return the boolean
	 */
	public boolean isRepositoryPlaceActionEmpty() {
		return repositoryActions.isEmpty();
	}
}