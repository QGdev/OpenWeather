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

package fr.qgdev.openweather.ui.place

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
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
import java.util.Date
import java.util.TimeZone

/**
 * A place, as a card carrying its own sky.
 *
 * Replaces the card that expanded in place. Tapping it now opens the place's own screen, so the
 * card only has to answer "what is it like there, right now" - the hero is simply the same card at
 * a larger size, not a different one, because a list where the first entry shows the weather and
 * the rest show a single number reads as two unrelated things.
 *
 * Values older than [STALE_AFTER_MILLIS] are labelled rather than silently shown as current, and a
 * place whose last update failed keeps its previous values with a line saying so: an empty card
 * would lose information the app already has.
 */
@Composable
fun PlaceSkyCard(
    place: Place,
    formattingService: FormattingService,
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    updateFailed: Boolean = false,
    onClick: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val palette = LocalWeatherPalette.current
    val currentWeather = place.currentWeather
    val sky = conditionSky(
        currentWeather.weatherCode,
        currentWeather.isDaytime(),
        currentWeather.cloudiness
    )

    val alertCount = place.weatherAlertsListCount
    val today = place.dailyForecastListList.firstOrNull()

    val lastUpdate = place.properties.lastSuccessfulWeatherUpdateTime
    val isStale = lastUpdate > 0L &&
            System.currentTimeMillis() - lastUpdate > STALE_AFTER_MILLIS
    val lastUpdateLabel =
        if (lastUpdate > 0L) formattingService.getFormattedTime(Date(lastUpdate), TimeZone.getDefault())
        else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (hero) 24.dp else 22.dp))
            .background(sky.wash)
            .border(1.dp, sky.border, RoundedCornerShape(if (hero) 24.dp else 22.dp))
            .clickable(onClick = onClick)
    ) {
        Box {
            //  The condition's glow, off the top-right corner, behind everything else.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-70).dp)
                    .size(if (hero) 200.dp else 170.dp)
                    .clip(CircleShape)
                    .background(sky.haloBrush)
            )

            Column(modifier = Modifier.padding(if (hero) 18.dp else 16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = place.geolocation.city,
                                color = palette.textPrimary,
                                fontSize = if (hero) 19.sp else 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (alertCount > 0) AlertBadge(alertCount)
                        }
                        Text(
                            text = conditionLine(place),
                            color = palette.textSecondary,
                            fontSize = if (hero) 11.5.sp else 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isStale && lastUpdateLabel.isNotEmpty() && !updateFailed) {
                        StaleChip(label = lastUpdateLabel)
                    }
                }

                Row(
                    modifier = Modifier.padding(top = if (hero) 16.dp else 12.dp),
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
                        modifier = Modifier.size(if (hero) 62.dp else 48.dp)
                    )
                    Temperature(
                        formatted = formattingService.getFloatFormattedTemperature(
                            currentWeather.temperature,
                            FormattingSpec.NO_UNIT_NO_SPACE
                        ),
                        unit = formattingService.temperatureUnitLabel,
                        wholeSize = if (hero) 44.sp else 31.sp,
                        decimalSize = if (hero) 20.sp else 15.sp,
                        unitSize = if (hero) 14.sp else 11.5.sp
                    )
                    if (today != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            ExtremeTemperature(
                                iconRes = R.drawable.temperature_maximum_material,
                                value = formattingService.getFloatFormattedTemperature(
                                    today.temperatureMaximum, FormattingSpec.NO_UNIT_NO_SPACE
                                ),
                                color = palette.textPrimary,
                                fontSize = if (hero) 13.sp else 12.sp
                            )
                            ExtremeTemperature(
                                iconRes = R.drawable.temperature_minimum_material,
                                value = formattingService.getFloatFormattedTemperature(
                                    today.temperatureMinimum, FormattingSpec.NO_UNIT_NO_SPACE
                                ),
                                color = palette.textSecondary,
                                fontSize = if (hero) 13.sp else 12.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (hero) 16.dp else 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Measure(
                        label = stringResource(R.string.title_temperature_feelslike),
                        value = formattingService.getFloatFormattedTemperature(
                            currentWeather.temperatureFeelsLike, FormattingSpec.NO_UNIT_NO_SPACE
                        ),
                        unit = "",
                        hero = hero
                    )
                    Measure(
                        label = stringResource(R.string.title_humidity),
                        value = currentWeather.humidity.toString(),
                        unit = "%",
                        hero = hero
                    )
                    Measure(
                        label = stringResource(R.string.title_pressure),
                        value = formattingService.getFormattedPressure(
                            currentWeather.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE
                        ),
                        unit = formattingService.pressureUnitLabel,
                        hero = hero
                    )
                    Measure(
                        label = stringResource(R.string.label_wind),
                        value = formattingService.getFloatFormattedSpeed(
                            currentWeather.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE
                        ),
                        unit = formattingService.getFormattedDirection(
                            currentWeather.windDirection,
                            currentWeather.isWindDirectionReadable
                        ),
                        hero = hero
                    )
                }
            }
        }

        if (updateFailed) {
            UpdateFailedFooter(
                valuesFrom = lastUpdateLabel,
                onRetry = onRetry
            )
        }
    }
}

