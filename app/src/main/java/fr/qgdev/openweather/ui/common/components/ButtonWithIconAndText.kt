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

package fr.qgdev.openweather.ui.common.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R

/**
 * A bordered row acting as a button.
 *
 * [onClick] is required rather than defaulted: this composable is named like a button and reads like
 * one on screen, and when it had no click parameter at all the weather alerts button in
 * `PlaceCardView` shipped looking tappable while doing nothing.
 */
@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    iconResId: Int,
    text: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = Color.Gray, // Replace with actual color resource
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = description,
            tint = colorResource(id = R.color.colorFirstText),
            modifier = Modifier
                .size(30.dp)
                .padding(end = 10.dp)
        )
        Text(
            text = text,
            color = colorResource(id = R.color.colorFirstText),
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    iconResId: Int,
    textResId: Int,
    descriptionResId: Int,
    onClick: () -> Unit
) {
    ButtonWithIconAndText(
        modifier = modifier,
        iconResId = iconResId,
        text = stringResource(id = textResId),
        description = stringResource(id = descriptionResId),
        onClick = onClick
    )
}

@Composable
@Preview
fun ButtonWithIconAndTextPreview() {
    ButtonWithIconAndText(
        modifier = Modifier,
        iconResId = R.drawable.danger,
        text = "Alert",
        description = "Weather alert",
        onClick = {}
    )
}