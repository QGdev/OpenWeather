package fr.qgdev.openweather.dataplaces;

/**
 * PlaceAlreadyExistException
 * <p>
 * A simple class to identify exception due to a place already present in storage
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Exception
 */
public class PlaceAlreadyExistException extends Exception {

	/**
	 * PlaceAlreadyExistException Constructor
	 * <p>
	 * Just the constructor of PlaceAlreadyExistException class
	 * </p>
	 */
	public PlaceAlreadyExistException() {
		super("Cannot add a place that already present in storage");
	}
}
