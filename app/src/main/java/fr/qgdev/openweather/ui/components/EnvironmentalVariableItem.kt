/*
 *  Copyright (c) 2019 - 2024
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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
fun EnvironmentalVariableItem(
    modifier: Modifier = Modifier,
    variable: String,
    @DrawableRes drawableId: Int,
    contentDescription: String
) {
    val colorSecondaryText = colorResource(id = R.color.colorSecondaryText)
    val colorIcons = colorResource(id = R.color.colorIcons)

    Row(
        modifier = modifier
            .wrapContentHeight()
            .width(100.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            tint = colorIcons,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(18.dp)
        )
        Text(
            text = variable,
            color = colorSecondaryText,
            fontSize = 16.sp,
        )
    }
}


@Preview
@Composable
private fun EnvironmentalVariableItemPreview() {
    EnvironmentalVariableItem(
        variable = "10",
        drawableId = R.drawable.ic_about_us,
        contentDescription = "Temperature icon"
    )
}
