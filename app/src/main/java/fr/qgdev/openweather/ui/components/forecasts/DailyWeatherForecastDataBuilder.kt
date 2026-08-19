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

package fr.qgdev.openweather.ui.components.forecasts

import android.util.Log
import androidx.collection.floatListOf
import androidx.compose.ui.util.fastFlatMap
import fr.qgdev.openweather.data.models.DailyForecast
import java.util.TimeZone

/**
 * DailyWeatherForecastDataBuilder
 *
 * Builds a list of [DailyWeatherForecastItemViewData] from raw daily forecast data.
 * Computes global graph bounds (so all columns share the same Y-axis scale)
 * and per-item graph segments for smooth curves.
 *
 * Daily forecasts have 4 temperature points per day (morning, day, evening, night)
 * which are expanded into the temperature graph arrays.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
object DailyWeatherForecastDataBuilder {

    /**
     * Builds the complete list of view data for each daily forecast item.
     *
     * @param dailyForecasts The daily forecast list from the Place proto.
     * @param timeZone       The timezone of the place.
     * @return A list of [DailyWeatherForecastItemViewData] ready for rendering.
     */
    fun buildList(
        dailyForecasts: List<DailyForecast>,
        timeZone: TimeZone
    ): List<DailyWeatherForecastItemViewData> {
        if (dailyForecasts.isEmpty()) return emptyList()

        // Temperature arrays: 4 points per day (morning, day, evening, night)
        val allTemperatures = dailyForecasts.flatMap {
            listOf(it.temperatureMorning, it.temperatureDay, it.temperatureEvening, it.temperatureNight)
        }
        val allFeelsLike = dailyForecasts.flatMap {
            listOf(
                it.temperatureMorningFeelsLike, it.temperatureDayFeelsLike,
                it.temperatureEveningFeelsLike, it.temperatureNightFeelsLike
            )
        }

        // Simple arrays: 1 point per day
        val windSpeeds = dailyForecasts.map { it.windSpeed }
        val windGusts = dailyForecasts.map { it.windGustSpeed }
        val rains = dailyForecasts.map { it.rain }
        val snows = dailyForecasts.map { it.snow }
        val pops = dailyForecasts.map { it.pop }

        // Global bounds
        val tempBounds = ForecastGraphUtils.computeBounds(allTemperatures + allFeelsLike)
        val windBounds = ForecastGraphUtils.computeBounds(windSpeeds + windGusts)
        val precipBounds = ForecastGraphUtils.computeBounds(rains + snows)

        return dailyForecasts.mapIndexed { index, forecast ->
            // Temperature graph: 4 points for THIS day (morning, day, evening, night)
            val startIdx = index * 4
            val endIdx = startIdx + 3

            //  With this data set [A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z]
            // For example,
            // For the begin, we duplicate, the first point so [A, B, C, D] -> [A, A, B, C, D, E]
            // For the end, we duplicate, the last point so [W, X, Y, Z] -> [V, W, X, Y, Z, Z]
            // For other cases, we take the outer points so [N,O,P,Q] -> [M, N, O, P, Q, R]

            // In resume, we will take the night before and the morning after, in order to generate
            // partials beziers

            val idxBefore = if (startIdx > 0) startIdx - 1 else 0
            val idxAfter = if (endIdx < dailyForecasts.size - 1) endIdx + 1 else endIdx

            Log.i("DailyDataBuilder", "%d -> %d [%d, %d, %d, %d] %d".format(index, idxBefore, startIdx, startIdx + 1, startIdx + 2, startIdx + 3, idxAfter))

            val temperatureGraphPoints: FloatArray = floatArrayOf(
                allTemperatures[idxBefore],
                *(allTemperatures.subList(startIdx, endIdx).toFloatArray()),
                allTemperatures[idxAfter])

            val feelsLikeTemperatureGraphPoints: FloatArray = floatArrayOf(
                allFeelsLike[idxBefore],
                *(allFeelsLike.subList(startIdx, endIdx).toFloatArray()),
                allFeelsLike[idxAfter])

            val uniqueDataIdxBefore = if (index > 0) index - 1 else 0
            val uniqueDataIdxAfter = if (index < dailyForecasts.size - 1) index + 1 else index

            val windSpeedGraphPoints: FloatArray = floatArrayOf(
                windSpeeds[uniqueDataIdxBefore],
                windSpeeds[index],
                windSpeeds[uniqueDataIdxAfter])

            val windGustSpeedGraphPoints: FloatArray = floatArrayOf(
                windGusts[uniqueDataIdxBefore],
                windGusts[index],
                windGusts[uniqueDataIdxAfter])


            val rainGraphPoints: FloatArray = floatArrayOf(
                rains[uniqueDataIdxBefore],
                rains[index],
                rains[uniqueDataIdxAfter])

            val snowGraphPoints: FloatArray = floatArrayOf(
                snows[uniqueDataIdxBefore],
                snows[index],
                snows[uniqueDataIdxAfter])

            Log.i("DailyDataBuilder", "Bounds: temp=%.1f-%.1f, wind=%.1f-%.1f, precip=%.1f-%.1f".format(
                tempBounds.first, tempBounds.second,
                windBounds.first, windBounds.second,
                precipBounds.first, precipBounds.second
            ))

            DailyWeatherForecastItemViewData(
                dailyWeatherForecast = forecast,
                temperatureGraphPoints = temperatureGraphPoints,
                feelsLikeTemperatureGraphPoints = feelsLikeTemperatureGraphPoints,
                temperatureGraphBounds = tempBounds,
                windSpeedGraphPoints = windSpeedGraphPoints,
                windGustSpeedGraphPoints = windGustSpeedGraphPoints,
                windSpeedGraphBounds = windBounds,
                rainGraphPoints = rainGraphPoints,
                snowGraphPoints = snowGraphPoints,
                popGraphPoint = pops[index],
                precipitationGraphBounds = precipBounds,
                timeZone = timeZone
            )
        }
    }
}
