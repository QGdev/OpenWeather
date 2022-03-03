package fr.qgdev.openweather.dataplaces;

/**
 * PlaceDoesntExistException
 * <p>
 * A simple class to identify exception due to a place doesn't exist in storage
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Exception
 */
public class PlaceDoesntExistException extends Exception {

	/**
	 * PlaceDoesntExistException Constructor
	 * <p>
	 * Just the constructor of PlaceDoesntExistException class
	 * </p>
	 */
	public PlaceDoesntExistException() {
		super("Cannot save a place data that isn't present in storage");
	}
}