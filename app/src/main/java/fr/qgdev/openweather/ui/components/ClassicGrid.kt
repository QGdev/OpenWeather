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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ClassicGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 0.dp,
    horizontalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {

    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->

        val itemWidth = ((constraints.maxWidth - (columns - 1) * horizontalSpacing.roundToPx()) / columns).coerceAtLeast(constraints.minWidth / columns)
        val itemConstraints = constraints.copy(maxWidth = itemWidth, minWidth = itemWidth)

        val itemPlaceables = measurables.map { measurable ->
            val intrinsicHeight = measurable.minIntrinsicHeight(itemWidth)
            val adjustedConstraints = constraints.copy(maxWidth = itemWidth, minWidth = itemWidth, maxHeight = intrinsicHeight, minHeight = intrinsicHeight)
            measurable.measure(adjustedConstraints)
        }

        val rows = (itemPlaceables.size + columns - 1) / columns
        val itemHeights = List(rows) { row ->
            (0 until columns).mapNotNull { col ->
                val index = row * columns + col
                if (index < itemPlaceables.size) {
                    itemPlaceables[index].height
                } else {
                    null
                }
            }.maxOrNull() ?: 0
        }

        val height = itemHeights.sum() + (rows - 1) * verticalSpacing.roundToPx()
        layout(constraints.maxWidth, height) {
            var y = 0
            itemPlaceables.forEachIndexed { index, placeable ->
                val row = index / columns
                val col = index % columns
                val x = col * (itemWidth + horizontalSpacing.roundToPx())
                placeable.place(x, y)
                if (col == columns - 1) {
                    y += itemHeights[row] + verticalSpacing.roundToPx()
                }
            }
        }
    }
}

@Preview
@Composable
fun ClassicGridPreview() {

    ClassicGrid(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        columns = 5,
        verticalSpacing = 8.dp,
        horizontalSpacing = 8.dp
    ) {
    // 16 items with a different color
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF000000))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF00007F))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF0000FF))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF007F00))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF007F7F))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF007FFF))
        )
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(10.dp)
                .background(Color(0xFF00FF00))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF00FF7F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF00FFFF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F0000))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F007F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F00FF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F7F00))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F7F7F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7F7FFF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7FFF00))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7FFF7F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFF7FFFFF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF0000))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF007F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF00FF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF7F00))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF7F7F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFF7FFF))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFFFF00))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFFFF7F))
        )
        Box(modifier = Modifier
            .height(10.dp)
            .width(10.dp)
            .background(Color(0xFFFFFFFF))
        )
    }
}