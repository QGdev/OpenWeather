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
package fr.qgdev.openweather.ui.components.dialogs

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.ui.adapter.WeatherAlertAdapter
import fr.qgdev.openweather.repositories.FormattingService

/**
 * WeatherAlertDialog
 *
 *
 * Weather Alert dialog box where all weather alerts informations are presented<br></br>
 *
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Dialog
 */
class WeatherAlertDialog(context: Context, place: Place, formattingService: FormattingService) :
    Dialog(context) {
    /**
     * WeatherAlertDialog Constructor
     *
     *
     * Just the constructor of WeatherAlertDialog class
     *
     *
     * @param context           Context of the application in order to get resources
     * @param place             Just to get Weather alerts information
     * @param formattingService In order to get well formatted values
     * @apiNote None of the parameters can be null
     */
    init {
        setContentView(R.layout.dialog_weather_alert)


        //	Set exit button behavior
        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener { v: View? -> dismiss() }


        //	Fill weather alerts section
        val alertListLinearLayout = findViewById<LinearLayout>(R.id.alertList)
        val weatherAlertAdapter =
            WeatherAlertAdapter(
                context,
                place,
                formattingService
            )
        for (i in 0 until place.weatherAlertsListCount) {
            alertListLinearLayout.addView(weatherAlertAdapter.getView(i, null, null), i)
        }
    }

    /**
     * build()
     *
     *
     * Just the function to build the whole SnackBar but in this case, the dialog, will be built and shown
     *
     */
    fun build() {
        show()
    }
}
