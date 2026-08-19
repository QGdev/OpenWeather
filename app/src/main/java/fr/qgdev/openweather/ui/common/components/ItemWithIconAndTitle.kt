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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R

@Composable
fun ItemWithIconAndTitle (
    modifier: Modifier = Modifier,
    iconResId: Int,
    title: String,
    itemData: String,
    description: String
) {
    Row(
        modifier = modifier
            .width(200.dp),
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = description,
            tint = colorResource(id = R.color.colorSecondaryText),
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = title,
            color = colorResource(id = R.color.colorSecondaryText),
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 10.dp)
        )
        Text(
            text = itemData,
            color = colorResource(id = R.color.colorFirstText),
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 15.dp)
        )
    }
}

@Preview
@Composable
fun ItemWithIconAndTitlePreview() {
    ItemWithIconAndTitle(
        iconResId = R.drawable.rain_material,
        title = "Temperature",
        itemData = "25°C",
        description = "Temperature icon"
    )
}