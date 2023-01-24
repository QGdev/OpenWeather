package fr.qgdev.openweather.repositories.settings;

import android.content.Context;

import java.util.Locale;

public class SettingsManager {
	
	private final SecuredPreferenceDataStore securedPreferenceDataStore;
	
	public SettingsManager(Context context) {
		securedPreferenceDataStore = new SecuredPreferenceDataStore(context);
	}
	
	public TemperatureSettings getTemperatureSetting() {
		switch (securedPreferenceDataStore.getString("conf_temperature_unit", null)) {
			case "fahrenheit":
				return TemperatureSettings.FAHRENHEIT;
			case "kelvin":
				return TemperatureSettings.KELVIN;
			default:
			case "celsius":
				return TemperatureSettings.CELSIUS;
		}
	}
	
	public MeasureSettings getMeasureSetting() {
		switch (securedPreferenceDataStore.getString("conf_measure_unit", null)) {
			case "imperial":
				return MeasureSettings.IMPERIAL;
			default:
			case "metric":
				return MeasureSettings.METRIC;
		}
	}
	
	public PressureSettings getPressureSetting() {
		switch (securedPreferenceDataStore.getString("conf_pressure_unit", null)) {
			case "mbar":
				return PressureSettings.BAROMETRIC;
			case "psi":
				return PressureSettings.POUNDS_SQUARE_INCH;
			case "inhg":
				return PressureSettings.INCH_MERCURY;
			default:
			case "pascal":
				return PressureSettings.HECTOPASCAL;
		}
	}
	
	public WindDirectionSettings getWindDirectionSetting() {
		switch (securedPreferenceDataStore.getString("conf_direction_unit", null)) {
			case "angular":
				return WindDirectionSettings.ANGULAR;
			default:
			case "cardinal":
				return WindDirectionSettings.CARDINAL_POINTS;
		}
	}
	
	public TimeSettings getTimeSetting() {
		switch (securedPreferenceDataStore.getString("conf_time_format", null)) {
			case "12":
				return TimeSettings.TWELVE_HOURS;
			default:
			case "24":
				return TimeSettings.TWENTY_FOUR_HOURS;
		}
	}
	
	public Locale getDefaultLocale() {
		return Locale.getDefault();
	}
	
	public String getApiKey() {
		return securedPreferenceDataStore.getString("conf_api_key", null);
	}
}
