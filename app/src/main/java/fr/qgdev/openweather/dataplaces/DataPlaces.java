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

/*  DataPlaces
 *      Organise place data with SharedPreferences
 *      The organisation of the data is pretty simple :
 *          -   A JSONArray contains all places Keys and it is stored at PREFERENCE_LIST_NAME key
 *          -   Each place stored in the JSONArray will have a "location" in the SharedPreferences
 *              Data place is stored on a String form which is a JSON of the place data
 */
public class DataPlaces implements SharedPreferences {

	private final SharedPreferences sharedPreferences;
	private final String PREFERENCE_FILE_NAME = "places";
	private final String PREFERENCE_LIST_NAME = "places_list";


	public DataPlaces(@NonNull Context context) {
		this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
	}


	//  Delete Place data from SharedPreferences
	public boolean deletePlace(@NonNull String dataPlaceName) throws PlaceDoesntExistException {
		SharedPreferences.Editor editor = sharedPreferences.edit();

		//  The place must exist in storage, if it is not the case we throw an exception
		if (!this.contains(dataPlaceName)) throw new PlaceDoesntExistException();

		//  Remove place from the register and "place_list" in storage
		ArrayList<String> placeRegister = getPlacesRegister();
		placeRegister.remove(dataPlaceName);
		JSONArray placeRegisterJsonArray = new JSONArray(placeRegister);
		editor.putString(PREFERENCE_LIST_NAME, placeRegisterJsonArray.toString());

		//  Remove place data
		editor.remove(dataPlaceName);

		return editor.commit();
	}


	//  Delete Place with a deletionPosition from SharedPreferences
	public boolean deletePlace(int deletionPosition) throws ArrayIndexOutOfBoundsException {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		ArrayList<String> placeRegister = getPlacesRegister();
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


	//  Add and store data of a new Place in SharedPreferences
	public boolean addPlace(@NonNull Place place) throws Exception {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String dataPlaceName = this.getDataPlaceString(place);

		//  The place must not exist in storage, if it is not the case we throw an exception
		if (this.contains(dataPlaceName)) throw new PlaceAlreadyExistException();

		//  Add place from the register and "place_list" in storage
		ArrayList<String> placeRegister = getPlacesRegister();
		placeRegister.add(dataPlaceName);
		JSONArray placeRegisterJSON = new JSONArray(placeRegister);
		editor.putString(PREFERENCE_LIST_NAME, placeRegisterJSON.toString());

		//  Create and add place data
		editor.putString(dataPlaceName, place.getPlaceObjectJSON().toString());

		return editor.commit();
	}


	//  Update place data in SharedPreferences
	public boolean updatePlace(@NonNull Place place) throws Exception {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String dataPlaceName = this.getDataPlaceString(place);

		//  The place must exist in storage, if it is not the case we throw an exception
		if (!this.contains(dataPlaceName)) new PlaceDoesntExistException();

		//  Update place data
		editor.putString(dataPlaceName, place.getPlaceObjectJSON().toString());

		return editor.commit();
	}


	//  Get key string of a place
	public String getDataPlaceString(@NonNull Place place) {
		return String.format("%s/%s", place.getCity().toUpperCase(), place.getCountryCode());
	}


	//  Get Place from SharedPreferences
	public Place getPlace(String placeKey) throws Exception {

		//  Take the value inside the placeKey to transform it into a JSONObject that can be transform into a place Object
		String placeStringData = sharedPreferences.getString(placeKey, null);
		assert placeStringData != null;
		JSONObject placeJSONData = new JSONObject(placeStringData);

		return new Place(placeJSONData);
	}


	//  Get places register from SharedPreferences
	public ArrayList<String> getPlacesRegister() {
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


	//  Get places stored in SharedPreferences
	public ArrayList<Place> getAllPlacesStored() throws Exception {
		ArrayList<String> placesKeyArrayList = this.getPlacesRegister();
		ArrayList<Place> placeArrayList = new ArrayList<>();

		for (String placeKey : placesKeyArrayList) {
			placeArrayList.add(this.getPlace(placeKey));
		}

		return placeArrayList;
	}


	//  Get place position in places register from SharedPreferences
	public int getPlacePositionInRegister(@NonNull String dataPlaceName) {
		return this.getPlacesRegister().indexOf(dataPlaceName);
	}


	//  Get place position in places register from SharedPreferences
	public int getPlacePositionInRegister(@NonNull Place place) {
		return this.getPlacesRegister().indexOf(this.getDataPlaceString(place));
	}


	//  Get number of places in places register from SharedPreferences
	public int size() {
		return this.getPlacesRegister().size();
	}


	//  Check if the places register is empty
	public boolean registerIsEmpty() {
		return this.getPlacesRegister().isEmpty();
	}


	@Override
	public Map<String, ?> getAll() {
		return null;
	}

	@Nullable
	@Override
	public String getString(String key, @Nullable String defValue) {
		return null;
	}

	@Nullable
	@Override
	public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
		return null;
	}

	@Override
	public int getInt(String key, int defValue) {
		return 0;
	}

	@Override
	public long getLong(String key, long defValue) {
		return 0;
	}

	@Override
	public float getFloat(String key, float defValue) {
		return 0;
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return false;
	}

	@Override
	public boolean contains(String dataPlaceName) {
		ArrayList<String> placeRegister = getPlacesRegister();

		return placeRegister.contains(dataPlaceName);
	}

	@Override
	public Editor edit() {
		return null;
	}


	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		this.sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}


	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
}