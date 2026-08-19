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

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.repositories.FormattingService
import java.util.TimeZone

/**
 * HourlyWeatherForecastsView
 *
 * A horizontally scrollable list of hourly weather forecast items.
 * Each item displays weather data (temperature, humidity, pressure, wind, etc.)
 * with smooth graph curves connecting adjacent items.
 *
 * @param hourlyWeatherForecasts The list of hourly forecasts from the Place proto.
 * @param sunriseTimestamps      Sunrise timestamps (one per day), in milliseconds.
 * @param sunsetTimestamps       Sunset timestamps (one per day), in milliseconds.
 * @param formattingService      The formatting service for unit conversion and display.
 * @param timeZone               The timezone of the place.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Composable
fun HourlyWeatherForecastsView(
    modifier: Modifier = Modifier,
    hourlyWeatherForecasts: List<HourlyForecast> = emptyList(),
    sunriseTimestamps: List<Long> = emptyList(),
    sunsetTimestamps: List<Long> = emptyList(),
    formattingService: FormattingService,
    timeZone: TimeZone = TimeZone.getDefault()
) {
    if (hourlyWeatherForecasts.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    val itemDataList = remember(hourlyWeatherForecasts, sunriseTimestamps, sunsetTimestamps, timeZone) {
        HourlyWeatherForecastDataBuilder.buildList(
            hourlyForecasts = hourlyWeatherForecasts,
            sunriseTimestamps = sunriseTimestamps,
            sunsetTimestamps = sunsetTimestamps,
            timeZone = timeZone
        )
    }

    LazyRow(modifier = modifier) {
        items(
            count = itemDataList.size,
            key = { index -> itemDataList[index].hourlyWeatherForecast.dt }
        ) { index ->
            HourlyWeatherForecastItemView(
                formattingService = formattingService,
                textMeasurer = textMeasurer,
                data = itemDataList[index]
            )
        }
    }
}

@Preview
@Composable
fun HourlyWeatherForecastsViewPreview() {
    HourlyWeatherForecastsView(
        formattingService = FormattingService.getDumbInstance(LocalContext.current)
    )
}