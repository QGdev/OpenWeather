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

package fr.qgdev.openweather.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.AirQuality
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import fr.qgdev.openweather.ui.place.detail.Pollutant
import fr.qgdev.openweather.ui.place.detail.airQualityLabelRes
import fr.qgdev.openweather.ui.place.detail.dominantPollutant
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import java.util.Locale

/**
 * The air quality in full: the index, then each of the six pollutants with its measured
 * concentration, its level and its thresholds.
 *
 * The middle of three levels. The detail screen's card answers "is the air fit to breathe"; this
 * answers "what is polluting it"; "Understanding the index" ([AirQualityInfoDialog]) answers
 * "what does a 3 mean". The measurements themselves had no screen after the redesign - the card
 * opened the explanations directly.
 */
@Composable
fun AirQualityDetailDialog(
    airQuality: AirQuality,
    onOpenExplainer: () -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    val palette = LocalWeatherPalette.current
    val dominant = dominantPollutant(airQuality)

    FullScreenDialog(
        title = stringResource(R.string.title_air_quality),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            IndexSummary(airQuality, dominant)

            Text(
                text = stringResource(R.string.label_six_pollutants),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.05.sp,
                modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 10.dp)
            )
            //  One card, one row per pollutant: six separate cards made the list taller than the
            //  screen for values that read in a line each.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.outline, RoundedCornerShape(16.dp))
            ) {
                Pollutant.entries.forEachIndexed { index, pollutant ->
                    if (index > 0) HorizontalDivider(color = palette.outline)
                    PollutantRow(pollutant, pollutant.valueOf(airQuality), isDominant = pollutant == dominant)
                }
            }

            ExplainerEntry(onOpenExplainer)
        }
    }
}

/** The index, its label and the dominant pollutant, over the five-step scale it sits on. */
@Composable
private fun IndexSummary(airQuality: AirQuality, dominant: Pollutant?) {
    val palette = LocalWeatherPalette.current
    val labelRes = airQualityLabelRes(airQuality.aqi)
    val indexColor = palette.aqiColor(airQuality.aqi) ?: palette.textMuted
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, palette.outline, shape)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = if (labelRes != null) airQuality.aqi.toString() else "—",
                color = indexColor,
                fontSize = 52.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Medium
            )
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = labelRes?.let { stringResource(it) }.orEmpty(),
                    color = palette.textPrimary,
                    fontSize = 16.sp
                )
                if (dominant != null && labelRes != null) {
                    Text(
                        text = stringResource(R.string.label_dominant_pollutant, dominant.label),
                        color = palette.textSecondary,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }

        //  The five steps as rising bars, the current one lit and marked: where the index sits
        //  reads at a glance, and how far it is from either end.
        Row(
            modifier = Modifier.padding(top = 18.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..5).forEach { level ->
                val color = palette.aqiColor(level) ?: palette.textMuted
                val current = level == airQuality.aqi
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((8 + level * 4).dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color.copy(alpha = if (current) 1f else 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(5.dp)
                            .rotate(45f)
                            .background(if (current) palette.textPrimary else palette.screen.copy(alpha = 0f))
                    )
                    Text(
                        text = level.toString(),
                        color = if (current) palette.textPrimary else palette.textQuiet,
                        fontSize = 10.sp,
                        fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.air_quality_1).lowercase(Locale.getDefault()),
                color = palette.textQuiet,
                fontSize = 9.5.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.air_quality_5).lowercase(Locale.getDefault()),
                color = palette.textQuiet,
                fontSize = 9.5.sp
            )
        }
        Text(
            text = stringResource(R.string.aqi_worst_not_average),
            color = palette.textSecondary,
            fontSize = 11.5.sp,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

/**
 * One pollutant on a line: symbol, name, concentration and level, over a bar placed on its own
 * scale rather than a shared one - 600 µg/m³ of carbon monoxide is low, 44 µg/m³ of fine
 * particles is high. The thresholds themselves are left to "Understanding the index".
 */
@Composable
private fun PollutantRow(pollutant: Pollutant, value: Float, isDominant: Boolean) {
    val palette = LocalWeatherPalette.current
    val level = pollutant.levelOf(value)
    val levelColor = palette.aqiColor(level) ?: palette.textMuted

    Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = pollutant.label,
                color = palette.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(44.dp)
            )
            //  The existing strings read "SO₂ - Sulphur dioxide"; the symbol is already on the left.
            Text(
                text = stringResource(pollutant.descriptionRes).substringAfter(" - "),
                color = palette.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isDominant) {
                Text(
                    text = stringResource(R.string.label_dominant_badge),
                    color = palette.textSecondary,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier
                        .border(1.dp, palette.outlineStrong, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
            Text(text = formatConcentration(value), color = palette.textPrimary, fontSize = 14.sp)
            Text(text = stringResource(R.string.label_level_short, level), color = levelColor, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(palette.outline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pollutant.severityOf(value).coerceAtLeast(0.02f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(levelColor)
            )
        }
    }
}

/** Concentrations as measured, to one decimal while that decimal still says something. */
private fun formatConcentration(value: Float): String =
    if (value < 100f) String.format(Locale.getDefault(), "%.1f", value)
    else String.format(Locale.getDefault(), "%.0f", value)

@Composable
private fun ExplainerEntry(onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = 0.07f))
            .border(1.dp, accent.copy(alpha = 0.34f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.3.dp, accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "i", color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = stringResource(R.string.title_dialog_understand_index),
            color = palette.textPrimary,
            fontSize = 12.5.sp,
            modifier = Modifier.weight(1f)
        )
        Text(text = "›", color = accent, fontSize = 14.sp)
    }
}
