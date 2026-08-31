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

package fr.qgdev.openweather.ui.place.detail

import androidx.annotation.StringRes
import fr.qgdev.openweather.R

/**
 * The World Health Organisation's UV bands.
 *
 * A bare index means nothing to most readers - 3 and 8 are both "a number" - so the band is what
 * actually carries the advice.
 */
@StringRes
fun uvLevelLabel(uvIndex: Int): Int = when {
    uvIndex < 3 -> R.string.uv_level_low
    uvIndex < 6 -> R.string.uv_level_moderate
    uvIndex < 8 -> R.string.uv_level_high
    uvIndex < 11 -> R.string.uv_level_very_high
    else -> R.string.uv_level_extreme
}

/**
 * The moon phase, named.
 *
 * OpenWeatherMap gives a fraction where 0 and 1 are a new moon, 0.25 the first quarter and 0.5 a
 * full moon. Printed as "25 %" it says nothing; the eight traditional names are what a reader
 * recognises. The quarters are given a small tolerance so a value a hair off 0.25 still reads as
 * the first quarter rather than as a crescent.
 */
@StringRes
fun moonPhaseLabel(phase: Float): Int {
    val wrapped = phase.mod(1f)
    return when {
        wrapped < 0.02f || wrapped > 0.98f -> R.string.moon_phase_new
        wrapped < 0.23f -> R.string.moon_phase_waxing_crescent
        wrapped < 0.27f -> R.string.moon_phase_first_quarter
        wrapped < 0.48f -> R.string.moon_phase_waxing_gibbous
        wrapped < 0.52f -> R.string.moon_phase_full
        wrapped < 0.73f -> R.string.moon_phase_waning_gibbous
        wrapped < 0.77f -> R.string.moon_phase_last_quarter
        else -> R.string.moon_phase_waning_crescent
    }
}
