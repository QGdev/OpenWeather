
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

package fr.qgdev.openweather.widgets;

import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * WidgetsSettings
 * <p>
 * 	Used to as a data holder for widgets settings.
 * 	Contains the place id and the widget id.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
public class WidgetsSettings {
	
	private final String placeId;
	private final int widgetId;
	private final int backgroundTransparency;
	private final boolean showDetails;
	
	/**
	 * Create a new widget settings
	 *
	 * @param placeId  The id of the place
	 * @param widgetId The id of the widget
	 */
	public WidgetsSettings(String placeId, int widgetId) {
		this(placeId, widgetId, 0, true);
	}
	
	/**
	 * Create a new widget settings
	 *
	 * @param placeId                The id of the place
	 * @param widgetId               The id of the widget
	 * @param backgroundTransparency How transparent the widget's sky is, from 0 (opaque) to 100
	 * @param showDetails            Whether a one-row widget may show more than the temperature
	 */
	public WidgetsSettings(String placeId, int widgetId, int backgroundTransparency, boolean showDetails) {
		if (placeId == null) throw new IllegalArgumentException("placeId must not be null");
		if (widgetId == INVALID_APPWIDGET_ID)
			throw new IllegalArgumentException("widgetId must be a valid widget id");
		if (backgroundTransparency < 0 || backgroundTransparency > 100)
			throw new IllegalArgumentException("backgroundTransparency must be between 0 and 100");
		
		this.placeId = placeId;
		this.widgetId = widgetId;
		this.backgroundTransparency = backgroundTransparency;
		this.showDetails = showDetails;
	}
	
	/**
	 * Converts a json object to a widget settings
	 *
	 * @param json The json object to convert
	 * @return The widget settings
	 */
	public static WidgetsSettings fromJson(@NonNull JSONObject json) throws JSONException {
		// Settings saved before these options existed have none: they were opaque, and showed
		// details, the default.
		return new WidgetsSettings(json.getString("placeId"),
				  json.getInt("widgetId"),
				  json.optInt("backgroundTransparency", 0),
				  json.optBoolean("showDetails", true));
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
	 * Get how transparent the widget's sky is
	 *
	 * @return The transparency, from 0 (opaque) to 100 (invisible)
	 */
	public int getBackgroundTransparency() {
		return backgroundTransparency;
	}
	
	/**
	 * Whether a one-row widget may show more than the temperature when it has the room
	 *
	 * @return True to show the place, the range and the next hours as the width allows
	 */
	public boolean getShowDetails() {
		return showDetails;
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
				  ", backgroundTransparency=" + backgroundTransparency +
				  ", showDetails=" + showDetails +
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
		json.put("backgroundTransparency", backgroundTransparency);
		json.put("showDetails", showDetails);
		
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
		
		// Compared by value: != on two Strings compares references, so a settings object read
		// back from storage never matched the one that was saved.
		if (!placeId.equals(that.placeId)) return false;
		if (backgroundTransparency != that.backgroundTransparency) return false;
		if (showDetails != that.showDetails) return false;
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
		result += backgroundTransparency;
		result += showDetails;
		return result.hashCode();
	}
}
