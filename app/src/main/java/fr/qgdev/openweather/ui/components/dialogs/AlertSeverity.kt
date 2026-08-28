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

package fr.qgdev.openweather.ui.components.dialogs

import androidx.annotation.StringRes
import fr.qgdev.openweather.R
import java.util.Locale

/**
 * How serious an alert is, as far as can be told.
 *
 * OpenWeatherMap carries no severity field (see the design brief, §4.8): the only signal is the
 * wording an agency chose for the event, and European services overwhelmingly use the traffic-light
 * names - "Yellow Thunderstorm Warning", "Alerte orange". Those words are matched, in the handful
 * of languages the app is likely to meet, and anything unrecognised stays [UNKNOWN] rather than
 * being guessed at: colouring an alert wrongly is worse than not colouring it.
 */
enum class AlertSeverity(@param:StringRes val labelRes: Int?) {
    RED(R.string.alert_severity_red),
    ORANGE(R.string.alert_severity_orange),
    YELLOW(R.string.alert_severity_yellow),
    UNKNOWN(null)
}

private val RED_WORDS = listOf("red", "rouge", "rot", "rojo", "rosso")
private val ORANGE_WORDS = listOf("orange", "arancione", "naranja")
private val YELLOW_WORDS = listOf("yellow", "jaune", "gelb", "amarillo", "giallo")

/** Reads the severity out of an alert's event wording. */
fun severityOf(event: String): AlertSeverity {
    val words = event.lowercase(Locale.ROOT).split(Regex("[^\\p{L}]+"))
    return when {
        words.any { it in RED_WORDS } -> AlertSeverity.RED
        words.any { it in ORANGE_WORDS } -> AlertSeverity.ORANGE
        words.any { it in YELLOW_WORDS } -> AlertSeverity.YELLOW
        else -> AlertSeverity.UNKNOWN
    }
}
