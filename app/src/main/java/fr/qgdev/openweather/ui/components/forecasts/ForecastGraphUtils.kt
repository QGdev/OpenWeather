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

/**
 * ForecastGraphUtils
 *
 * Shared utility functions for building forecast graph data.
 * This is the Compose-era equivalent of the shared logic that was
 * in the legacy `ForecastView` Java base class.
 *
 * Provides:
 * - Global min/max bounds computation across value arrays
 * - 3-point graph segment extraction (previous, current, next)
 *   for smooth Bézier curve rendering between adjacent items
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
object ForecastGraphUtils {

    /**
     * Computes the min/max bounds across a list of values.
     * Used so all graph columns share the same Y-axis scale.
     *
     * @param values All values to consider (can merge multiple metrics, e.g. temp + feelsLike)
     * @return A [Pair] of (min, max). Returns (0, 0) if the list is empty.
     */
    fun computeBounds(values: List<Float>): Pair<Float, Float> {
        if (values.isEmpty()) return 0f to 0f
        return values.min() to values.max()
    }

    /**
     * Extracts 3 graph points (left boundary, current, right boundary) for a given index.
     *
     * To ensure smooth curve connections between adjacent columns,
     * boundary values are computed as the midpoint of the current and
     * neighboring values — mirroring the original Java ForecastView
     * where a single cubic Bézier spanned columns with control points
     * at the column boundaries.
     *
     * Column N's right boundary = (values[N] + values[N+1]) / 2
     * Column N+1's left boundary = (values[N] + values[N+1]) / 2
     * → They match → curves connect seamlessly.
     *
     * @param values The full array of values across all items.
     * @param index  The current item index.
     * @return A list of 3 floats: [leftBoundary, current, rightBoundary].
     */
    fun getGraphPoints(values: List<Float>, index: Int): List<Float> {
        val prev = if (index > 0) values[index - 1] else values[index]
        val current = values[index]
        val next = if (index < values.size - 1) values[index + 1] else values[index]

        val leftBoundary = (prev + current) / 2f
        val rightBoundary = (current + next) / 2f

        return listOf(leftBoundary, current, rightBoundary)
    }
}



