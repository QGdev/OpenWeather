/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.settings.OneCallVersion
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette

/**
 * The One Call version as a switch, on for 3.0, with a "?" that explains the choice. Shared by the
 * settings and the onboarding's key step, which is where a key refused by one version is fixed.
 */
@Composable
fun OneCallVersionSwitch(
    modifier: Modifier = Modifier,
    version: OneCallVersion,
    onVersionChanged: (OneCallVersion) -> Unit
) {
    val palette = LocalWeatherPalette.current
    var helpOpened by remember { mutableStateOf(false) }
    val isV3 = version == OneCallVersion.V3_0
    val toggle = {
        onVersionChanged(if (isV3) OneCallVersion.V2_5 else OneCallVersion.V3_0)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = toggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.title_settings_one_call_version),
                    color = palette.textPrimary,
                    fontSize = 13.5.sp
                )
                HelpButton(onClick = { helpOpened = true })
            }
            Text(
                text = stringResource(
                    if (isV3) R.string.summary_settings_one_call_v3 else R.string.summary_settings_one_call_v2_5
                ),
                color = palette.textQuiet,
                fontSize = 11.sp
            )
        }
        Switch(
            modifier = Modifier.padding(start = 14.dp),
            checked = isV3,
            onCheckedChange = { toggle() }
        )
    }

    if (helpOpened) {
        AlertDialog(
            onDismissRequest = { helpOpened = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = stringResource(R.string.title_one_call_version_help),
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.content_one_call_version_help),
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { helpOpened = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}

/** A small circled question mark, with a touch area larger than what it draws. */
@Composable
private fun HelpButton(onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current
    val description = stringResource(R.string.description_one_call_version_help)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(1.dp, palette.outlineStrong, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            //  Drawn rather than typed: a "?" in Text is centred on its line's ascent and descent,
            //  not on the glyph, and sits visibly off the middle of the circle.
            val color = palette.textMuted
            Canvas(modifier = Modifier.size(14.dp)) {
                val unit = size.minDimension / 24f
                val stroke = Stroke(width = 1.9f * unit, cap = StrokeCap.Round)
                //  The hook, from nine o'clock over the top down to six, then the stem and the dot.
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(9.2f * unit, 6.4f * unit),
                    size = Size(5.6f * unit, 5.6f * unit),
                    style = stroke
                )
                drawLine(color, Offset(12f * unit, 12f * unit), Offset(12f * unit, 13.8f * unit), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 1.2f * unit, center = Offset(12f * unit, 17.4f * unit))
            }
        }
    }
}
