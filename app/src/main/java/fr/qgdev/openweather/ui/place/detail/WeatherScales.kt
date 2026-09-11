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
 * The phase in the few characters a reading row has, as the mock-up writes it: "gib. 72 %",
 * "dern. quart.". New, full and the quarters are named alone; the others carry how much of the
 * disc is lit, since "crescent" alone does not say whether it is a sliver or nearly half.
 */
@StringRes
fun moonPhaseShortLabel(phase: Float): Int {
    val wrapped = phase.mod(1f)
    return when {
        wrapped < 0.02f || wrapped > 0.98f -> R.string.moon_short_new
        wrapped < 0.23f -> R.string.moon_short_waxing_crescent
        wrapped < 0.27f -> R.string.moon_short_first_quarter
        wrapped < 0.48f -> R.string.moon_short_waxing_gibbous
        wrapped < 0.52f -> R.string.moon_short_full
        wrapped < 0.73f -> R.string.moon_short_waning_gibbous
        wrapped < 0.77f -> R.string.moon_short_last_quarter
        else -> R.string.moon_short_waning_crescent
    }
}

/** Whether [moonPhaseShortLabel] is followed by the lit share: not for new, full and the quarters. */
fun moonPhaseShowsIllumination(phase: Float): Boolean {
    val wrapped = phase.mod(1f)
    return !(wrapped < 0.02f || wrapped > 0.98f || wrapped in 0.23f..0.27f ||
            wrapped in 0.48f..0.52f || wrapped in 0.73f..0.77f)
}

/** The lit share of the disc, 0 to 100, for a phase from 0 (new) through 0.5 (full) to 1. */
fun moonIllumination(phase: Float): Int =
    ((1 - kotlin.math.cos(2 * Math.PI * phase.mod(1f))) / 2 * 100).toInt()
