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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import fr.qgdev.openweather.ui.place.detail.Pollutant
import fr.qgdev.openweather.ui.place.detail.airQualityLabelRes
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette

/**
 * What the air quality index means, and what each pollutant is.
 *
 * The least obvious thing about the index is not the chemistry but the arithmetic: it is the worst
 * of the six measurements, not their average. That goes first, because six modest-looking numbers
 * under a bad index otherwise look like a mistake.
 *
 * Each pollutant carries its four thresholds beside its name. They are what make the bars on the
 * detail screen readable, and they explain why carbon monoxide never seems to move - its scale is a
 * hundred times the others'.
 *
 * Sections are laid out open rather than as an accordion: six short paragraphs fit in one scroll,
 * and folding them would cost six taps to read what is already brief.
 */
@Composable
fun AirQualityInfoDialog(
    onDismissRequest: () -> Unit = {}
) {
    val palette = LocalWeatherPalette.current

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_understand_index),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.aqi_explainer_heading),
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.aqi_explainer_body),
                color = palette.textSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(top = 6.dp)
            )

            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                (1..5).forEach { level -> IndexLevelRow(level) }
            }

            Pollutant.entries.forEach { pollutant ->
                PollutantSection(pollutant)
            }

            Text(
                text = stringResource(R.string.aqi_scales_note),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun IndexLevelRow(level: Int) {
    val palette = LocalWeatherPalette.current
    val color = palette.aqiColor(level) ?: palette.textMuted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = level.toString(),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = airQualityLabelRes(level)?.let { stringResource(it) }.orEmpty(),
            color = palette.textPrimary,
            fontSize = 12.5.sp
        )
    }
}

@Composable
private fun PollutantSection(pollutant: Pollutant) {
    val palette = LocalWeatherPalette.current

    Column(modifier = Modifier.padding(top = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = pollutant.label,
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                //  The four bounds, in order: they are the scale the composition bars are drawn on.
                text = pollutant.boundaries.joinToString(" · ") { boundary ->
                    boundary.toInt().toString()
                },
                color = palette.textQuiet,
                fontSize = 10.5.sp
            )
        }
        Text(
            text = stringResource(pollutant.descriptionRes),
            color = palette.textPrimary,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = stringResource(pollutantDescriptionBodyRes(pollutant)),
            color = palette.textSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** The existing, already translated explanation for each pollutant. */
private fun pollutantDescriptionBodyRes(pollutant: Pollutant): Int = when (pollutant) {
    Pollutant.SO2 -> R.string.content_air_quality_so2_information
    Pollutant.NO2 -> R.string.content_air_quality_no2_information
    Pollutant.PM10 -> R.string.content_air_quality_pm10_information
    Pollutant.PM25 -> R.string.content_air_quality_pm25_information
    Pollutant.O3 -> R.string.content_air_quality_o3_information
    Pollutant.CO -> R.string.content_air_quality_co_information
}
