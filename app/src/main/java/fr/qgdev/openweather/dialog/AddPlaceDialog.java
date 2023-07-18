/*
 *  Copyright (c) 2019 - 2023
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather.
 *
 *  OpenWeather is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenWeather is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.repositories.weather.FetchDataCallback;
import fr.qgdev.openweather.repositories.weather.RequestStatus;

/**
 * AppPlaceDialog
 * <p>
 * The dialog used to add places<br>
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Dialog
 */
public class AddPlaceDialog extends Dialog {
	
	private static final String TAG = AddPlaceDialog.class.getSimpleName();
	private final Logger logger = Logger.getLogger(TAG);
	
	//	Dialog elements
	private final ConstraintLayout dialogWindow;
	private final TextInputLayout cityTextInputLayout;
	private final TextInputLayout countryTextInputLayout;
	private final TextInputEditText cityEditText;
	private final AutoCompleteTextView countryEditText;
	private final ProgressBar addButtonProgressSpinner;
	private final Button exitButton;
	private final Button addButton;
	
	//	List of countries
	//	Used in order to sort country names
	private final List<String> countryNames;
	private final List<String> countryCodes;
	
	/**
	 * AddPlaceDialog Constructor
	 * <p>
	 * Just the constructor of AddPlaceDialog class
	 * </p>
	 *
	 * @param context                   Context of the application in order to get resources
	 * //@param addPlaceFABView           Basically just a parent view
	 
	 * //@param placeFragmentInteractions In order to get well formatted values
	 * @apiNote None of the parameters can be null
	 */
	public AddPlaceDialog(Context context, AppRepository appRepository) {
		super(context);
		setContentView(R.layout.dialog_add_place);
		
		dialogWindow = findViewById(R.id.dialog_window);
		cityTextInputLayout = findViewById(R.id.cityTextInputLayout);
		countryTextInputLayout = findViewById(R.id.countryTextInputLayout);
		cityEditText = findViewById(R.id.city);
		countryEditText = findViewById(R.id.country);
		exitButton = findViewById(R.id.exit_button);
		addButton = findViewById(R.id.add_button);
		
		addButtonProgressSpinner = findViewById(R.id.add_button_progress_spinner);
		addButtonProgressSpinner.setVisibility(View.GONE);
		
		countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		List<String> countryNamesSorted = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		Collections.sort(countryNamesSorted);
		countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.dialog_country_list_item, countryNamesSorted);
		countryEditText.setThreshold(1);
		countryEditText.performValidation();
		countryEditText.setAdapter(adapter);
		
		
		//  Observe country field to show an error if the registered country name doesn't exist
		countryEditText.setOnFocusChangeListener((v, hasFocus) -> {
			//  The country name doesn't exist, the field is not focused and the field isn't empty
			if (getCountryCode() == null && !hasFocus && !getCountryField().isEmpty()) {
				countryTextInputLayout.setError(context.getString(R.string.error_place_country_not_in_list));
				return;
			}
			countryTextInputLayout.setErrorEnabled(false);
		});
		
		
		//  Observe city field to turn off any errors
		cityEditText.setOnFocusChangeListener((v, hasFocus) -> cityTextInputLayout.setErrorEnabled(false));
		
		FetchDataCallback fetchDataCallback = new FetchDataCallback() {
			@Override
			public void onSuccess(Place place) {
				dismiss();
			}
			
			@Override
			public void onPartialSuccess(Place place, RequestStatus requestStatus) {
				dismiss();
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				enableDialogWindowControls();
				logger.log(Level.WARNING, "Error while adding place: {0}", requestStatus);
				switch (requestStatus) {
					case NO_ANSWER:
						showSnackbar(dialogWindow, context.getString(R.string.error_server_unreachable));
						break;
					case NOT_CONNECTED:
						showSnackbar(dialogWindow, context.getString(R.string.error_device_not_connected));
						break;
					case TOO_MANY_REQUESTS:
						showSnackbar(dialogWindow, context.getString(R.string.error_too_many_request_in_a_day));
						break;
					case AUTH_FAILED:
						showSnackbar(dialogWindow, context.getString(R.string.error_wrong_api_key));
						break;
					case NOT_FOUND:
						showSnackbar(dialogWindow, context.getString(R.string.error_place_not_found));
						break;
					case ALREADY_PRESENT:
						showSnackbar(dialogWindow, context.getString(R.string.error_place_already_added));
						break;
					default:
						showSnackbar(dialogWindow, context.getString(R.string.error_unknown_error));
						break;
				}
			}
		};
		
		
		//  Verify button click listener
		addButton.setOnClickListener(
				  verifyButtonView -> {
					  disableDialogWindowControls();
					  
					  //  Nothing was registered
					  if (getCityField().isEmpty()) {
						  cityTextInputLayout.setError(context.getString(R.string.error_place_city_field_empty));
						  enableDialogWindowControls();
					  } else if (getCountryField().isEmpty()) {
						  countryTextInputLayout.setError(context.getString(R.string.error_place_country_field_empty));
						  enableDialogWindowControls();
					  } else if (getCountryCode() == null) {
						  countryTextInputLayout.setError(context.getString(R.string.error_place_country_not_in_list));
						  enableDialogWindowControls();
					  }
					  
					  //  API key and place settings is correctly registered
					  else {
						  appRepository.findPlaceAndAdd(getCityField(), getCountryCode(), fetchDataCallback);
					  }
				  });
		
