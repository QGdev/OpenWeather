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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R

@Composable
fun FoldableContainer(
    modifier: Modifier = Modifier,
    title : String,
    headerColor: Color,
    isUnFolded: MutableState<Boolean>,
    onFold: () -> Unit = {},
    onUnfold: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.colorSecondaryText),
                shape = MaterialTheme.shapes.small
            )
            .animateContentSize()
            .clickable {
                isUnFolded.value = !isUnFolded.value
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val buttonRotation by animateFloatAsState(
            animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            targetValue = if (isUnFolded.value) 0f else 180f,
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = headerColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_expend_menu_icon),
                contentDescription = "",
                tint = headerColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(buttonRotation)
            )
        }

        if (isUnFolded.value) {
            content()
        }

    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun FoldableContainerPreview() {
    FoldableContainer(
        title = "Title",
        headerColor = Color.Black,
        isUnFolded = remember { mutableStateOf(false) }
    ) {
        Text(
            modifier =
                Modifier.padding(16.dp),
            text = "Content"
        )
    }
}