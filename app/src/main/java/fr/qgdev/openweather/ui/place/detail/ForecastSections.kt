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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.DailyForecast
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.components.forecasts.components.MoonPhaseIndicator
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.conditionFamily
import fr.qgdev.openweather.ui.theme.sky
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.toPercentage
import java.util.Date
import java.util.TimeZone

/** One labelled group of readings for a single hour or day. */
internal data class ReadingGroup(val title: String, val rows: List<Reading>)

/** One reading: what it is, what it says, in what unit. */
internal data class Reading(val label: String, val value: String, val unit: String)

/**
 * The next 48 hours as a strip, with one hour opened at a time.
 *
 * The old column stacked all eighteen fields under every hour, so reading Thursday afternoon meant
 * scrolling past everything about Thursday morning. The strip carries only what distinguishes one
 * hour from the next - time, sky, temperature, chance of rain - and the hour the reader picks is
 * spelled out underneath in full.
 */
@Composable
internal fun HourlySection(
    place: Place,
    formattingService: FormattingService,
    timeZone: TimeZone,
    modifier: Modifier = Modifier
) {
    val hours = place.hourlyForecastListList
    if (hours.isEmpty()) return

    val palette = LocalWeatherPalette.current
    var selected by remember(place.geolocation.city) { mutableIntStateOf(0) }
    val sunrises = place.dailyForecastListList.map { it.sunriseDt }
    val sunsets = place.dailyForecastListList.map { it.sunsetDt }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(20.dp))
            .padding(vertical = 15.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.title_forecast_hourly),
            trailing = stringResource(R.string.label_hours_count, hours.size)
        )

        LazyRow(
            modifier = Modifier.padding(top = 12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(hours.size) { index ->
                HourChip(
                    forecast = hours[index],
                    formattingService = formattingService,
                    timeZone = timeZone,
                    isDaytime = isDaytimeAt(hours[index].dt, sunrises, sunsets),
                    selected = index == selected,
                    onClick = { selected = index }
                )
            }
        }

        SelectedHour(
            forecast = hours[selected.coerceIn(hours.indices)],
            formattingService = formattingService,
            timeZone = timeZone,
            isDaytime = isDaytimeAt(hours[selected.coerceIn(hours.indices)].dt, sunrises, sunsets)
        )
    }
}

@Composable
private fun HourChip(
    forecast: HourlyForecast,
    formattingService: FormattingService,
    timeZone: TimeZone,
    isDaytime: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val sky = conditionFamily(forecast.weatherCode, isDaytime).sky()
    val probability = forecast.pop.toPercentage()

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(sky.wash)
            .border(
                width = 1.dp,
                color = if (selected) palette.accent else sky.border,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattingService.getFormattedHour(Date(forecast.dt), timeZone),
            color = palette.textSecondary,
            fontSize = 10.5.sp
        )
        Image(
            painter = painterResource(
                id = getDrawableResIdFromWeatherCode(forecast.weatherCode, isDaytime)
            ),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(26.dp)
        )
        Text(
            text = formattingService.getFloatFormattedTemperature(
                forecast.temperature, FormattingSpec.NO_UNIT_NO_SPACE
            ),
            color = palette.textPrimary,
            fontSize = 13.sp
        )
        //  The chance of rain, as a bar and as a figure: the bar is read at a glance across the
        //  strip, the figure answers "how much" without counting pixels.
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(28.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(probability / 100f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.accent)
            )
        }
        Text(
            text = formattingService.getIntFormattedPercentage(
                probability, FormattingSpec.UNIT_BUT_NO_SPACE
            ),
            color = if (probability >= 50f) palette.accent else palette.textQuiet,
            fontSize = 9.5.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun SelectedHour(
    forecast: HourlyForecast,
    formattingService: FormattingService,
    timeZone: TimeZone,
    isDaytime: Boolean
) {
    val palette = LocalWeatherPalette.current

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(
                    id = getDrawableResIdFromWeatherCode(forecast.weatherCode, isDaytime)
                ),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
            Column(modifier = Modifier.padding(start = 11.dp).weight(1f)) {
                Text(
                    text = formattingService.getFormattedHour(Date(forecast.dt), timeZone),
                    color = palette.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = forecast.weatherDescription.ifEmpty {
                        stringResource(R.string.label_selected_hour)
                    },
                    color = palette.textSecondary,
                    fontSize = 10.5.sp
                )
            }
            Text(
                text = formattingService.getFloatFormattedTemperature(
                    forecast.temperature, FormattingSpec.UNIT_BUT_NO_SPACE
                ),
                color = palette.textPrimary,
                fontSize = 20.sp
            )
        }

        ReadingGroups(
            groups = hourlyGroups(forecast, formattingService),
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

/**
 * The eight days, each opening onto everything known about it.
 *
 * The bar between the two temperatures places each day on one scale shared by the week, so a cold
 * day is short and to the left without having to read the numbers.
 */
@Composable
internal fun DailySection(
    place: Place,
    formattingService: FormattingService,
    timeZone: TimeZone,
    modifier: Modifier = Modifier
) {
    val days = place.dailyForecastListList
    if (days.isEmpty()) return

    val palette = LocalWeatherPalette.current
    var expanded by remember(place.geolocation.city) { mutableStateOf(-1) }

    val coldest = days.minOf { it.temperatureMinimum }
    val warmest = days.maxOf { it.temperatureMaximum }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(20.dp))
            .padding(vertical = 15.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.title_forecast_daily),
            trailing = stringResource(
                R.string.label_common_scale,
                formattingService.getFloatFormattedTemperature(coldest, FormattingSpec.NO_UNIT_NO_SPACE),
                formattingService.getFloatFormattedTemperature(warmest, FormattingSpec.UNIT_BUT_NO_SPACE)
            )
        )

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            days.forEachIndexed { index, day ->
                DayRow(
                    day = day,
                    formattingService = formattingService,
                    timeZone = timeZone,
                    coldest = coldest,
                    warmest = warmest,
                    expanded = expanded == index,
                    onClick = { expanded = if (expanded == index) -1 else index }
                )
            }
        }
    }
}

