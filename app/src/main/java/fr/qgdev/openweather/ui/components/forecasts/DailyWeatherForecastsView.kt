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
import fr.qgdev.openweather.data.models.DailyForecast
import fr.qgdev.openweather.repositories.FormattingService
import java.util.TimeZone

/**
 * DailyWeatherForecastsView
 *
 * A horizontally scrollable list of daily weather forecast items.
 * Each item displays one day's weather data including temperatures (4 periods),
 * environmental variables, wind, and precipitations with graph curves.
 *
 * @param dailyWeatherForecasts The list of daily forecasts from the Place proto.
 * @param formattingService     The formatting service for unit conversion and display.
 * @param timeZone              The timezone of the place.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Composable
fun DailyWeatherForecastsView(
    modifier: Modifier = Modifier,
    dailyWeatherForecasts: List<DailyForecast> = emptyList(),
    formattingService: FormattingService,
    timeZone: TimeZone = TimeZone.getDefault()
) {
    if (dailyWeatherForecasts.isEmpty()) return

    val itemDataList = remember(dailyWeatherForecasts, timeZone) {
        DailyWeatherForecastDataBuilder.buildList(
            dailyForecasts = dailyWeatherForecasts,
            timeZone = timeZone
        )
    }

    LazyRow(
        modifier = modifier) {
        items(
            count = itemDataList.size,
            key = { index -> itemDataList[index].dailyWeatherForecast.dt }
        ) { index ->
            DailyWeatherForecastItemView(
                formattingService = formattingService,
                data = itemDataList[index]
            )
        }
    }
}

@Preview
@Composable
fun DailyWeatherForecastsViewPreview() {
    DailyWeatherForecastsView(
        formattingService = FormattingService.getDumbInstance(LocalContext.current)
    )
}