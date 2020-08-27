package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;

public class AddPlaceDialog extends Dialog {

    private TextView title;
    private TextInputEditText cityEditText;
    private AutoCompleteTextView countryEditText;
    private Button verifyButton;

    private List<String> countryNames, countryCodes;

    private View addPlaceFABView;


    public AddPlaceDialog(Context context, View addPlaceFABView, WeatherService.WeatherCallback callback) {
        super(context);
        setContentView(R.layout.dialog_add_place);

        this.title = findViewById(R.id.title);
        this.cityEditText = findViewById(R.id.city);
        this.countryEditText = findViewById(R.id.country);
        this.verifyButton = findViewById(R.id.verify_button);

        this.addPlaceFABView = addPlaceFABView;

        this.countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
        this.countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, countryNames);
        countryEditText.setThreshold(0);
        countryEditText.setAdapter(adapter);


        verifyButton.setOnClickListener(
                verifyButtonView -> {
                    verifyButton.setText(context.getString(R.string.title_dialog_add_place_verify_button_verification));
                    verifyButton.setEnabled(false);

                    SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String apiKey = apiKeyPref.getString("api_key", null);

                    //  Nothing was registered
                    if (getCity().isEmpty() || getCountryName().isEmpty()) {

                        Snackbar.make(addPlaceFABView, context.getString(R.string.error_no_place_settings_registered), Snackbar.LENGTH_LONG)
                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                .setMaxInlineActionWidth(3)
                                .show();
                        verifyButton.setText(context.getString(R.string.title_dialog_add_place_verify_button));
                        verifyButton.setEnabled(true);

                    }
                    //  No API key is registered
                    else if (apiKey == null) {

                        dismiss();
                        Snackbar.make(addPlaceFABView, context.getString(R.string.error_no_api_key_registered), Snackbar.LENGTH_LONG)
                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                .setMaxInlineActionWidth(3)
                                .show();
                        verifyButton.setText(context.getString(R.string.title_dialog_add_place_verify_button));
                        verifyButton.setEnabled(true);

                    }
                    //  API key and place settings is correctly registered
                    else {

                        Place place = new Place(getCity(), getCountryName(), getCountryCode());

                        RequestQueue weatherDataRequest = Volley.newRequestQueue(context);
                        String url = String.format(context.getString(R.string.url_owm_coordinates), place.getCity(), place.getCountryCode(), apiKey);

                        JsonObjectRequest verifyPlaceRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                                response -> {

                                    place.setErrorDuringDataAcquisition(false);
                                    place.setErrorCode(200);


                                    try {
                                        //  Get correct city name
                                        //place.setCity(response.getString("name"));
                                        //String cityName = place.getCity();
                                        //cityName.charAt(0) = cityName.charAt(0)

                                        final int timeZoneOffSet = response.getInt("timezone");
                                        int UTCnumber = timeZoneOffSet / 3600;
                                        if (timeZoneOffSet == 0) {
                                            place.setTimeZone("UTC");
                                        } else if (timeZoneOffSet < 0) {
                                            place.setTimeZone(String.format("%d", UTCnumber));
                                        } else {
                                            place.setTimeZone(String.format("+%d", UTCnumber));
                                        }


                                        JSONObject coordinatesJSON = response.getJSONObject("coord");
                                        place.setLongitude(coordinatesJSON.getDouble("lon"));
                                        place.setLatitude(coordinatesJSON.getDouble("lat"));


                                        DataPlaces dataPlaces = new DataPlaces(context);

                                        WeatherService weatherService = new WeatherService(getContext(), apiKey, dataPlaces);
                                        WeatherService.WeatherCallback weatherCallback = new WeatherService.WeatherCallback() {
                                            @Override
                                            public void onWeatherData(final Place place, DataPlaces dataPlaces) {

                                                String dataPlaceName = place.getCity().toUpperCase() + '/' + place.getCountryCode();

                                                try {

                                                    if (!dataPlaces.getPlacesRegister().contains(dataPlaceName)) {

                                                        //  Save Place in the register
                                                        if (!dataPlaces.savePlaceInRegister(dataPlaceName)) {
                                                            dismiss();
                                                            Snackbar.make(addPlaceFABView, context.getString(R.string.error_cannot_save_place_in_register), Snackbar.LENGTH_LONG)
                                                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                                    .setMaxInlineActionWidth(3)
                                                                    .show();
                                                            return;
                                                        }
                                                        //  Save Place
                                                        if (!dataPlaces.savePlace(place)) {
                                                            dismiss();
                                                            Snackbar.make(addPlaceFABView, context.getString(R.string.error_cannot_save_place), Snackbar.LENGTH_LONG)
                                                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                                    .setMaxInlineActionWidth(3)
                                                                    .show();
                                                            return;
                                                        }
                                                        dismiss();
                                                        Snackbar.make(addPlaceFABView, String.format("%s, %s %s", place.getCity(), getCountryCode(), context.getString(R.string.place_added_succcessfully)), Snackbar.LENGTH_LONG)
                                                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                                .setMaxInlineActionWidth(3)
                                                                .show();
                                                        callback.onWeatherData(place, dataPlaces);
                                                    } else {
                                                        dismiss();
                                                        Snackbar.make(addPlaceFABView, context.getString(R.string.error_place_already_added), Snackbar.LENGTH_LONG)
                                                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                                .setMaxInlineActionWidth(3)
                                                                .show();
                                                    }


                                                } catch (Exception e) {
                                                    dismiss();
                                                    Snackbar.make(addPlaceFABView, context.getString(R.string.error_unknow_error), Snackbar.LENGTH_LONG)
                                                            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                            .setMaxInlineActionWidth(3)
                                                            .show();
                                                    e.printStackTrace();
                                                }


                                            }

                                            @Override
                                            public void onError(Exception exception, Place place, DataPlaces dataPlaces) {

                                            }


                                            public void onConnectionError(VolleyError error, Place place, DataPlaces dataPlaces) {

                                            }
                                        };
                                        weatherService.getWeatherDataOWM(place, weatherCallback);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                },

                                error -> {

                                    //  no server response (NO INTERNET or SERVER DOWN)
                                    if (error.networkResponse == null) {
                                        dismiss();
                                        Snackbar.make(addPlaceFABView, context.getString(R.string.error_server_unreachable), Snackbar.LENGTH_LONG)
                                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                .setMaxInlineActionWidth(3)
                                                .show();
                                    }
                                    //  Server response
                                    else {

                                        place.setErrorDuringDataAcquisition(true);
                                        place.setErrorCode(error.networkResponse.statusCode);

                                        switch (place.getErrorCode()) {
                                            case 429:
                                                dismiss();
                                                Snackbar.make(addPlaceFABView, context.getString(R.string.error_too_many_request_in_a_day), Snackbar.LENGTH_LONG)
                                                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                        .setMaxInlineActionWidth(3)
                                                        .show();
                                                break;
                                            case 404:
                                                Snackbar.make(addPlaceFABView, context.getString(R.string.error_place_not_found), Snackbar.LENGTH_LONG)
                                                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                        .setMaxInlineActionWidth(3)
                                                        .show();
                                                break;

                                            case 401:
                                                dismiss();
                                                Snackbar.make(addPlaceFABView, context.getString(R.string.error_wrong_api_key), Snackbar.LENGTH_LONG)
                                                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                        .setMaxInlineActionWidth(3)
                                                        .show();
                                                break;

                                            default:
                                                dismiss();
                                                Snackbar.make(addPlaceFABView, context.getString(R.string.error_unknow_error), Snackbar.LENGTH_LONG)
                                                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                                        .setMaxInlineActionWidth(3)
                                                        .show();
                                                break;
                                        }
                                    }
                                });

                        weatherDataRequest.add(verifyPlaceRequest);
                    }
                    verifyButton.setText(context.getString(R.string.title_dialog_add_place_verify_button));
                    verifyButton.setEnabled(true);
                });

    }


    public String getCity() {
        Editable city = cityEditText.getText();
        if (city.length() == 0) {
            return "";
        }
        return city.toString();
    }

    public String getCountryCode() {
        int indexOf = countryNames.indexOf(getCountryName());

        if (indexOf == -1) {
            return null;
        }
        return countryCodes.get(indexOf);
    }

    public String getCountryName() {
        Editable country = countryEditText.getText();
        if (country.length() == 0) {
            return "";
        }
        return country.toString();
    }

    public Button getVerifyButton() {
        return verifyButton;
    }

    public void build() {
        show();
    }
}
