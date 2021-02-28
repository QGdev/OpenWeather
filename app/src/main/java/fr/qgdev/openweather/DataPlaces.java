package fr.qgdev.openweather;

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

import static android.content.Context.MODE_PRIVATE;

public class DataPlaces implements SharedPreferences {

    private final SharedPreferences sharedPreferences;

    public DataPlaces(@NonNull Context context){
        this.sharedPreferences = context.getSharedPreferences("places", MODE_PRIVATE);
    }


    //  Delete data from SharedPreferences
    public boolean deletePlace(@NonNull Place place) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String dataPlaceName = place.getCity().toUpperCase() + '/' + place.getCountryCode();
        ArrayList<String> placeRegister = getPlacesRegister();

        if(!placeRegister.contains(dataPlaceName)) return false;

        placeRegister.remove(dataPlaceName);
        JSONArray placeRegisterJsonArray = new JSONArray(placeRegister);
        editor.putString("places_list", placeRegisterJsonArray.toString());
        editor.remove(dataPlaceName);
        return editor.commit();
    }

    //  Delete data from SharedPreferences
    public boolean deletePlace(@NonNull String dataSetName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        ArrayList<String> placeRegister = getPlacesRegister();

        if(!placeRegister.contains(dataSetName)) return false;

        placeRegister.remove(dataSetName);
        JSONArray placeRegisterJsonArray = new JSONArray(placeRegister);
        editor.putString("places_list", placeRegisterJsonArray.toString());
        editor.remove(dataSetName);
        return editor.commit();
    }


    //  Save Place to SharedPreferences
    public boolean savePlace(@NonNull Place place) throws Exception {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String dataPlaceName = place.getCity().toUpperCase() + '/' + place.getCountryCode();

        if(!getPlacesRegister().contains(dataPlaceName)) return false;
        editor.putString(dataPlaceName, place.getPlaceObjectJSON().toString());

        return editor.commit();
    }

    //  Save Place into places register to SharedPreferences
    public boolean savePlaceInRegister(@NonNull String dataPlaceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        ArrayList<String> placeRegister = getPlacesRegister();

        if(placeRegister.contains(dataPlaceName)) return false;

        placeRegister.add(dataPlaceName);
        JSONArray placeRegisterJSON = new JSONArray(placeRegister);

        editor.putString("places_list", placeRegisterJSON.toString());

        return editor.commit();
    }

    //  Get Places from SharedPreferences
    public ArrayList<Place> getPlaces() throws Exception{

        ArrayList<String> placeListRegister = getPlacesRegister();
        ArrayList<Place> returnedData = new ArrayList<>();

        Place placeTmp;
        String currentPlaceTmp;
        String currentPlaceStringData;

        if (placeListRegister != null){
            for(int i = 0; i < placeListRegister.size(); i++){

                currentPlaceTmp = placeListRegister.get(i);
                currentPlaceStringData = sharedPreferences.getString(currentPlaceTmp, null);

                assert currentPlaceStringData != null;
                JSONObject currentPlaceJSONData = new JSONObject(currentPlaceStringData);

                placeTmp = new Place(currentPlaceJSONData);
                returnedData.add(i, placeTmp);

            }
        }
        return returnedData;
    }


    //  Get places register from SharedPreferences
    public ArrayList<String> getPlacesRegister() {
        ArrayList<String> returnedData = new ArrayList<>();

        String placeListString = sharedPreferences.getString("places_list", null);

        if(placeListString != null){
            try {
                JSONArray placeListJSONArray = new JSONArray(placeListString);
                for(int i = 0; i < placeListJSONArray.length(); i++){
                    returnedData.add(i, placeListJSONArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return returnedData;
    }


    //  Get place position in places register from SharedPreferences
    public int getPlacePositionInRegister(@NonNull String dataPlaceName) { return getPlacesRegister().indexOf(dataPlaceName); }

    //  Get place position in places register from SharedPreferences
    public int getPlacePositionInRegister(@NonNull Place place) {
        String dataPlaceName = place.getCity().toUpperCase() + '/' + place.getCountryCode();
        return getPlacesRegister().indexOf(dataPlaceName);
    }

    //  Get number of places in places register from SharedPreferences
    public int getNumberOfPlacesInRegister() { return getPlacesRegister().size(); }

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
    public boolean contains(String key) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }
}
