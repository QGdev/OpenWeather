package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapter.WeatherAlertAdapter;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * WeatherAlertDialog
 * <p>
 * Weather Alert dialog box where all weather alerts informations are presented<br>
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Dialog
 */
public class WeatherAlertDialog extends Dialog {

	/**
	 * WeatherAlertDialog Constructor
	 * <p>
	 * Just the constructor of WeatherAlertDialog class
	 * </p>
	 *
	 * @param context           Context of the application in order to get resources
	 * @param place             Just to get Weather alerts information
	 * @param formattingService In order to get well formatted values
	 * @apiNote None of the parameters can be null
	 */
	public WeatherAlertDialog(Context context, Place place, FormattingService formattingService) {
		super(context);
		setContentView(R.layout.dialog_weather_alert);

		//	Set exit button behavior
		Button exitButton = findViewById(R.id.exit_button);
		exitButton.setOnClickListener(v -> dismiss());

		//	Fill weather alerts section
		LinearLayout alertListLinearLayout = findViewById(R.id.alertList);
		WeatherAlertAdapter weatherAlertAdapter = new WeatherAlertAdapter(context, place, formattingService);
		for (int i = 0; i < place.getMWeatherAlertCount(); i++) {
			alertListLinearLayout.addView(weatherAlertAdapter.getView(i, null, null), i);
		}
	}

	/**
	 * build()
	 * <p>
	 * Just the function to build the whole SnackBar but in this case, the dialog, will be built and shown
	 * </p>
	 */
	public void build() {
		show();
	}
}
