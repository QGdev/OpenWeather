/*
 *  Copyright (c) 2019 - 2024
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

package fr.qgdev.openweather.repositories.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SettingsManager
 * <p>
 *    A class to manage the settings of the application.
 *    It uses the SecuredPreferenceDataStore to store and retrieve the settings.
 *    It's a singleton.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
public final class SettingsManager {
	
	private static final AtomicReference<SettingsManager> instance = new AtomicReference<>(null);
	private final SecuredPreferenceDataStore securedPreferenceDataStore;
	
	
	public static SettingsManager getInstance(@NonNull Context context) {
		Context applicationContext = context.getApplicationContext();
		
		if (instance.get() == null) {
			synchronized (SettingsManager.class) {
				instance.compareAndSet(null, new SettingsManager(applicationContext));
			}
		}
		return instance.get();
	}
	
	private SettingsManager(@NonNull Context context) {
		securedPreferenceDataStore = new SecuredPreferenceDataStore(context,
				  "fr.qgdev.openweather_preferences");
	}
	
	public TemperatureSettings getTemperatureSetting() {
		switch (securedPreferenceDataStore.getString("conf_temperature_unit", "")) {
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
		switch (securedPreferenceDataStore.getString("conf_measure_unit", "")) {
			case "imperial":
				return MeasureSettings.IMPERIAL;
			default:
			case "metric":
				return MeasureSettings.METRIC;
		}
	}
	
	public PressureSettings getPressureSetting() {
		switch (securedPreferenceDataStore.getString("conf_pressure_unit", "")) {
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
		switch (securedPreferenceDataStore.getString("conf_direction_unit", "")) {
			case "angular":
				return WindDirectionSettings.ANGULAR;
			default:
			case "cardinal":
				return WindDirectionSettings.CARDINAL_POINTS;
		}
	}
	
	public TimeSettings getTimeSetting() {
		switch (securedPreferenceDataStore.getString("conf_time_format", "")) {
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
	
	public boolean isPeriodicUpdateEnabled() {
		return securedPreferenceDataStore.getBoolean("conf_update_periodic", false);
	}
}
