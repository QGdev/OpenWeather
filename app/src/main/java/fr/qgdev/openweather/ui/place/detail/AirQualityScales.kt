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
import fr.qgdev.openweather.data.models.AirQuality

/**
 * The six pollutants the app reports, with the thresholds that separate air quality levels 1 to 5.
 *
 * The boundaries are OpenWeatherMap's own, in µg/m³, and they are wildly different from one
 * pollutant to the next - carbon monoxide's scale is a hundred times the others'. That is exactly
 * why a raw concentration says nothing on its own and every value is placed on its own scale.
 */
enum class Pollutant(
    val label: String,
    @param:StringRes val descriptionRes: Int,
    val boundaries: List<Float>,
    val valueOf: (AirQuality) -> Float
) {
    SO2("SO₂", R.string.title_air_quality_so2, listOf(20f, 80f, 250f, 350f), AirQuality::getSo2),
    NO2("NO₂", R.string.title_air_quality_no2, listOf(40f, 70f, 150f, 200f), AirQuality::getNo2),
    PM10("PM₁₀", R.string.title_air_quality_pm10, listOf(20f, 50f, 100f, 200f), AirQuality::getPm10),
    PM25("PM₂,₅", R.string.title_air_quality_pm25, listOf(10f, 25f, 50f, 75f), AirQuality::getPm25),
    O3("O₃", R.string.title_air_quality_o3, listOf(60f, 100f, 140f, 180f), AirQuality::getO3),
    CO("CO", R.string.title_air_quality_co, listOf(4400f, 9400f, 12400f, 15400f), AirQuality::getCo);

    /** The level, 1 to 5, this concentration falls in. */
    fun levelOf(value: Float): Int = boundaries.count { value >= it } + 1

    /**
     * How far along its own scale this concentration sits, from 0 to 1.
     *
     * Used to compare pollutants with each other, which their raw values cannot do. Above the last
     * boundary the ratio is capped: the scale has no upper bound, and a runaway value should read
     * as "at the top", not as ten times the width of the bar.
     */
    fun severityOf(value: Float): Float =
        (value / boundaries.last()).coerceIn(0f, 1f)
}

/**
 * The pollutant driving the index.
 *
 * The overall index is the worst of the six, not their average, so naming that one pollutant
 * explains an index that would otherwise look arbitrary next to six modest-looking numbers. The
 * level decides, as it does for the index; how far along its scale a pollutant sits only separates
 * those on the same level.
 */
fun dominantPollutant(airQuality: AirQuality): Pollutant? =
    Pollutant.entries.maxWithOrNull(
        compareBy<Pollutant>(
            { it.levelOf(it.valueOf(airQuality)) },
            { it.severityOf(it.valueOf(airQuality)) }
        )
    )

/** The level label resource for an index of 1 to 5, or null when there is no measurement. */
@StringRes
fun airQualityLabelRes(aqi: Int): Int? = when (aqi) {
    1 -> R.string.air_quality_1
    2 -> R.string.air_quality_2
    3 -> R.string.air_quality_3
    4 -> R.string.air_quality_4
    5 -> R.string.air_quality_5
    else -> null
}