@Composable
private fun DayRow(
    day: DailyForecast,
    formattingService: FormattingService,
    timeZone: TimeZone,
    coldest: Float,
    warmest: Float,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val sky = conditionFamily(day.weatherCode, true).sky()
    val span = (warmest - coldest).takeIf { it > 0f } ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sky.wash)
            .border(
                width = 1.dp,
                color = if (expanded) palette.accent else sky.border,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.width(74.dp)) {
                Text(
                    text = formattingService.getFormattedShortDayName(Date(day.dt), timeZone),
                    color = palette.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formattingService.getFormattedDayMonth(Date(day.dt), timeZone),
                    color = palette.textSecondary,
                    fontSize = 10.sp
                )
            }
            Image(
                painter = painterResource(
                    id = getDrawableResIdFromWeatherCode(day.weatherCode, true)
                ),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = formattingService.getFloatFormattedTemperature(
                    day.temperatureMinimum, FormattingSpec.NO_UNIT_NO_SPACE
                ),
                color = palette.textSecondary,
                fontSize = 11.sp
            )
            TemperatureSpan(
                fraction = (day.temperatureMinimum - coldest) / span,
                width = (day.temperatureMaximum - day.temperatureMinimum) / span,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formattingService.getFloatFormattedTemperature(
                    day.temperatureMaximum, FormattingSpec.NO_UNIT_NO_SPACE
                ),
                color = palette.textPrimary,
                fontSize = 11.sp
            )
        }

        if (expanded) {
            Column(modifier = Modifier.padding(start = 13.dp, end = 13.dp, bottom = 14.dp)) {
                if (day.weatherDescription.isNotEmpty()) {
                    Text(
                        text = day.weatherDescription,
                        color = palette.textSecondary,
                        fontSize = 11.5.sp
                    )
                }

                DayTemperatures(
                    day = day,
                    formattingService = formattingService,
                    accent = sky.icon,
                    modifier = Modifier.padding(top = 10.dp)
                )

                ReadingGroups(
                    groups = dailyGroups(day, formattingService, timeZone),
                    modifier = Modifier.padding(top = 16.dp)
                )

                MoonPhase(
                    phase = day.moonPhase,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}

/** The day's range, placed on the week's scale. */
@Composable
private fun TemperatureSpan(fraction: Float, width: Float, modifier: Modifier = Modifier) {
    val palette = LocalWeatherPalette.current
    //  Laid out in three weighted parts - before, the day's range, after - so the bar scales with
    //  the row rather than with a fixed width.
    val before = fraction.coerceIn(0f, 1f)
    val span = width.coerceIn(0.02f, 1f - before)
    val after = (1f - before - span).coerceAtLeast(0f)

    Row(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(palette.outline)
    ) {
        if (before > 0f) Box(modifier = Modifier.weight(before))
        Box(
            modifier = Modifier
                .weight(span)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.accent)
        )
        if (after > 0f) Box(modifier = Modifier.weight(after))
    }
}

/**
 * The day's eight temperatures: four measured, four felt, drawn rather than listed.
 *
 * The header repeats the day's extremes because the curve is scaled to its own day - two days side
 * by side are not comparable by shape alone, and the row above already places them on the week's
 * scale.
 */
@Composable
private fun DayTemperatures(
    day: DailyForecast,
    formattingService: FormattingService,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val convert = { kelvin: Float -> formattingService.convertTemperature(kelvin) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.group_temperatures).uppercase(),
                color = palette.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = formattingService.getFloatFormattedTemperature(
                        day.temperatureMaximum, FormattingSpec.UNIT_BUT_NO_SPACE
                    ),
                    color = palette.textPrimary,
                    fontSize = 10.sp
                )
                Text(
                    text = formattingService.getFloatFormattedTemperature(
                        day.temperatureMinimum, FormattingSpec.UNIT_BUT_NO_SPACE
                    ),
                    color = palette.textSecondary,
                    fontSize = 10.sp
                )
            }
        }

        DayTemperatureCurve(
            actual = listOf(
                convert(day.temperatureMorning),
                convert(day.temperatureDay),
                convert(day.temperatureEvening),
                convert(day.temperatureNight)
            ),
            feelsLike = listOf(
                convert(day.temperatureMorningFeelsLike),
                convert(day.temperatureDayFeelsLike),
                convert(day.temperatureEveningFeelsLike),
                convert(day.temperatureNightFeelsLike)
            ),
            periodLabels = listOf(
                stringResource(R.string.label_morning),
                stringResource(R.string.title_daily_forecast_noon),
                stringResource(R.string.title_daily_forecast_evening),
                stringResource(R.string.title_daily_forecast_night)
            ),
            format = { value -> formattingService.formatConvertedTemperature(value) },
            accent = accent,
            valueColor = palette.textPrimary,
            secondaryColor = palette.textSecondary,
            pointFill = palette.screen,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CurveLegend(stringResource(R.string.label_real), accent, dashed = false)
            CurveLegend(
                stringResource(R.string.title_temperature_feelslike),
                palette.textSecondary,
                dashed = true
            )
        }
    }
}

