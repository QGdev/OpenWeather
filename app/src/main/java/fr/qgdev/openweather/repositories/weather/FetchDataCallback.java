package fr.qgdev.openweather.repositories.weather;

import fr.qgdev.openweather.repositories.places.Place;

public interface FetchDataCallback {
	
	/**
	 * On success of both requests (weather and air quality)
	 *
	 * @param place The resulting place with new data
	 */
	void onSuccess(Place place);
	
	/**
	 * On success of weather request but failure of air quality request
	 *
	 * @param place         The resulting place with new data
	 * @param requestStatus The error cause of the air quality request
	 */
	void onPartialSuccess(Place place, RequestStatus requestStatus);
	
	/**
	 * On failure of weather request
	 *
	 * @param requestStatus The error cause of the weather request
	 */
	void onError(RequestStatus requestStatus);
}
