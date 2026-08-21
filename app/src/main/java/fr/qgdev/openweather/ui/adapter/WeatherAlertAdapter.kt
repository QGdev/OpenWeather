/*
 *  Copyright (c) 2019 - 2026
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

package fr.qgdev.openweather.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.WeatherAlert
import fr.qgdev.openweather.repositories.FormattingService
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone

/**
 * WeatherAlertAdapter
 *
 * Renders the weather alerts held by a [Place] into `adapter_weather_alert.xml`.
 *
 * This is still a View-based [BaseAdapter] rather than a composable: the weather alerts feature has
 * not been ported to Compose yet. `PlaceCardView` already draws an alert icon and button, but
 * neither is wired to anything, so this adapter and [fr.qgdev.openweather.ui.components.dialogs.WeatherAlertDialog]
 * remain the only working alerts UI. See `PORTING.md`.
 *
 * @param context Context used to inflate the item layout and resolve the link colour
 * @param place Place whose alerts are displayed; also supplies the time offset used for dates
 * @param formattingService Formats the start and end dates according to the user's settings
 * @author Quentin GOMES DOS REIS
 * @version 2
 * @see BaseAdapter
 */
class WeatherAlertAdapter(
    private val context: Context,
    private val place: Place,
    private val formattingService: FormattingService
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val weatherAlertsList: List<WeatherAlert> = place.weatherAlertsListList

    /**
     * @return The number of weather alerts held by the place
     */
    override fun getCount(): Int = weatherAlertsList.size

    /**
     * @param position Index of the wanted item
     * @return The weather alert at the given position
     */
    override fun getItem(position: Int): WeatherAlert = weatherAlertsList[position]

    /**
     * Not implemented: the alerts have no stable identifier of their own.
     *
     * @param position Index of the wanted item id
     * @return Always 0
     */
    override fun getItemId(position: Int): Long = 0

    /**
     * Builds the view for one alert.
     *
     * @param position Index of the wanted item
     * @param convertView A recycled view to reuse, or null to inflate a new one
     * @param parent The parent view; may be null when the caller inflates detached views
     * @return The completed view
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.adapter_weather_alert, parent)
        val currentWeatherAlert = getItem(position)
        //  SimpleTimeZone rejects a null ID - TimeZone.setID throws NPE - so pass an empty one,
        //  matching how PlaceCardView already builds its zones. The Java original passed null and
        //  would have crashed too; it never ran because nothing ever opened this dialog.
        val timeZone: TimeZone = SimpleTimeZone(place.properties.timeOffset, "")

        val descriptionTextView = view.findViewById<TextView>(R.id.description)
        descriptionTextView.linksClickable = true
        descriptionTextView.setLinkTextColor(context.getColor(R.color.colorAccent))

        view.findViewById<TextView>(R.id.event).text = currentWeatherAlert.event
        view.findViewById<TextView>(R.id.sender).text = currentWeatherAlert.sender
        view.findViewById<TextView>(R.id.start_date).text =
            formattingService.getFormattedFullTimeHour(Date(currentWeatherAlert.startDt), timeZone)
        view.findViewById<TextView>(R.id.end_date).text =
            formattingService.getFormattedFullTimeHour(Date(currentWeatherAlert.endDt), timeZone)
        descriptionTextView.text = currentWeatherAlert.description

        return view
    }
}
