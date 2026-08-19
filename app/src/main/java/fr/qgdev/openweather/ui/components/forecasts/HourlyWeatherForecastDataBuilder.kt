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

import fr.qgdev.openweather.data.models.HourlyForecast
import java.util.TimeZone
import kotlin.math.min

/**
 * HourlyWeatherForecastDataBuilder
 *
 * Builds a list of [HourlyWeatherForecastItemViewData] from raw forecast data.
 * Computes global graph bounds (so all columns share the same Y-axis scale)
 * and per-item 3-point graph segments (previous, current, next) for smooth curves.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
object HourlyWeatherForecastDataBuilder {

    /**
     * Builds the complete list of view data for each hourly forecast item.
     *
     * @param hourlyForecasts  The hourly forecast list from the Place proto.
     * @param sunriseTimestamps Sunrise timestamps (one per day), in milliseconds.
     * @param sunsetTimestamps  Sunset timestamps (one per day), in milliseconds.
     * @param timeZone          The timezone of the place.
     * @return A list of [HourlyWeatherForecastItemViewData] ready for rendering.
     */
    fun buildList(
        hourlyForecasts: List<HourlyForecast>,
        sunriseTimestamps: List<Long>,
        sunsetTimestamps: List<Long>,
        timeZone: TimeZone
    ): List<HourlyWeatherForecastItemViewData> {
        if (hourlyForecasts.isEmpty()) return emptyList()

        // Extract raw value arrays
        val temperatures = hourlyForecasts.map { it.temperature }
        val feelsLike = hourlyForecasts.map { it.temperatureFeelsLike }
        val humidities = hourlyForecasts.map { it.humidity.toFloat() }
        val pressures = hourlyForecasts.map { it.pressure.toFloat() }
        val windSpeeds = hourlyForecasts.map { it.windSpeed }
        val windGusts = hourlyForecasts.map { it.windGustSpeed }
        val rains = hourlyForecasts.map { it.rain }
        val snows = hourlyForecasts.map { it.snow }

        // Compute global bounds (shared across all items for consistent graph scaling)
        val tempBounds = ForecastGraphUtils.computeBounds(temperatures + feelsLike)
        val humidityBounds = ForecastGraphUtils.computeBounds(humidities)
        val pressureBounds = ForecastGraphUtils.computeBounds(pressures)
        val windBounds = ForecastGraphUtils.computeBounds(windSpeeds + windGusts)
        val precipBounds = ForecastGraphUtils.computeBounds(rains + snows)

        // Compute isDayTime for each hourly item
        val isDayTimeArray = computeIsDayTime(hourlyForecasts, sunriseTimestamps, sunsetTimestamps)

        return hourlyForecasts.mapIndexed { index, forecast ->
            val idxBefore = if (index > 0) index - 1 else 0
            val idxAfter = if (index < hourlyForecasts.size - 1) index + 1 else index

            HourlyWeatherForecastItemViewData(
                hourlyWeatherForecast = forecast,
                temperatureGraphPoints = floatArrayOf(
                    temperatures[idxBefore],
                    temperatures[index],
                    temperatures[idxAfter]
                ),
                feelsLikeTemperatureGraphPoints = floatArrayOf(
                    feelsLike[idxBefore],
                    feelsLike[index],
                    feelsLike[idxAfter]
                ),
                temperatureGraphBounds = tempBounds,
                humidityGraphPoints = floatArrayOf(
                    humidities[idxBefore],
                    humidities[index],
                    humidities[idxAfter]
                ),
                humidityGraphBounds = humidityBounds,
                pressureGraphPoints = floatArrayOf(
                    pressures[idxBefore],
                    pressures[index],
                    pressures[idxAfter]
                ),
                pressureGraphBounds = pressureBounds,
                windSpeedGraphPoints = floatArrayOf(
                    windSpeeds[idxBefore],
                    windSpeeds[index],
                    windSpeeds[idxAfter]
                ),
                windGustSpeedGraphPoints = floatArrayOf(
                    windGusts[idxBefore],
                    windGusts[index],
                    windGusts[idxAfter]
                ),
                windSpeedGraphBounds = windBounds,
                rainGraphPoints = floatArrayOf(
                    rains[idxBefore],
                    rains[index],
                    rains[idxAfter]
                ),
                snowGraphPoints = floatArrayOf(
                    snows[idxBefore],
                    snows[index],
                    snows[idxAfter]
                ),
                precipitationGraphBounds = precipBounds,
                isDayTime = isDayTimeArray[index],
                timeZone = timeZone
            )
        }
    }

    /**
     * Computes whether each hourly forecast falls during daytime.
     * An hourly timestamp is considered daytime if it falls between any
     * sunrise/sunset pair.
     */
    private fun computeIsDayTime(
        forecasts: List<HourlyForecast>,
        sunriseTimestamps: List<Long>,
        sunsetTimestamps: List<Long>
    ): BooleanArray {
        val result = BooleanArray(forecasts.size)
        val dayCount = min(sunriseTimestamps.size, sunsetTimestamps.size)

        for (i in forecasts.indices) {
            val dt = forecasts[i].dt
            result[i] = (0 until dayCount).any { d ->
                sunriseTimestamps[d] < dt && dt < sunsetTimestamps[d]
            }
        }

        return result
    }
}
