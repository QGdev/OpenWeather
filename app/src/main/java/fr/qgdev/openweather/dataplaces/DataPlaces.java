package fr.qgdev.openweather.dataplaces;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import fr.qgdev.openweather.Place;

/**
 * DataPlaces
 * <p>
 * Organise place data with SharedPreferences<br>
 * The organisation of the data is pretty simple :<br>
 * -  A JSONArray contains all places Keys and it is stored at PREFERENCE_LIST_NAME key<br>
 * -  Each place stored in the JSONArray will have a "location" in the SharedPreferences<br>
 * -  Data place is stored on a String form which is a JSON of the place data<br>
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see SharedPreferences
 */
public class DataPlaces implements SharedPreferences {

	private final SharedPreferences sharedPreferences;
	private final String PREFERENCE_FILE_NAME = "places";
	private final String PREFERENCE_LIST_NAME = "places_list";


	/**
	 * DataPlaces Constructor
	 * <p>
	 * Just the constructor of DataPlaces class
	 * </p>
	 *
	 * @param context           Context of the application to initialize SharedPreferences
	 * @apiNote None of the parameters can be null
	 */
	public DataPlaces(@NonNull Context context) {
		this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
	}


	/**
	 *  deletePlace(@NonNull String dataPlaceName) throws PlaceDoesntExistException
	 *  <p>
	 * Used to remove data with dataPlaceName of the place
	 * </p>
	 *
	 * @param dataPlaceName     dataPlaceName of the deleted place
	 * @throws PlaceDoesntExistException    If the place doesn't exist
	 * @apiNote dataPlaceName shouldn't be null
	 * @return A boolean to know if the operation was a success or not
	 */
	public boolean deletePlace(@NonNull String dataPlaceName) throws PlaceDoesntExistException {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		//  The place must exist in storage, if it is not the case we throw an exception
		if (!this.contains(dataPlaceName)) throw new PlaceDoesntExistException();

		//  Remove place from the register and "place_list" in storage
		ArrayList<String> placeRegister = getPlacesKeyRegister();
		placeRegister.remove(dataPlaceName);
		JSONArray placeRegisterJsonArray = new JSONArray(placeRegister);
		editor.putString(PREFERENCE_LIST_NAME, placeRegisterJsonArray.toString());

		//  Remove place data
		editor.remove(dataPlaceName);

		return editor.commit();
	}


	/**
	 *  deletePlace(int deletionPosition) throws ArrayIndexOutOfBoundsException
	 *  <p>
	 * Used to remove data with index of the place in memory
	 * </p>
	 *
	 * @param deletionPosition      Index in memory of the place you want to delete
	 * @throws ArrayIndexOutOfBoundsException    If the index is out of bounds
	 * @return A boolean to know if the operation was a success or not
	 */
	public boolean deletePlace(int deletionPosition) throws ArrayIndexOutOfBoundsException {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		ArrayList<String> placeRegister = getPlacesKeyRegister();

		if (0 <= deletionPosition && deletionPosition < placeRegister.size()) {

			//  Remove place from the register and "place_list" in storage but keep the dataPlaceName linked to it
			String dataPlaceName = placeRegister.get(deletionPosition);
			placeRegister.remove(deletionPosition);
			JSONArray placeRegisterJsonArray = new JSONArray(placeRegister);
			editor.putString(PREFERENCE_LIST_NAME, placeRegisterJsonArray.toString());

			//  Remove place data
			editor.remove(dataPlaceName);
		} else {
			throw new ArrayIndexOutOfBoundsException("Cannot delete something that is outside of the array !");
		}

		return editor.commit();
	}


	/**
	 *  addPlace(@NonNull Place place) throws PlaceAlreadyExistException, JSONException
	 *  <p>
	 * Used to add data with place object
	 * </p>
	 *
	 * @param place         The place you want to add
	 * @throws PlaceAlreadyExistException   If the place already exist
	 * @throws JSONException                If there is any error during parsing or deparsing from and to JSON
	 * @return A boolean to know if the operation was a success or not
	 */
	public boolean addPlace(@NonNull Place place) throws PlaceAlreadyExistException, JSONException {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String dataPlaceName = this.getDataPlaceString(place);

		//  The place must not exist in storage, if it is not the case we throw an exception
		if (this.contains(dataPlaceName)) throw new PlaceAlreadyExistException();

		//  Add place from the register and "place_list" in storage
		ArrayList<String> placesRegister = getPlacesKeyRegister();
		placesRegister.add(dataPlaceName);
		JSONArray placeRegisterJSON = new JSONArray(placesRegister);
		editor.putString(PREFERENCE_LIST_NAME, placeRegisterJSON.toString());

		//  Create and add place data
		editor.putString(dataPlaceName, place.getPlaceObjectJSON().toString());

		return editor.commit();
	}


