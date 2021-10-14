package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapter.WeatherAlertAdapter;

public class WeatherAlertDialog extends Dialog {

	private final WeatherAlertAdapter weatherAlertAdapter;
	private final Button exitButton;
	private final LinearLayout alertListLinearLayout;

	public WeatherAlertDialog(Context context, Place place) {
		super(context);
		setContentView(R.layout.dialog_weather_alert);

		this.alertListLinearLayout = findViewById(R.id.alertList);
		this.exitButton = findViewById(R.id.exit_button);
		this.exitButton.setOnClickListener(v -> dismiss());

		this.weatherAlertAdapter = new WeatherAlertAdapter(context, place);
		for (int i = 0; i < place.getMWeatherAlertCount(); i++) {
			alertListLinearLayout.addView(weatherAlertAdapter.getView(i, null, null), i);
		}
	}

	public void build() {
		show();
	}
}
