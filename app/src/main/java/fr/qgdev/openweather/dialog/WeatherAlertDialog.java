package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapters.WeatherAlertAdapter;

public class WeatherAlertDialog extends Dialog {


	public WeatherAlertDialog(Context context, Place place) {
		super(context);
		setContentView(R.layout.dialog_weather_alert);

		LinearLayout alertListLinearLayout = findViewById(R.id.alertList);
		WeatherAlertAdapter weatherAlertAdapter;
		weatherAlertAdapter = new WeatherAlertAdapter(context, place);
		for (int i = 0; i < place.getMWeatherAlertCount(); i++) {
			alertListLinearLayout.addView(weatherAlertAdapter.getView(i, null, null), i);
		}
	}

	public void build() {
		show();
	}
}
