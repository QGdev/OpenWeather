package fr.qgdev.openweather.repositories.weather;

import fr.qgdev.openweather.repositories.places.Coordinates;

public interface SearchPlaceCallback {
	void onPlaceFound(Coordinates coordinates);
	
	void onError(RequestStatus requestStatus);
}