	/**
	 *  updatePlace(@NonNull Place place) throws PlaceAlreadyExistException, JSONException
	 *  <p>
	 * Used to update data with a place
	 * </p>
	 *
	 * @param place         The place you want to update
	 * @throws PlaceDoesntExistException    If the place doesn't exist
	 * @throws JSONException                If there is any error during parsing or deparsing from and to JSON
	 * @return A boolean to know if the operation was a success or not
	 */
	public boolean updatePlace(@NonNull Place place) throws PlaceDoesntExistException, JSONException {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String dataPlaceName = this.getDataPlaceString(place);

		//  The place must exist in storage, if it is not the case we throw an exception
		if (!this.contains(dataPlaceName)) throw new PlaceDoesntExistException();

		//  Update place data
		editor.putString(dataPlaceName, place.getPlaceObjectJSON().toString());

		return editor.commit();
	}


	/**
	 *  movePlace(int initialPosition, int finalPosition) throws ArrayIndexOutOfBoundsException
	 *  <p>
	 * Used to move place from a position to an other position in storage
	 * </p>
	 *
	 * @param initialPosition       The initial place position
	 * @param finalPosition         The final place position
	 * @throws ArrayIndexOutOfBoundsException   If any of the provided indexes are out of bounds
	 * @return A boolean to know if the operation was a success or not
	 */
	public boolean movePlace(int initialPosition, int finalPosition) throws ArrayIndexOutOfBoundsException {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		ArrayList<String> placesRegister = getPlacesKeyRegister();

		//  Positions must in array bounds, if it is not the case we throw an exception
		if (initialPosition < 0 || placesRegister.size() < initialPosition)
			throw new ArrayIndexOutOfBoundsException(String.format("initialPosition is out of array bounds !	Size: %d, Index: %d", placesRegister.size(), initialPosition));
		if (finalPosition < 0 || placesRegister.size() < finalPosition)
			throw new ArrayIndexOutOfBoundsException(String.format("finalPosition is out of array bounds !	Size: %d, Index: %d", placesRegister.size(), finalPosition));

		//  Move place data
		if (initialPosition != finalPosition) {
			if (finalPosition == placesRegister.size() - 1)
				placesRegister.add(placesRegister.get(initialPosition));
			else if (initialPosition < finalPosition)
				placesRegister.add(finalPosition + 1, placesRegister.get(initialPosition));
			else placesRegister.add(finalPosition, placesRegister.get(initialPosition));

			if (initialPosition < finalPosition) placesRegister.remove(initialPosition);
			else placesRegister.remove(initialPosition + 1);
		}

		JSONArray placeRegisterJSON = new JSONArray(placesRegister);
		editor.putString(PREFERENCE_LIST_NAME, placeRegisterJSON.toString());

		return editor.commit();
	}


	/**
	 * getDataPlaceString(@NonNull Place place)
	 * <p>
	 * Used to get dataPlace key in memory for a place<br>
	 * That doesn't mean that place exist in storage !
	 * </p>
	 *
	 * @param place The place for which you want to get a dataPlace key
	 * @return The dataPlace key
	 */
	public String getDataPlaceString(@NonNull Place place) {
		return String.format("%s/%s", place.getCity().toUpperCase(), place.getCountryCode());
	}


	/**
	 * getPlace(String placeKey) throws JSONException, PlaceDoesntExistException
	 * <p>
	 * Used to get place with the dataPlace key
	 * </p>
	 *
	 * @param placeKey The initial place position
	 * @return The wanted place
	 * @throws JSONException             If an error occurs during deparsing process
	 * @throws PlaceDoesntExistException If the wanted place doesn't exist
	 */
	public Place getPlace(String placeKey) throws JSONException, PlaceDoesntExistException {
		//  Take the value inside the placeKey to transform it into a JSONObject that can be transform into a place Object
		String placeStringData = sharedPreferences.getString(placeKey, null);
		if (placeStringData == null) throw new PlaceDoesntExistException();

		return new Place(new JSONObject(placeStringData));
	}


