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

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
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
import androidx.compose.material3.MaterialTheme
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
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.conditionSky
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.toPercentage
import java.util.Calendar
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    //  48 hours cross midnight once or twice, and nothing used to say so: from 23h
                    //  the strip went on to 0h of a day it never named. A marker now opens each new
                    //  day, reckoned in the place's own time zone.
                    if (index > 0 && startsNewDay(hours[index - 1].dt, hours[index].dt, timeZone)) {
                        DayMarker(Date(hours[index].dt), formattingService, timeZone)
                    }
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
        }

        SelectedHour(
            forecast = hours[selected.coerceIn(hours.indices)],
            formattingService = formattingService,
            timeZone = timeZone,
            isDaytime = isDaytimeAt(hours[selected.coerceIn(hours.indices)].dt, sunrises, sunsets)
        )
    }
}

/** Whether two instants fall on different calendar days in [timeZone]. */
internal fun startsNewDay(previous: Long, current: Long, timeZone: TimeZone): Boolean {
    val calendar = Calendar.getInstance(timeZone)
    calendar.timeInMillis = previous
    val previousDay = calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
    calendar.timeInMillis = current
    return previousDay != calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
}

/** The start of a new day in the hour strip: its short name and date, over a thin rule. */
@Composable
private fun DayMarker(date: Date, formattingService: FormattingService, timeZone: TimeZone) {
    val palette = LocalWeatherPalette.current
    Column(
        modifier = Modifier.width(34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattingService.getFormattedShortDayName(date, timeZone),
            color = palette.textPrimary,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = formattingService.getFormattedDayMonth(date, timeZone),
            color = palette.textSecondary,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(1.dp)
                .height(84.dp)
                .background(palette.outlineStrong)
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
    val sky = conditionSky(forecast.weatherCode, isDaytime, forecast.cloudiness)
    val probability = forecast.pop.toPercentage()

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(sky.wash)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else sky.border,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            //  The other hours step back, as in the mock-up: the one opened below stands out.
            .alpha(if (selected) 1f else 0.6f)
            .padding(top = 11.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattingService.getFormattedShortHour(Date(forecast.dt), timeZone),
            color = palette.textSecondary,
            fontSize = 10.5.sp,
            lineHeight = 13.sp
        )
        Image(
            painter = painterResource(
                id = getDrawableResIdFromWeatherCode(forecast.weatherCode, isDaytime)
            ),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(22.dp)
        )
        Text(
            text = formattingService.getFloatFormattedTemperature(
                forecast.temperature, FormattingSpec.NO_UNIT_NO_SPACE
            ),
            color = palette.textPrimary,
            fontSize = 14.sp,
            lineHeight = 17.sp
        )
        //  The chance of rain, as a bar and as a figure: the bar is read at a glance across the
        //  strip, the figure answers "how much" without counting pixels.
        //  In the hour's own colour, as in the mock-up, like the day bars below.
        Box(
            modifier = Modifier
                .padding(top = 9.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(probability / 100f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sky.icon)
            )
        }
        Text(
            text = formattingService.getIntFormattedPercentage(
                probability, FormattingSpec.UNIT_AND_SPACE
            ),
            color = if (probability >= 50f) sky.icon else palette.textSecondary,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 5.dp)
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

    //  Under a hairline, as in the mock-up: "À 16h", then what the panel is.
    HorizontalDivider(modifier = Modifier.padding(top = 14.dp), color = palette.outline)
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(
                    id = getDrawableResIdFromWeatherCode(forecast.weatherCode, isDaytime)
                ),
                contentDescription = forecast.weatherDescription,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.padding(start = 11.dp).weight(1f)) {
                //  "À 0h" and, beside it in the secondary colour, the day it belongs to: past
                //  midnight the hour alone no longer says which day is being read.
                val date = Date(forecast.dt)
                Text(
                    text = buildAnnotatedString {
                        append(
                            stringResource(
                                R.string.label_at_hour,
                                formattingService.getFormattedShortHour(date, timeZone)
                            )
                        )
                        withStyle(SpanStyle(color = palette.textSecondary, fontWeight = FontWeight.Normal, fontSize = 12.sp)) {
                            append(
                                "  " + formattingService.getFormattedShortDayName(date, timeZone) +
                                        " " + formattingService.getFormattedDayMonth(date, timeZone)
                            )
                        }
                    },
                    color = palette.textPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.label_selected_hour),
                    color = palette.textSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            //  The value large and its unit small beside it, as in the mock-up: the number is what
            //  is read, the unit only confirms it.
            Text(
                text = buildAnnotatedString {
                    append(
                        formattingService.getFloatFormattedTemperature(
                            forecast.temperature, FormattingSpec.NO_UNIT_NO_SPACE
                        ).removeSuffix("°")
                    )
                    withStyle(SpanStyle(fontSize = 11.5.sp, color = palette.textSecondary)) {
                        append(" " + formattingService.temperatureUnitLabel)
                    }
                },
                color = palette.textPrimary,
                fontSize = 20.sp
            )
        }

        ReadingGroups(
            groups = hourlyGroups(forecast, formattingService),
            modifier = Modifier.padding(top = 16.dp)
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
            //  The week's extremes, which are also the ends of the grey track every day's bar sits on:
            //  "min 11,3° · max 27,8°" reads plainly where "shared scale 11,3° → 27,8°C" did not.
            trailing = stringResource(
                R.string.label_week_min_max,
                formattingService.getFloatFormattedTemperature(coldest, FormattingSpec.NO_UNIT_NO_SPACE),
                formattingService.getFloatFormattedTemperature(warmest, FormattingSpec.NO_UNIT_NO_SPACE)
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
    val sky = conditionSky(day.weatherCode, true, day.cloudiness)
    val span = (warmest - coldest).takeIf { it > 0f } ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sky.wash)
            .border(
                width = 1.dp,
                color = if (expanded) MaterialTheme.colorScheme.primary else sky.border,
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
                warmEnd = sky.icon,
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
            //  A hairline under the day's row, as in the mock-up, rather than the condition's words:
            //  the icon on the row already says it.
            HorizontalDivider(color = palette.outline)
            Column(modifier = Modifier.padding(start = 13.dp, end = 13.dp, top = 14.dp, bottom = 14.dp)) {
                DayTemperatures(
                    day = day,
                    formattingService = formattingService,
                    accent = sky.icon
                )

                ReadingGroups(
                    groups = dailyGroups(day, formattingService, timeZone),
                    modifier = Modifier.padding(top = 16.dp)
                )

            }
        }
    }
}

/**
 * The day's range, placed on the week's scale: the grey track runs from the week's coldest minimum
 * to its warmest maximum (the "shared scale" in the section's header), the coloured part from this
 * day's minimum to its maximum. A cooler day sits further left, a day of wider swings is longer.
 */
@Composable
private fun TemperatureSpan(fraction: Float, width: Float, warmEnd: Color, modifier: Modifier = Modifier) {
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
                //  From the cool blue of the minimum to the day's own colour at the maximum, as in
                //  the mock-up.
                .background(Brush.horizontalGradient(listOf(palette.accent, warmEnd)))
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
            //  "max 22,5°  min 12,0°": labelled, since the curve below is read against them.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    R.string.label_max to day.temperatureMaximum,
                    R.string.label_min to day.temperatureMinimum
                ).forEach { (label, value) ->
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(label) + " ")
                            withStyle(SpanStyle(color = palette.textPrimary)) {
                                append(formattingService.getFloatFormattedTemperature(value, FormattingSpec.NO_UNIT_NO_SPACE))
                            }
                        },
                        color = palette.textSecondary,
                        fontSize = 10.sp
                    )
                }
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        groups.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                pair.forEach { group ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.title.uppercase(),
                            color = lerp(palette.textSecondary, palette.textPrimary, 0.3f),
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            //  One line per reading - label, value, unit in a fixed column - so the
                            //  values of a group line up; a long label is cut rather than wrapped.
                            //  The unit column only where the group has units: sun and moon has none,
                            //  and the room is better given to "Lever de lune".
                            val hasUnits = group.rows.any { it.unit.isNotEmpty() }
                            group.rows.forEach { reading ->
                                //  Aligned on the text baseline: label, value and unit have three sizes,
                                //  and aligning their boxes left them visibly off one another.
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = reading.label,
                                        color = palette.textSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f)
                                            .alignByBaseline()
                                    )
                                    Text(
                                        text = reading.value,
                                        color = palette.textPrimary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 1,
                                        modifier = Modifier.alignByBaseline()
                                    )
                                    if (hasUnits) {
                                        Text(
                                            text = reading.unit,
                                            color = palette.textSecondary,
                                            fontSize = 9.5.sp,
                                            lineHeight = 16.sp,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .width(26.dp)
                                                .alignByBaseline()
                                        )
                                    }
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
        stringResource(R.string.group_temperature),
        listOf(
            Reading(
                stringResource(R.string.label_actual),
                formatting.getFloatFormattedTemperature(forecast.temperature, FormattingSpec.NO_UNIT_NO_SPACE).removeSuffix("°"),
                formatting.temperatureUnitLabel
            ),
            Reading(
                stringResource(R.string.title_temperature_feelslike),
                formatting.getFloatFormattedTemperature(forecast.temperatureFeelsLike, FormattingSpec.NO_UNIT_NO_SPACE).removeSuffix("°"),
                formatting.temperatureUnitLabel
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
                stringResource(R.string.label_rain),
                formatting.getFloatFormattedShortDistance(forecast.rain, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            ),
            Reading(
                stringResource(R.string.label_snow),
                formatting.getFloatFormattedShortDistance(forecast.snow, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
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
                stringResource(R.string.label_gusts),
                formatting.getFloatFormattedSpeed(forecast.windGustSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_direction),
                formatting.getFormattedDirection(forecast.windDirection, true),
                ""
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_atmosphere),
        listOf(
            Reading(stringResource(R.string.title_humidity), forecast.humidity.toString(), "%"),
            Reading(
                stringResource(R.string.title_pressure),
                formatting.getFormattedPressure(forecast.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.pressureUnitLabel
            ),
            Reading(
                stringResource(R.string.label_dew_point),
                formatting.getFloatFormattedTemperature(forecast.dewPoint, FormattingSpec.NO_UNIT_NO_SPACE).removeSuffix("°"),
                formatting.temperatureUnitLabel
            ),
            Reading(
                stringResource(R.string.title_visibility),
                formatting.getIntFormattedDistance(forecast.visibility.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.distanceUnitLabel
            ),
            Reading(stringResource(R.string.label_uv_index), forecast.uvIndex.toString(), ""),
            Reading(stringResource(R.string.label_clouds), forecast.cloudiness.toString(), "%")
        )
    )
)

/**
 * The day's fields, grouped as in the mock-up: precipitation, wind, atmosphere, then sun and moon.
 * The temperatures are not repeated here - the curve above carries them.
 */
@Composable
private fun dailyGroups(
    day: DailyForecast,
    formatting: FormattingService,
    timeZone: TimeZone
): List<ReadingGroup> = listOf(
    ReadingGroup(
        stringResource(R.string.group_precipitations),
        listOf(
            Reading(
                stringResource(R.string.label_probability),
                formatting.getIntFormattedPercentage(day.pop.toPercentage(), FormattingSpec.NO_UNIT_NO_SPACE),
                "%"
            ),
            Reading(
                stringResource(R.string.label_rain),
                formatting.getFloatFormattedShortDistance(day.rain, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            ),
            Reading(
                stringResource(R.string.label_snow),
                formatting.getFloatFormattedShortDistance(day.snow, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.shortDistanceUnitLabel
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_wind),
        listOf(
            Reading(
                stringResource(R.string.title_wind_speed),
                formatting.getFloatFormattedSpeed(day.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.label_gusts),
                formatting.getFloatFormattedSpeed(day.windGustSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.speedUnitLabel
            ),
            Reading(
                stringResource(R.string.title_wind_direction),
                formatting.getFormattedDirection(day.windDirection, true),
                ""
            )
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_atmosphere),
        listOf(
            Reading(stringResource(R.string.title_humidity), day.humidity.toString(), "%"),
            Reading(
                stringResource(R.string.title_pressure),
                formatting.getFormattedPressure(day.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                formatting.pressureUnitLabel
            ),
            Reading(
                stringResource(R.string.label_dew_point),
                formatting.getFloatFormattedTemperature(day.dewPoint, FormattingSpec.NO_UNIT_NO_SPACE).removeSuffix("°"),
                formatting.temperatureUnitLabel
            ),
            Reading(stringResource(R.string.label_uv_index), day.uvIndex.toString(), ""),
            Reading(stringResource(R.string.label_clouds), day.cloudiness.toString(), "%")
        )
    ),
    ReadingGroup(
        stringResource(R.string.group_sun_and_moon),
        listOf(
            Reading(stringResource(R.string.label_sunrise), formatting.getFormattedTime(Date(day.sunriseDt), timeZone), ""),
            Reading(stringResource(R.string.label_sunset), formatting.getFormattedTime(Date(day.sunsetDt), timeZone), ""),
            Reading(stringResource(R.string.title_moonrise), formatting.getFormattedTime(Date(day.moonriseDt), timeZone), ""),
            Reading(stringResource(R.string.title_moonset), formatting.getFormattedTime(Date(day.moonsetDt), timeZone), ""),
            Reading(
                stringResource(R.string.label_phase),
                stringResource(moonPhaseShortLabel(day.moonPhase)) +
                        if (moonPhaseShowsIllumination(day.moonPhase)) " ${moonIllumination(day.moonPhase)} %" else "",
                ""
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
