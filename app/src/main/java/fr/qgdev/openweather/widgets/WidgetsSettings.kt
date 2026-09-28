
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

package fr.qgdev.openweather.widgets

import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import org.json.JSONException
import org.json.JSONObject

/**
 * WidgetsSettings
 * <p>
 * 	Used to as a data holder for widgets settings.
 * 	Contains the place id and the widget id.
 * </p>
 *
 * @param placeId                The id of the place
 * @param widgetId               The id of the widget
 * @param backgroundTransparency How transparent the widget's sky is, from 0 (opaque) to 100
 * @param showDetails            Whether a one-row widget may show more than the temperature
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
data class WidgetsSettings(
    val placeId: String,
    val widgetId: Int,
    val backgroundTransparency: Int,
    val showDetails: Boolean,
) {
    init {
        require(widgetId != INVALID_APPWIDGET_ID) { "widgetId must be a valid widget id" }
        require(backgroundTransparency in 0..100) { "backgroundTransparency must be between 0 and 100" }
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject = JSONObject()
        .put("placeId", placeId)
        .put("widgetId", widgetId)
        .put("backgroundTransparency", backgroundTransparency)
        .put("showDetails", showDetails)

    @Throws(JSONException::class)
    fun toJsonString(): String = toJson().toString()

    companion object {
        /** Settings saved before these options existed have none: they were opaque, and showed details. */
        @JvmStatic
        @Throws(JSONException::class)
        fun fromJson(json: JSONObject): WidgetsSettings = WidgetsSettings(
            json.getString("placeId"),
            json.getInt("widgetId"),
            json.optInt("backgroundTransparency", 0),
            json.optBoolean("showDetails", true)
        )
    }
}
