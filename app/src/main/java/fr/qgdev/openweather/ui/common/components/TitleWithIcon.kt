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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R

@Composable
fun TitleWithIcon(
    modifier: Modifier = Modifier,
    iconResId: Int,
    text: String,
    description: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = description,
            tint = colorResource(id = R.color.colorFirstText),
            modifier = Modifier
                .size(width = 25.dp, height = 25.dp)
        )
        Text(
            text = text,
            color = colorResource(id = R.color.colorFirstText),
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun TitleWithIcon(
    modifier: Modifier = Modifier,
    iconResId: Int,
    textResId: Int,
    descriptionResId: Int
) {
    TitleWithIcon(
        modifier = modifier,
        iconResId = iconResId,
        text = stringResource(id = textResId),
        description = stringResource(id = descriptionResId)
    )
}

@Composable
@Preview
fun TitleWithIconPreview() {
    TitleWithIcon(
        iconResId = R.drawable.umbrella_material,
        text = "Title",
        description = "Description"
    )
}