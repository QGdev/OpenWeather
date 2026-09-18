/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.data.settings

import java.util.Locale

data class Settings(
    val temperatureUnit: TemperatureSettings = TemperatureSettings.CELSIUS,
    val measureUnit: MeasureSettings = MeasureSettings.METRIC,
    val pressureUnit: PressureSettings = PressureSettings.HECTOPASCAL,
    val windDirectionUnit: WindDirectionSettings = WindDirectionSettings.CARDINAL_POINTS,
    val timeFormat: TimeSettings = TimeSettings.TWENTY_FOUR_HOURS,
    val defaultLocale: Locale = Locale.getDefault(),
    val apiKey: String? = null,
    val periodicUpdateEnabled: Boolean = false,
    val updatePeriod: UpdatePeriodSettings = UpdatePeriodSettings.THIRTY_MINUTES,
    /**
     * The app version the onboarding was last completed with, never shown as a setting. Unset on
     * a fresh install or after the data is cleared, which is what brings the onboarding up. Kept
     * as a version rather than a flag, so a later onboarding can tell who saw which one.
     */
    val onboardingVersion: String? = null,
    /**
     * Whether the 1-minute update interval is offered. A debugging aid switched on from the
     * debug menu, off by default: it burns through the free API quota in an afternoon.
     */
    val oneMinuteUpdateAllowed: Boolean = false
)