		exitButton.setOnClickListener(v -> dismiss());
	}
	
	/**
	 * getCityField()
	 * <p>
	 * Will just provide the value registered in the city EditText element of the dialog
	 * </p>
	 *
	 * @return The content of the city EditText element
	 */
	public String getCityField() {
		Editable city = cityEditText.getText();
		if (city == null || city.length() == 0) {
			return "";
		}
		
		// remove extra spaces at the beginning and at the end
		while (city.charAt(0) == ' ') {
			city.delete(0, 1);
		}
		while (city.charAt(city.length() - 1) == ' ') {
			city.delete(city.length() - 1, city.length());
		}
		
		return city.toString();
	}
	
	/**
	 * getCountryField()
	 * <p>
	 * Will just provide the value registered in the country EditText element of the dialog
	 * </p>
	 *
	 * @return The content of the country EditText element
	 */
	public String getCountryField() {
		Editable country = countryEditText.getText();
		if (country.length() == 0) {
			return "";
		}
		
		// remove extra spaces at the beginning and at the end
		while (country.charAt(0) == ' ') {
			country.delete(0, 1);
		}
		while (country.charAt(country.length() - 1) == ' ') {
			country.delete(country.length() - 1, country.length());
		}
		
		return country.toString();
	}
	
	/**
	 * getCountryCode()
	 * <p>
	 * Will just take the content of the country EditText element and match it with the stored
	 * list in memory in order to get the corresponding country code
	 * </p>
	 *
	 * @return If there is a match, it will return the country code in two letters format ("XX") but null if not
	 * @apiNote Will return null if there is no match
	 */
	public String getCountryCode() {
		int indexOf = countryNames.indexOf(getCountryField());
		if (indexOf == -1) return null;
		return countryCodes.get(indexOf);
	}
	
	/**
	 * showSnackbar(...)
	 * <p>
	 * Will just compose and show a snackbar with predefined parameters, just need to provide a parent view and the content
	 * Acts like a "shortcut" function
	 * </p>
	 *
	 * @param view    The parent view, where the snackbar will be shown
	 * @param message The content of the snackbar, what will be shown
	 */
	private void showSnackbar(View view, String message) {
		Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_SHORT)
				  .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
				  .setMaxInlineActionWidth(3)
				  .show();
	}
	
	/**
	 * disableDialogWindowControls()
	 * <p>
	 * Just used like a "shortcut" function, will disable ADD and EXIT button of the dialog
	 * </p>
	 */
	public void disableDialogWindowControls() {
		addButton.setEnabled(false);
		exitButton.setEnabled(false);
		addButtonProgressSpinner.setVisibility(View.VISIBLE);
	}
	
	/**
	 * enableDialogWindowControls()
	 * <p>
	 * Just used like a "shortcut" function, will enable ADD and EXIT button of the dialog
	 * </p>
	 */
	public void enableDialogWindowControls() {
		addButton.setEnabled(true);
		exitButton.setEnabled(true);
		addButtonProgressSpinner.setVisibility(View.GONE);
	}
	
	/**
	 * build()
	 * <p>
	 * Just the function to build the whole SnackBar but in this case, the dialog, will be built and shown
	 * </p>
	 *
	 */
	public void build() {
		show();
	}
}
