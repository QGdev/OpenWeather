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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Spacer
import fr.qgdev.openweather.ui.place.ExtremeTemperature
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.conditionSky
import fr.qgdev.openweather.ui.utils.countryNameFromCode
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.isDaytime
import java.util.SimpleTimeZone
import java.util.TimeZone

/**
 * One place, in full.
 *
 * Replaces the card that expanded in place. A card that unfolded into forecasts, air quality and
 * alerts had to be both a summary and a screen; separating them lets the list stay scannable and
 * gives everything below the header the room it needs.
 *
 * The order answers questions by how soon they matter: what it is like now, what is about to
 * happen (alerts, then the next hour), then the next two days by the hour, then the week, and last
 * the air, which changes slowly.
 */
@Composable
fun PlaceDetailScreen(
    place: Place,
    formattingService: FormattingService,
    onBack: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenAirQuality: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val timeZone: TimeZone = SimpleTimeZone(place.properties.timeOffset, "")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen)
    ) {
        item(key = "header") {
            DetailHeader(place, formattingService, onBack)
        }

        if (place.weatherAlertsListCount > 0) {
            item(key = "alerts") {
                AlertsEntry(
                    count = place.weatherAlertsListCount,
                    onClick = onOpenAlerts,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        item(key = "nowcast") {
            NowcastCard(
                minutely = place.minutelyForecastListList,
                formattingService = formattingService,
                timeZone = timeZone,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        //  Right after the next hour: whether the air is fit to go out in is read before the
        //  forecasts, not scrolled to past them.
        if (place.hasAirQuality()) {
            item(key = "air") {
                AirQualitySummaryCard(
                    airQuality = place.airQuality,
                    onClick = onOpenAirQuality,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        item(key = "hourly") {
            HourlySection(
                place = place,
                formattingService = formattingService,
                timeZone = timeZone,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item(key = "daily") {
            DailySection(
                place = place,
                formattingService = formattingService,
                timeZone = timeZone,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item(key = "bottom-spacer") {
            Box(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * The place, its sky, and the six measurements worth reading before anything else.
 *
 * The header carries the condition wash so the screen is recognisably the same place as the card
 * that opened it; the tiles sit on a scrim over that wash rather than on the wash itself, which is
 * what keeps a six-value grid legible over an amber noon or a violet storm.
 */
/**
 * The header's temperature as the mock-up sets it: the whole degrees large, then the decimal and
 * the unit smaller and top-aligned, "20" "{,9}{°C}". The whole degrees are what is read.
 */
@Composable
private fun HeaderTemperature(formatted: String, unit: String) {
    val palette = LocalWeatherPalette.current
    val cleaned = formatted.removeSuffix("°")
    val separatorIndex = cleaned.indexOfFirst { it == ',' || it == '.' }
    val whole = if (separatorIndex >= 0) cleaned.substring(0, separatorIndex) else cleaned
    val decimal = if (separatorIndex >= 0) cleaned.substring(separatorIndex) else ""

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = whole,
            color = palette.textPrimary,
            fontSize = 62.sp,
            lineHeight = 62.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1.5).sp
        )
        Text(
            text = buildAnnotatedString {
                append(decimal)
                withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, color = palette.textSecondary)) {
                    append(unit)
                }
            },
            color = palette.textPrimary,
            fontSize = 27.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** How far the header's sky takes to fade into the screen's ground. */
private val HEADER_FADE = 56.dp

@Composable
private fun DetailHeader(
    place: Place,
    formattingService: FormattingService,
    onBack: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val currentWeather = place.currentWeather
    val sky = conditionSky(currentWeather.weatherCode, currentWeather.isDaytime(), currentWeather.cloudiness)
    val today = place.dailyForecastListList.firstOrNull()

    Box(
        modifier = Modifier
            .background(sky.wash)
            //  The sky fades into the screen's ground over its last stretch, rather than stopping
            //  on a hard edge above the sections. Drawn from the palette's ground, so it holds on
            //  the light theme as on the dark one.
            .drawBehind {
                val fade = HEADER_FADE.toPx()
                drawRect(
                    Brush.verticalGradient(
                        colors = listOf(palette.screen.copy(alpha = 0f), palette.screen),
                        startY = size.height - fade,
                        endY = size.height
                    )
                )
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-80).dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(sky.haloBrush)
        )

        //  The bottom padding leaves room for the fade under the tiles rather than behind them.
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 44.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .border(1.dp, palette.outlineStrong, RoundedCornerShape(11.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = palette.textPrimary,
                        fontSize = 16.sp
                    )
                }
                Column(modifier = Modifier.padding(start = 13.dp)) {
                    Text(
                        text = place.geolocation.city,
                        color = palette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            countryNameFromCode(place.geolocation.countryCode).ifEmpty { null },
                            currentWeather.weatherDescription.ifEmpty { null }
                        ).joinToString(" · "),
                        color = palette.textSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = getDrawableResIdFromWeatherCode(
                            currentWeather.weatherCode,
                            currentWeather.isDaytime()
                        )
                    ),
                    contentDescription = currentWeather.weatherDescription,
                    modifier = Modifier.size(84.dp)
                )
                HeaderTemperature(
                    formatted = formattingService.getFloatFormattedTemperature(
                        currentWeather.temperature, FormattingSpec.NO_UNIT_NO_SPACE
                    ),
                    unit = formattingService.temperatureUnitLabel
                )
                if (today != null) {
                    //  Against the right edge, as in the mock-up: the day's range frames the reading
                    //  rather than trailing it.
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        ExtremeTemperature(
                            iconRes = R.drawable.temperature_maximum_material,
                            value = formattingService.getFloatFormattedTemperature(
                                today.temperatureMaximum, FormattingSpec.NO_UNIT_NO_SPACE
                            ),
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        )
                        ExtremeTemperature(
                            iconRes = R.drawable.temperature_minimum_material,
                            value = formattingService.getFloatFormattedTemperature(
                                today.temperatureMinimum, FormattingSpec.NO_UNIT_NO_SPACE
                            ),
                            color = palette.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            //  Six tiles, three to a row: the four from the card plus gust and visibility, which
            //  the old card only revealed once expanded.
            val tiles = listOf(
                Triple(
                    stringResource(R.string.title_temperature_feelslike),
                    //  The degree sign belongs to the unit line below, not to the value as well.
                    formattingService.getFloatFormattedTemperature(
                        currentWeather.temperatureFeelsLike, FormattingSpec.NO_UNIT_NO_SPACE
                    ).removeSuffix("°"),
                    formattingService.temperatureUnitLabel
                ),
                Triple(
                    stringResource(R.string.label_wind),
                    formattingService.getFloatFormattedSpeed(
                        currentWeather.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE
                    ),
                    formattingService.speedUnitLabel + " · " + formattingService.getFormattedDirection(
                        currentWeather.windDirection, currentWeather.isWindDirectionReadable
                    )
                ),
                Triple(
                    stringResource(R.string.label_gusts),
                    formattingService.getFloatFormattedSpeed(
                        currentWeather.windGustSpeed, FormattingSpec.NO_UNIT_NO_SPACE
                    ),
                    formattingService.speedUnitLabel
                ),
                Triple(
                    stringResource(R.string.title_humidity),
                    currentWeather.humidity.toString(),
                    "%"
                ),
                Triple(
                    stringResource(R.string.title_pressure),
                    formattingService.getFormattedPressure(
                        currentWeather.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE
                    ),
                    formattingService.pressureUnitLabel
                ),
                Triple(
                    stringResource(R.string.title_visibility),
                    formattingService.getIntFormattedDistance(
                        currentWeather.visibility.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE
                    ),
                    formattingService.distanceUnitLabel
                )
            )

            //  A hairline between the reading and its measures, as in the mock-up.
            HorizontalDivider(modifier = Modifier.padding(top = 20.dp), color = palette.outline)
            Column(
                modifier = Modifier.padding(top = 15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiles.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (label, value, unit) ->
                            MeasureTile(
                                label = label,
                                value = value,
                                unit = unit,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasureTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.tileScrim)
            .border(1.dp, palette.outlineStrong, RoundedCornerShape(16.dp))
            //  Lines set tight, as in the mock-up: Material's default line heights (24 sp for a
            //  16 sp value) made the tiles nearly square where the design has them wider than tall.
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        //  A shade lighter than the unit below it, and spaced, as the mock-up sets the labels.
        Text(
            text = label.uppercase(),
            color = lerp(palette.textSecondary, palette.textPrimary, 0.3f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.63.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = palette.textPrimary,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = unit,
            color = palette.textSecondary,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The way into the alerts, with how many are active - the alerts themselves live in their dialog. */
@Composable
private fun AlertsEntry(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.orange.copy(alpha = 0.12f))
            .border(1.dp, palette.orange.copy(alpha = 0.40f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.danger),
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.orange),
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.title_dialog_weather_alert),
                color = palette.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = pluralStringResource(R.plurals.status_active_alerts, count, count),
                color = palette.textSecondary,
                fontSize = 11.sp
            )
        }
        Text(text = "›", color = palette.orange, fontSize = 14.sp)
    }
}

/** The index, what it means, and which pollutant is driving it. The six scales live one tap away. */
@Composable
private fun AirQualitySummaryCard(
    airQuality: fr.qgdev.openweather.data.models.AirQuality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val labelRes = airQualityLabelRes(airQuality.aqi)
    val indexColor = palette.aqiColor(airQuality.aqi) ?: palette.textMuted
    val dominant = dominantPollutant(airQuality)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_air_quality),
                color = palette.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.action_details) + " ›",
                color = palette.textSecondary,
                fontSize = 12.sp
            )
        }

        Row(
            modifier = Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (labelRes != null) airQuality.aqi.toString() else "—",
                color = indexColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium
            )
            Column {
                Text(
                    text = labelRes?.let { stringResource(it) } ?: "",
                    color = palette.textPrimary,
                    fontSize = 13.sp
                )
                if (dominant != null && labelRes != null) {
                    Text(
                        text = stringResource(R.string.label_dominant_pollutant, dominant.label),
                        color = palette.textSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        //  Five levels, so each step is a fifth of the bar.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.outline)
        ) {
            if (labelRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(airQuality.aqi / 5f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(indexColor)
                )
            }
        }
    }
}
