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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.MinutelyForecast
import fr.qgdev.openweather.ui.format.FormattingService
import fr.qgdev.openweather.ui.format.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import java.util.Date
import java.util.TimeZone

/**
 * The next hour, minute by minute.
 *
 * These sixty points have always been downloaded and stored, and until now only the developer tool
 * ever showed them. They answer the one question the hourly forecast cannot: not "will it rain this
 * evening" but "should I leave now".
 *
 * The sentence comes first and the bars justify it - the reader takes the sentence and goes. Bars
 * rather than a curve, because the data is per-minute and discontinuous: a smoothed line would
 * invent values between the samples.
 *
 * The card only appears when precipitation is expected within the hour. A dry hour has nothing
 * to say that the header does not already, and absent coverage - common outside supported
 * regions - would otherwise draw a flat, falsely reassuring hour.
 */
@Composable
fun NowcastCard(
    minutely: List<MinutelyForecast>,
    formattingService: FormattingService,
    timeZone: TimeZone,
    modifier: Modifier = Modifier
) {
    if (minutely.none { it.precipitation > 0f }) return

    val palette = LocalWeatherPalette.current
    val values = minutely.map { it.precipitation }
    val peak = values.max()
    val peakIndex = values.indexOf(peak)
    val total = values.sum() / 60f       // mm/h sampled per minute, so the hour's accumulation
    val firstWet = values.indexOfFirst { it > 0f }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.accent.copy(alpha = 0.10f))
            .border(1.dp, palette.accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_next_hour),
                color = palette.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${formattingService.shortDistanceUnitLabel}/h",
                color = palette.textSecondary,
                fontSize = 10.5.sp
            )
        }

        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = when {
                firstWet == 0 -> stringResource(R.string.nowcast_falling_now)
                else -> pluralStringResource(R.plurals.nowcast_starting_in, firstWet, firstWet)
            },
            color = palette.textSecondary,
            fontSize = 12.5.sp
        )

        if (peak > 0f) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(64.dp)
            ) {
                val gap = 1.5.dp.toPx()
                val barWidth = (size.width - gap * (values.size - 1)) / values.size
                values.forEachIndexed { index, value ->
                    val ratio = (value / peak).coerceIn(0f, 1f)
                    val barHeight = size.height * ratio
                    if (barHeight <= 0f) return@forEachIndexed
                    drawRoundRect(
                        color = palette.accent,
                        topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(stringResource(R.string.label_now), "+15", "+30", "+45", "+60").forEach {
                    Text(text = it, color = palette.textSecondary, fontSize = 10.sp)
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NowcastFigure(
                    label = stringResource(R.string.label_nowcast_total),
                    value = formattingService.getFloatFormattedShortDistance(
                        total, FormattingSpec.UNIT_AND_SPACE
                    )
                )
                NowcastFigure(
                    label = stringResource(R.string.label_nowcast_peak),
                    value = formattingService.getFloatFormattedShortDistance(
                        peak, FormattingSpec.NO_UNIT_NO_SPACE
                    ) + " " + formattingService.shortDistanceUnitLabel + "/h",
                    suffix = formattingService.getFormattedTime(
                        Date(minutely[peakIndex].dt),
                        timeZone
                    )
                )
            }
        }
    }
}

@Composable
private fun NowcastFigure(label: String, value: String, suffix: String? = null) {
    val palette = LocalWeatherPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(text = label, color = palette.textSecondary, fontSize = 11.5.sp)
        Text(text = value, color = palette.textPrimary, fontSize = 11.5.sp)
        if (suffix != null) {
            Text(text = suffix, color = palette.textSecondary, fontSize = 11.5.sp)
        }
    }
}