/**
 * The moon phase, drawn and named.
 *
 * It sits on its own rather than in the value column: "Waning gibbous" does not fit where a
 * temperature does, and the phase is one of the few readings a picture states better than a word.
 */
@Composable
private fun MoonPhase(phase: Float, modifier: Modifier = Modifier) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MoonPhaseIndicator(
            modifier = Modifier.size(34.dp),
            moonPhase = phase
        )
        Column {
            Text(
                text = stringResource(R.string.label_moon_phase).uppercase(),
                color = palette.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(moonPhaseLabel(phase)),
                color = palette.textPrimary,
                fontSize = 12.5.sp
            )
        }
    }
}

@Composable
private fun CurveLegend(label: String, color: Color, dashed: Boolean) {
    val palette = LocalWeatherPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (dashed) color.copy(alpha = 0.55f) else color)
        )
        Text(text = label, color = palette.textSecondary, fontSize = 9.5.sp)
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = trailing, color = palette.textSecondary, fontSize = 10.5.sp)
    }
}

/** Labelled groups, two columns, the way a spec sheet reads. */
@Composable
private fun ReadingGroups(groups: List<ReadingGroup>, modifier: Modifier = Modifier) {
    val palette = LocalWeatherPalette.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        groups.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                pair.forEach { group ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.title.uppercase(),
                            color = palette.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(
                            modifier = Modifier.padding(top = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            group.rows.forEach { reading ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = reading.label,
                                        color = palette.textSecondary,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = reading.value,
                                        color = palette.textPrimary,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = reading.unit,
                                        color = palette.textSecondary,
                                        fontSize = 9.5.sp,
                                        modifier = Modifier.width(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (pair.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Every one of the eighteen hourly fields, grouped. */
@Composable
private fun hourlyGroups(
    forecast: HourlyForecast,
    formatting: FormattingService
): List<ReadingGroup> = listOf(
    ReadingGroup(
        stringResource(R.string.group_temperatures),
        listOf(
            Reading(
                stringResource(R.string.title_temperature_feelslike),
                formatting.getFloatFormattedTemperature(forecast.temperatureFeelsLike, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.temperatureUnitLabel
            ),
            Reading(
                stringResource(R.string.label_dew_point),
                formatting.getFloatFormattedTemperature(forecast.dewPoint, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.temperatureUnitLabel
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_wind),
        listOf(
            Reading(
                stringResource(R.string.title_wind_speed),
                formatting.getFloatFormattedSpeed(forecast.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_gust_speed),
                formatting.getFloatFormattedSpeed(forecast.windGustSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_direction),
                formatting.getFormattedDirectionInCardinalPoints(forecast.windDirection),
                formatting.getFormattedDirectionInDegrees(forecast.windDirection)
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_atmosphere),
        listOf(
            Reading(
                stringResource(R.string.title_humidity),
                forecast.humidity.toString(), "%"
            ),
            Reading(
                stringResource(R.string.title_pressure),
                formatting.getFormattedPressure(forecast.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.pressureUnitLabel
            ),
            Reading(
                stringResource(R.string.title_cloudiness),
                forecast.cloudiness.toString(), "%"
            ),
            Reading(
                stringResource(R.string.title_visibility),
                formatting.getIntFormattedDistance(forecast.visibility.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.distanceUnitLabel
            ),
            Reading(
                stringResource(R.string.label_uv_index),
                forecast.uvIndex.toString(),
                stringResource(uvLevelLabel(forecast.uvIndex))
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_precipitations),
        listOf(
            Reading(
                stringResource(R.string.label_probability),
                formatting.getIntFormattedPercentage(forecast.pop.toPercentage(), FormattingSpec.NO_UNIT_NO_SPACE),
                "%"
            ),
            Reading(
                stringResource(R.string.title_precipitation_rain),
                formatting.getFloatFormattedShortDistance(forecast.rain, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            ),
            Reading(
                stringResource(R.string.title_precipitation_snow),
                formatting.getFloatFormattedShortDistance(forecast.snow, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            )
        )
    )
)

/** Every one of the thirty daily fields, grouped. */
@Composable
private fun dailyGroups(
    day: DailyForecast,
    formatting: FormattingService,
    timeZone: TimeZone
): List<ReadingGroup> = listOf(
    ReadingGroup(
        stringResource(R.string.group_wind),
        listOf(
            Reading(
                stringResource(R.string.title_wind_speed),
                formatting.getFloatFormattedSpeed(day.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_gust_speed),
                formatting.getFloatFormattedSpeed(day.windGustSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_direction),
                formatting.getFormattedDirectionInCardinalPoints(day.windDirection),
                formatting.getFormattedDirectionInDegrees(day.windDirection)
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_atmosphere),
        listOf(
            Reading(stringResource(R.string.title_humidity), day.humidity.toString(), "%"),
            Reading(
                stringResource(R.string.label_dew_point),
                formatting.getFloatFormattedTemperature(day.dewPoint, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.temperatureUnitLabel
            ),
            Reading(
                stringResource(R.string.title_pressure),
                formatting.getFormattedPressure(day.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.pressureUnitLabel
            ),
            Reading(stringResource(R.string.title_cloudiness), day.cloudiness.toString(), "%"),
            Reading(
                stringResource(R.string.label_uv_index),
                day.uvIndex.toString(),
                stringResource(uvLevelLabel(day.uvIndex))
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_precipitations),
        listOf(
            Reading(
                stringResource(R.string.label_probability),
                formatting.getIntFormattedPercentage(day.pop.toPercentage(), FormattingSpec.NO_UNIT_NO_SPACE),
                "%"
            ),
            Reading(
                stringResource(R.string.title_precipitation_rain),
                formatting.getFloatFormattedShortDistance(day.rain, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            ),
            Reading(
                stringResource(R.string.title_precipitation_snow),
                formatting.getFloatFormattedShortDistance(day.snow, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_sun_and_moon),
        listOf(
            Reading(
                stringResource(R.string.title_sunrise),
                formatting.getFormattedTime(Date(day.sunriseDt), timeZone), ""
            ),
            Reading(
                stringResource(R.string.title_sunset),
                formatting.getFormattedTime(Date(day.sunsetDt), timeZone), ""
            ),
            Reading(
                stringResource(R.string.title_moonrise),
                formatting.getFormattedTime(Date(day.moonriseDt), timeZone), ""
            ),
            Reading(
                stringResource(R.string.title_moonset),
                formatting.getFormattedTime(Date(day.moonsetDt), timeZone), ""
            )
        )
    )
)

/**
 * Whether a forecast hour falls in daylight, from the sunrises and sunsets of the days around it.
 *
 * Each day's pair covers that day only, so the nearest sunrise before the hour and the nearest
 * sunset after it are what decide - simply comparing against day zero would call every hour after
 * tomorrow's sunset "night". Before any of them, the first one to come tells: a sunrise means night.
 */
internal fun isDaytimeAt(timestamp: Long, sunrises: List<Long>, sunsets: List<Long>): Boolean {
    val lastSunrise = sunrises.filter { it <= timestamp }.maxOrNull()
    val lastSunset = sunsets.filter { it <= timestamp }.maxOrNull()
    return when {
        //  Before the first event of the strip: the sun's state is the opposite of that event.
        lastSunrise == null && lastSunset == null ->
            (sunrises.minOrNull() ?: Long.MAX_VALUE) > (sunsets.minOrNull() ?: Long.MAX_VALUE)
        lastSunrise == null -> false
        lastSunset == null -> true
        else -> lastSunrise > lastSunset
    }
}
