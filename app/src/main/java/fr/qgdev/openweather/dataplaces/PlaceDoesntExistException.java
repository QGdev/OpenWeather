package fr.qgdev.openweather.dataplaces;

//  A simple class to identify exception due to a place doesn't exist in storage
public class PlaceDoesntExistException extends Exception {

	public PlaceDoesntExistException() {
		super("Cannot save a place data that isn\'t present in storage");
	}
}