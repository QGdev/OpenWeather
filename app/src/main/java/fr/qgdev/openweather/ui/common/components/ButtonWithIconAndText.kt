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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    iconResId: Int,
    text: String,
    description: String
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.Gray, // Replace with actual color resource
                shape = MaterialTheme.shapes.small,
            )
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
    descriptionResId: Int
) {
    ButtonWithIconAndText(
        modifier = modifier,
        iconResId = iconResId,
        text = stringResource(id = textResId),
        description = stringResource(id = descriptionResId)
    )
}

@Composable
@Preview
fun ButtonWithIconAndTextPreview() {
    ButtonWithIconAndText(
        modifier = Modifier,
        iconResId = R.drawable.danger,
        text = "Alert",
        description = "Weather alert"
    )
}