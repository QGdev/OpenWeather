package fr.qgdev.openweather.dataplaces;

//  A simple class to identify exception due to a place already present in storage
public class PlaceAlreadyExistException extends Exception {

	public PlaceAlreadyExistException() {
		super("Cannot add a place that already present in storage");
	}
}
