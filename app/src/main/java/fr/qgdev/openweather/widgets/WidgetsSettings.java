
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

import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manage widgets settings
 */
public class WidgetsSettings {
	
	private final String placeId;
	private final int widgetId;
	
	/**
	 * Create a new widget settings
	 *
	 * @param placeId  The id of the place
	 * @param widgetId The id of the widget
	 */
	public WidgetsSettings(String placeId, int widgetId) {
		if (placeId == null) throw new IllegalArgumentException("placeId must not be null");
		if (widgetId == INVALID_APPWIDGET_ID)
			throw new IllegalArgumentException("widgetId must be a valid widget id");
		
		this.placeId = placeId;
		this.widgetId = widgetId;
	}
	
	/**
	 * Converts a json object to a widget settings
	 *
	 * @param json The json object to convert
	 * @return The widget settings
	 */
	public static WidgetsSettings fromJson(@NonNull JSONObject json) throws JSONException {
		return new WidgetsSettings(json.getString("placeId"),
				  json.getInt("widgetId"));
	}
	
	/**
	 * Get place id
	 *
	 * @return The place id
	 */
	public String getPlaceId() {
		return placeId;
	}
	
	/**
	 * Get widget id
	 *
	 * @return The widget id
	 */
	public int getWidgetId() {
		return widgetId;
	}
	
	/**
	 * Converts the widget settings to a string
	 *
	 * @return The widget settings as a string
	 */
	@NonNull
	@Override
	public String toString() {
		return "WidgetsSettings{" +
				  "placeId=" + placeId +
				  ", widgetId=" + widgetId +
				  '}';
	}
	
	/**
	 * Converts the widget settings to a json object
	 *
	 * @return The widget settings as a json object
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("placeId", placeId);
		json.put("widgetId", widgetId);
		
		return json;
	}
	
	/**
	 * Converts the widget settings to a json string
	 *
	 * @return The widget settings as a json string
	 */
	public String toJsonString() throws JSONException {
		return toJson().toString();
	}
	
	/**
	 * Check if two widget settings are equals
	 *
	 * @param o The object to compare
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WidgetsSettings)) return false;
		
		WidgetsSettings that = (WidgetsSettings) o;
		
		if (placeId != that.placeId) return false;
		return widgetId == that.widgetId;
	}
	
	/**
	 * Get the hash code of the widget settings
	 *
	 * @return The hash code of the widget settings
	 */
	@Override
	public int hashCode() {
		String result = placeId;
		result += widgetId;
		return result.hashCode();
	}
}
