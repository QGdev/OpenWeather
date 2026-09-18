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

package fr.qgdev.openweather.data.settings

/**
 * UpdatePeriodSettings
 *
 * Enumerates the available background update intervals.
 * Each entry carries its duration in milliseconds so the worker can compute
 * the next aligned trigger time without extra look-up logic.
 *
 * @property durationMillis Duration of the interval in milliseconds.
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
enum class UpdatePeriodSettings(val durationMillis: Long) {
    ONE_MINUTE(1 * 60 * 1_000L),  // 🔧 Offered only once enabled in the debug menu - 1 minute
    FIVE_MINUTES(5 * 60 * 1_000L),
    TEN_MINUTES(10 * 60 * 1_000L),
    FIFTEEN_MINUTES(15 * 60 * 1_000L),
    THIRTY_MINUTES(30 * 60 * 1_000L),
    ONE_HOUR(60 * 60 * 1_000L),
    TWO_HOURS(2 * 60 * 60 * 1_000L),
    THREE_HOURS(3 * 60 * 60 * 1_000L),
    SIX_HOURS(6 * 60 * 60 * 1_000L),
    TWELVE_HOURS(12 * 60 * 60 * 1_000L)
}