/** A place added but not downloaded yet: the list takes the user back immediately either way. */
@Composable
fun PendingPlaceCard(
    cityName: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = cityName,
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.status_downloading_forecast),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
        }
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AlertBadge(count: Int) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, palette.amber.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(palette.amber)
        )
        Text(
            text = pluralStringResource(R.plurals.badge_alerts, count, count),
            color = palette.amber,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StaleChip(label: String) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, palette.outlineStrong, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(palette.orange)
        )
        Text(text = label, color = palette.textMuted, fontSize = 10.5.sp)
    }
}

@Composable
private fun UpdateFailedFooter(valuesFrom: String, onRetry: () -> Unit) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.orange.copy(alpha = 0.07f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(palette.orange)
        )
        Text(
            text = stringResource(R.string.status_update_failed_values_from, valuesFrom),
            color = palette.textSecondary,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.action_retry),
            color = palette.orange,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onRetry)
        )
    }
}

/**
 * The temperature, with its decimal and its unit set smaller than the whole part.
 *
 * The formatter returns one string ("13,9°"), so it is split on its own decimal separator rather
 * than re-formatted here - the separator is the locale's, and the degree sign is dropped because
 * the unit is drawn separately.
 */
@Composable
private fun Temperature(
    formatted: String,
    unit: String,
    wholeSize: TextUnit,
    decimalSize: TextUnit,
    unitSize: TextUnit
) {
    val palette = LocalWeatherPalette.current
    val cleaned = formatted.removeSuffix("°")
    val separatorIndex = cleaned.indexOfFirst { it == ',' || it == '.' }
    val whole = if (separatorIndex >= 0) cleaned.substring(0, separatorIndex) else cleaned
    val decimal = if (separatorIndex >= 0) cleaned.substring(separatorIndex) else ""

    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = whole,
            color = palette.textPrimary,
            fontSize = wholeSize,
            fontWeight = FontWeight.Medium
        )
        Column {
            if (decimal.isNotEmpty()) {
                Text(
                    text = decimal,
                    color = palette.textPrimary,
                    fontSize = decimalSize,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(text = unit, color = palette.textSecondary, fontSize = unitSize)
        }
    }
}

@Composable
private fun ExtremeTemperature(
    iconRes: Int,
    value: String,
    color: Color,
    fontSize: TextUnit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color)
        )
        Text(text = value, color = color, fontSize = fontSize)
    }
}

@Composable
private fun Measure(label: String, value: String, unit: String, hero: Boolean) {
    val palette = LocalWeatherPalette.current
    Column {
        Text(
            text = label,
            color = palette.textSecondary,
            fontSize = if (hero) 10.5.sp else 10.sp,
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = palette.textPrimary,
                fontSize = if (hero) 15.sp else 13.5.sp
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = palette.textSecondary,
                    fontSize = if (hero) 11.sp else 10.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

/** "France · partiellement nuageux" - the country resolved, the description as the API worded it. */
@Composable
private fun conditionLine(place: Place): String {
    val country = countryNameFromCode(place.geolocation.countryCode)
    val description = place.currentWeather.weatherDescription
    return when {
        country.isEmpty() -> description
        description.isEmpty() -> country
        else -> "$country · $description"
    }
}

/** Values older than two hours are labelled with the time they were fetched. */
private const val STALE_AFTER_MILLIS = 2 * 60 * 60 * 1000L
