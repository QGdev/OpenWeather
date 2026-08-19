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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ClassicAnimatedGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 0.dp,
    horizontalSpacing: Dp = 0.dp,
    visibleArea: Rect, // Define the area where items are always visible
    showOutsideArea: Boolean, // Single boolean to control the visibility of items outside the visible area
    content: @Composable (index: Int) -> Unit // Modify content to accept an index
) {
    Layout(
        modifier = modifier
            .animateContentSize(),
        content = {
            val displayedItems : HashSet<Int> = HashSet()
            var index = 0

            while(!displayedItems.contains(content.hashCode())) {
                displayedItems.add(content.hashCode())

                content(index)

                index++
            }
        }
    ) { measurables, constraints ->


        val itemWidth =
            ((constraints.maxWidth - (columns - 1) * horizontalSpacing.roundToPx()) / columns).coerceAtLeast(
                constraints.minWidth / columns - 20
            )

        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()
        var maxItemWidth = 0
        var maxItemHeight = 0

        val itemPlaceable = measurables.map { measurable ->
            val intrinsicHeight = measurable.minIntrinsicHeight(itemWidth)
            val adjustedConstraints = constraints.copy(
                maxWidth = itemWidth,
                minWidth = itemWidth,
                maxHeight = intrinsicHeight,
                minHeight = intrinsicHeight
            )
            val placeable = measurable.measure(adjustedConstraints)

            maxItemWidth = maxOf(maxItemWidth, placeable.width)
            maxItemHeight = maxOf(maxItemHeight, placeable.height)

            placeable
        }

        val rows = (itemPlaceable.size + columns - 1) / columns
        val itemHeights = List(rows) { row ->
            (0 until columns).mapNotNull { col ->
                val index = row * columns + col
                if (index < itemPlaceable.size) {
                    itemPlaceable[index].height
                } else {
                    null
                }
            }.maxOrNull() ?: 0
        }

        val compactCols = visibleArea.right - visibleArea.left
        val compactRows = visibleArea.bottom - visibleArea.top

        val compactWidth = itemWidth * compactCols + (compactCols - 1) * horizontalSpacingPx
        val compactHeight = maxItemHeight * compactRows + (compactRows - 1) * verticalSpacingPx

        val totalWidth = maxItemWidth * columns + (columns - 1) * horizontalSpacingPx
        val totalHeight = maxItemHeight * rows + (rows - 1) * verticalSpacingPx

        val width = (if (showOutsideArea) totalWidth else compactWidth).coerceAtLeast(0)
        val height = (if (showOutsideArea) totalHeight else compactHeight).coerceAtLeast(0)

        layout(width, height) {
            var y = 0
            itemPlaceable.forEachIndexed { index, placeable ->
                val row = index / columns
                val col = index % columns
                val x = col * (itemWidth + horizontalSpacingPx)

                val isRowVisible = visibleArea.top <= row && row < visibleArea.bottom
                val isColVisible = visibleArea.left <= col && col < visibleArea.right
                val isItemVisible = isRowVisible && isColVisible

                if (isItemVisible) {
                    placeable.place(x, y)
                } else if (showOutsideArea) {
                    placeable.place(x, y)
                }

                if (col == columns - 1) {
                    y += itemHeights[row] + verticalSpacingPx
                }
            }
        }
    }
}



data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int)

@Preview
@Composable
fun PreviewClassicAnimatedGrid() {
    val showOutsideArea = remember { mutableStateOf(true) }
    val visibleArea = Rect(0, 0, 1, 2) // Define the visible area (top-left 2x2 area)
    //  Add a button to toggle the visibility of items outside the visible area
    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        Button (
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            onClick = { showOutsideArea.value = !showOutsideArea.value },
        ) {
            Text(text = "Toggle visibility of items outside the visible area")
        }
        ClassicAnimatedGrid(
            columns = 3,
            verticalSpacing = 8.dp,
            horizontalSpacing = 8.dp,
            visibleArea = visibleArea,
            showOutsideArea = showOutsideArea.value
        ) {
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
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF00FF7F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF00FFFF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F0000))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F007F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F00FF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F7F00))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F7F7F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7F7FFF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7FFF00))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7FFF7F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFF7FFFFF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF0000))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF007F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF00FF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF7F00))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF7F7F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFF7FFF))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFFFF00))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFFFF7F))
            )
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(10.dp)
                    .background(Color(0xFFFFFFFF))
            )
        }
    }
}