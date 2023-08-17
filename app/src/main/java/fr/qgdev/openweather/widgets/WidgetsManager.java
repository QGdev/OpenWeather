
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

package fr.qgdev.openweather.widgets;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import fr.qgdev.openweather.repositories.settings.SecuredPreferenceDataStore;

/**
 * Manage widgets settings storage
 */
public final class WidgetsManager {
	
	private static final String FILE_NAME = "fr.qgdev.openweather_widget";
	private static final String PREFIX_WIDGET = "widget_";
	private final SecuredPreferenceDataStore securedPreferenceDataStore;
	
	public WidgetsManager(@NonNull Context context) {
		this.securedPreferenceDataStore = new SecuredPreferenceDataStore(context,
				  FILE_NAME);
	}
	
	/**
	 * Generate the storage key name for a widget
	 *
	 * @param appWidgetId The id of the widget
	 * @return The key name
	 */
	private static String getKeyName(int appWidgetId) {
		return PREFIX_WIDGET + appWidgetId;
	}
	
	/**
	 * Save widget settings
	 *
	 * @param widgetsSettings The id of the widget
	 */
	public boolean saveWidgetSettings(@NonNull WidgetsSettings widgetsSettings) {
		int appWidgetId = widgetsSettings.getWidgetId();
		String key = getKeyName(appWidgetId);
		String json;
		
		try {
			json = widgetsSettings.toJsonString();
			Log.d("WidgetsSettings", key);
			Log.d("WidgetsSettings", json);
		} catch (JSONException e) {
			return false;
		}
		
		securedPreferenceDataStore.putString(key, json);
		
		//	Checks if the saving process was successful
		return widgetsSettings.equals(loadWidgetSettings(appWidgetId, null));
	}
	
	/**
	 * Load widget settings
	 *
	 * @param appWidgetId The id of the widget
	 * @return An object containing the settings from WidgetsSettings class
	 */
	public WidgetsSettings loadWidgetSettings(int appWidgetId, Object ifNotFound) {
		String key = getKeyName(appWidgetId);
		if (!securedPreferenceDataStore.contains(key)) return (WidgetsSettings) ifNotFound;
		String json = securedPreferenceDataStore.getString(key, null);
		if (json == null) return (WidgetsSettings) ifNotFound;
		
		try {
			JSONObject jsonObject = new JSONObject(json);
			WidgetsSettings widgetsSettings = WidgetsSettings.fromJson(jsonObject);
			
			if (widgetsSettings == null) return (WidgetsSettings) ifNotFound;
			return widgetsSettings;
		} catch (JSONException e) {
			return (WidgetsSettings) ifNotFound;
		}
	}
	
	/**
	 * Delete widget settings
	 *
	 * @param appWidgetId The id of the widget
	 */
	public void deleteWidgetSettings(int appWidgetId) {
		securedPreferenceDataStore.remove(getKeyName(appWidgetId));
	}
	
	/**
	 * Check if widget settings are present
	 *
	 * @param appWidgetId The id of the widget
	 * @return True if settings are present, false otherwise
	 */
	public boolean isWidgetSettingsPresent(int appWidgetId) {
		return securedPreferenceDataStore.contains(getKeyName(appWidgetId));
	}
	
	/**
	 * Will send a broadcast to all widgets to update them.
	 *
	 * @param context the context used to send the broadcast
	 */
	public void updateWidgets(Context context) {
		Intent updateIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
		updateIntent.setPackage("fr.qgdev.openweather");
		context.sendBroadcast(updateIntent, "fr.qgdev.openweather.permission.UPDATE_WIDGET");
	}
}
