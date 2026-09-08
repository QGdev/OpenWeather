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

package fr.qgdev.openweather.ui.common.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.mandatorySystemGesturesPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.theme.readableOn
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.surfaceColorAtElevation

private val DIALOG_ELEVATION = 10.dp

@Composable
fun FullScreenDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    AppTheme {
        Dialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                decorFitsSystemWindows = true
            ),
            onDismissRequest = onDismissRequest,
        ) {
            Surface(
                Modifier
                    .padding(16.dp)
                    .displayCutoutPadding()
                    .mandatorySystemGesturesPadding(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = DIALOG_ELEVATION,
                tonalElevation = DIALOG_ELEVATION,
                border = BorderStroke(
                    0.1.dp,
                    MaterialTheme.colorScheme.outlineVariant)
                ) {
                Column (
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 16.dp),

                            onClick = onDismissRequest
                        ) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize(),
                                painter = painterResource(id = R.drawable.ic_baseline_close),
                                contentDescription = ""
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Surface (
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        //  The palette's faintest texts are tuned for the screens' black or near-white
                        //  ground; this surface is lighter, tinted by its elevation, and they fell under
                        //  a readable contrast on it. They are adjusted to the surface actually drawn.
                        val surface = MaterialTheme.colorScheme.surfaceColorAtElevation(DIALOG_ELEVATION)
                        CompositionLocalProvider(
                            LocalWeatherPalette provides LocalWeatherPalette.current.readableOn(surface)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xF00,
    widthDp = 400,
    heightDp = 800
)
@Composable
private fun FullScreenDialogPreview() {
    FullScreenDialog(
        title = "Error",
        onDismissRequest = {}
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "An error occurred while fetching data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}