	/**
	 * getPlacesRegister()
	 * <p>
	 * Used to get arraylist of dataPlaces keys
	 * </p>
	 *
	 * @return An arraylist with all dataPlaces keys
	 */
	public ArrayList<String> getPlacesKeyRegister() {
		ArrayList<String> returnedData = new ArrayList<>();

		String placeListString = sharedPreferences.getString(PREFERENCE_LIST_NAME, null);

		if (placeListString != null) {
			try {
				JSONArray placeListJSONArray = new JSONArray(placeListString);
				for (int i = 0; i < placeListJSONArray.length(); i++) {
					returnedData.add(i, placeListJSONArray.getString(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return returnedData;
	}


	/**
	 * getAllPlacesStored() throws Exception
	 * <p>
	 * Used to get arraylist filled with all places stored in storage
	 * </p>
	 *
	 * @return An arraylist with all places in storage
	 * @throws JSONException             If an error occurs during deparsing process
	 * @throws PlaceDoesntExistException If a place written on the register doesn't exist
	 */
	public ArrayList<Place> getAllPlacesStored() throws JSONException, PlaceDoesntExistException {
		ArrayList<String> placesKeyArrayList = this.getPlacesKeyRegister();
		ArrayList<Place> placeArrayList = new ArrayList<>();

		for (String placeKey : placesKeyArrayList) {
			placeArrayList.add(this.getPlace(placeKey));
		}

		return placeArrayList;
	}


	/**
	 * getPlacePositionInRegister(@NonNull String dataPlaceName)
	 * <p>
	 * Used to get position of a given dataPlaceKey of a place
	 * </p>
	 *
	 * @return The position of the place if found or if not found -1
	 */
	public int getPlacePositionInRegister(@NonNull String dataPlaceName) {
		return this.getPlacesKeyRegister().indexOf(dataPlaceName);
	}


	/**
	 * getPlacePositionInRegister(@NonNull Place place)
	 * <p>
	 * Used to get position of a place in register
	 * </p>
	 *
	 * @return The position of the place if found or if not found -1
	 */
	public int getPlacePositionInRegister(@NonNull Place place) {
		return this.getPlacesKeyRegister().indexOf(this.getDataPlaceString(place));
	}


	/**
	 * size()
	 * <p>
	 * Used to get position of a size of the register
	 * </p>
	 *
	 * @return The size of the register
	 */
	public int size() {
		return this.getPlacesKeyRegister().size();
	}


	/**
	 * registerIsEmpty()
	 * <p>
	 * Used to know if the register is empty
	 * </p>
	 *
	 * @return The emptiness state of the register
	 */
	public boolean registerIsEmpty() {
		return this.getPlacesKeyRegister().isEmpty();
	}


	/**
	 * getAll()
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return null because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public Map<String, ?> getAll() {
		return null;
	}


	/**
	 * getString(String key, @Nullable String defValue)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return null because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Nullable
	@Override
	public String getString(String key, @Nullable String defValue) {
		return null;
	}


	/**
	 * getStringSet(String key, @Nullable Set<String> defValues)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return null because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Nullable
	@Override
	public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
		return null;
	}


	/**
	 * getInt(String key, int defValue)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return 0 because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public int getInt(String key, int defValue) {
		return 0;
	}


	/**
	 * getLong(String key, long defValue)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return 0 because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public long getLong(String key, long defValue) {
		return 0;
	}


	/**
	 * getFloat(String key, long defValue)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return 0 because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public float getFloat(String key, float defValue) {
		return 0;
	}


	/**
	 * getBoolean(String key, long defValue)
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return false because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return false;
	}


	/**
	 * contains(String dataPlaceKey, long defValue)
	 * <p>
	 * Used to know if a place is present in memory
	 * </p>
	 *
	 * @return Will return true if the place exist in memory but false if it doesn't exist
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public boolean contains(String dataPlaceKey) {
		ArrayList<String> placeRegister = getPlacesKeyRegister();
		return placeRegister.contains(dataPlaceKey);
	}


	/**
	 * edit()
	 * <p>
	 * Native Method of SharedPreferences<br>
	 * NOT IMPLEMENTED
	 * </p>
	 *
	 * @return Will return null because it is not implemented
	 * @apiNote NOT IMPLEMENTED
	 */
	@Override
	public Editor edit() {
		return null;
	}


	/**
	 * registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	 * <p>
	 * Used to register a Change listener
	 * </p>
	 *
	 * @param listener The register listener
	 */
	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}


	/**
	 * unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	 * <p>
	 * Used to unregister a Change listener
	 * </p>
	 *
	 * @param listener The unregistered listener
	 */
	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
}