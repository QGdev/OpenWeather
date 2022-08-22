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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.dataplaces.DataPlaces;
import fr.qgdev.openweather.dataplaces.PlaceAlreadyExistException;
import fr.qgdev.openweather.fragment.places.PlacesFragment;

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
	private final TextInputLayout cityTextInputLayout, countryTextInputLayout;
	private final TextInputEditText cityEditText;
	private final AutoCompleteTextView countryEditText;
	private final ProgressBar addButtonProgressSpinner;
	private final Button exitButton, addButton;

	//	Callbacks
	private final WeatherService.CallbackGetCoordinates getCoordinatesCallback;
	private final WeatherService.CallbackGetData getDataCallback;

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
	 * @param addPlaceFABView           Basically just a parent view
	 * @param apiKey                    In order to get well formatted values
	 * @param weatherService            Just to get Weather alerts information
	 * @param placeFragmentInteractions In order to get well formatted values
	 * @apiNote None of the parameters can be null
	 */
	public AddPlaceDialog(Context context, View addPlaceFABView, String apiKey, WeatherService weatherService, PlacesFragment.Interactions placeFragmentInteractions) {
		super(context);
		setContentView(R.layout.dialog_add_place);

		this.dialogWindow = findViewById(R.id.dialog_window);
		this.cityTextInputLayout = findViewById(R.id.cityTextInputLayout);
		this.countryTextInputLayout = findViewById(R.id.countryTextInputLayout);
		this.cityEditText = findViewById(R.id.city);
		this.countryEditText = findViewById(R.id.country);
		this.exitButton = findViewById(R.id.exit_button);
		this.addButton = findViewById(R.id.add_button);

		this.addButtonProgressSpinner = findViewById(R.id.add_button_progress_spinner);
		addButtonProgressSpinner.setVisibility(View.GONE);

		this.countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		List<String> countryNamesSorted = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		Collections.sort(countryNamesSorted);
		this.countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));

		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.dialog_country_list_item, countryNamesSorted);
		countryEditText.setThreshold(1);
		countryEditText.performValidation();
		countryEditText.setAdapter(adapter);

		//  Observe country field to show an error if the registered country name doesn't exist
		countryEditText.setOnFocusChangeListener((v, hasFocus) -> {
			//  The country name doesn't exist, the field is not focused and the field isn't empty
			if (getCountryCode() == null && !hasFocus && !getCountryField().isEmpty()) {
				countryTextInputLayout.setError(context.getString(R.string.error_place_country_not_in_list));
			} else {
				countryTextInputLayout.setErrorEnabled(false);
			}
		});

		//  Observe city field to turn off any errors
		cityEditText.setOnFocusChangeListener((v, hasFocus) -> cityTextInputLayout.setErrorEnabled(false));


		//  WeatherService Callbacks
		//________________________________________________________________
		//  This callback will fill place with data
		getCoordinatesCallback = new WeatherService.CallbackGetCoordinates() {
			@Override
			public void onPlaceFound(Place place, DataPlaces dataPlaces) {
				weatherService.getWeatherDataOWM(place, dataPlaces, getDataCallback);
			}

			@Override
			public void onTreatmentError() {
				showSnackbar(dialogWindow, context.getString(R.string.error_cannot_save_place_treatment));
				enableDialogWindowControls();

			}

			@Override
			public void onNoResponseError() {
				showSnackbar(dialogWindow, context.getString(R.string.error_server_unreachable));
				enableDialogWindowControls();
			}

			@Override
			public void onTooManyRequestsError() {
				showSnackbar(dialogWindow, context.getString(R.string.error_too_many_request_in_a_day));
				enableDialogWindowControls();
			}

			@Override
			public void onPlaceNotFoundError() {
				cityTextInputLayout.setError(context.getString(R.string.error_place_not_found));
				enableDialogWindowControls();
			}

			@Override
			public void onWrongOrUnknownApiKeyError() {
				showSnackbar(addPlaceFABView, context.getString(R.string.error_wrong_api_key));
				enableDialogWindowControls();

			}

			@Override
			public void onUnknownError() {
				showSnackbar(addPlaceFABView, context.getString(R.string.error_unknow_error));
				enableDialogWindowControls();

			}

			@Override
			public void onDeviceNotConnected() {
				showSnackbar(dialogWindow, context.getString(R.string.error_device_not_connected));
				enableDialogWindowControls();

			}

			@Override
			public void onTheEndOfTheRequest() {
			}
		};

		//________________________________________________________________
		//  This callback will fill place with data
		this.getDataCallback = new WeatherService.CallbackGetData() {

			@Override
			public void onTreatmentError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_cannot_refresh_place_list_network));
			}

			@Override
			public void onNoResponseError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_server_unreachable));
			}

			@Override
			public void onTooManyRequestsError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_too_many_request_in_a_day));
			}

			@Override
			public void onPlaceNotFoundError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_place_not_found));
			}

			@Override
			public void onWrongOrUnknownApiKeyError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_wrong_api_key));
			}

			@Override
			public void onUnknownError(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_unknow_error));
			}

			@Override
			public void onDeviceNotConnected(RequestStatus requestStatus) {
				showSnackbar(dialogWindow, context.getString(R.string.error_device_not_connected));
			}

			@Override
			public void onTheEndOfTheRequest(Place place, DataPlaces dataPlaces, RequestStatus requestStatus) {
				try {
					if (dataPlaces.addPlace(place)) {
						placeFragmentInteractions.onAddingPlace(place);
						dismiss();
					} else {
						throw new Exception("Commit Error");
					}
				} catch (PlaceAlreadyExistException e) {
					logger.log(Level.WARNING, e.getMessage());
					cityTextInputLayout.setError(context.getString(R.string.error_place_already_added));
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage());
					showSnackbar(dialogWindow, context.getString(R.string.error_cannot_save_place));
				}
				enableDialogWindowControls();
			}
		};


		//  Verify button click listener
		addButton.setOnClickListener(
				verifyButtonView -> {
					disableDialogWindowControls();

					//  Nothing was registered
					if (getCityField().isEmpty() || getCountryField().isEmpty() || getCountryCode() == null) {
						if (getCityField().isEmpty())
							cityTextInputLayout.setError(context.getString(R.string.error_place_city_field_empty));
						if (getCountryField().isEmpty())
							countryTextInputLayout.setError(context.getString(R.string.error_place_country_field_empty));
						else if (getCountryCode() == null)
							countryTextInputLayout.setError(context.getString(R.string.error_place_country_not_in_list));

						enableDialogWindowControls();
					}

					//  No API key is registered
					else if (apiKey == null || apiKey.length() != 32) {
						dismiss();
						Snackbar.make(dialogWindow, context.getString(R.string.error_no_api_key_registered_short), Snackbar.LENGTH_SHORT)
								.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setMaxInlineActionWidth(3)
								.show();
						enableDialogWindowControls();

					}

					//  API key and place settings is correctly registered
					else {
						weatherService.getCoordinatesOWM(new Place(getCityField(), getCountryCode()), getCoordinatesCallback);
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
		Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
				.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
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
