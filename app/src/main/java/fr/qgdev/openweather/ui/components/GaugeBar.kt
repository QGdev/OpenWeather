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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GaugeBarView(
    modifier: Modifier = Modifier,
    value: Float = 50f,
    labelText: String = "O²",
    textColor: Color = Color.Black,
    sectionsBoundaries: List<Float> = listOf(0f, 25f, 75f, 100f),
    sectionsColors: List<Color> = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green
    )
) {
    //  Check if the sectionsBoundaries and sectionsColors are valid
    //  ... on their size, both must have the same size
    //  The last section is from the last boundary to infinity
    require(sectionsBoundaries.size == sectionsColors.size) {
        "sectionsBoundaries.size (${sectionsBoundaries.size}) must be equal to sectionsColors.size (${sectionsColors.size})"
    }

    //  ... sectionsBoundaries must be sorted
    require(sectionsBoundaries == sectionsBoundaries.sorted()) {
        "sectionsBoundaries must be sorted"
    }


    val cursorSize = 12.dp
    val barThickness = 8.dp

    val halfBarThickness = barThickness / 2
    val halfCursorSize = cursorSize / 2

    val minHeight = listOf(cursorSize.value, barThickness.value).maxOf { it }.dp

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .widthIn(min = 70.dp, max = 100.dp)
                .padding(end = 16.dp),
            text = labelText,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
        )

        Canvas(modifier = modifier
            .height(minHeight)
            .weight(1f)
            .fillMaxWidth(),
        ) {
            val width = size.width
            val height = size.height

            val middleY = height / 2

            //  Convert DP values to PX in order to avoid multiple redundant conversions
            val cursorSizePx = cursorSize.toPx()
            val halfCursorSizePx = halfCursorSize.toPx()
            val barThicknessPx = barThickness.toPx()
            val halfBarThicknessPx = halfBarThickness.toPx()
            val minHeightPx = minHeight.toPx()

            //  Compute bar colors repartition
            val startBarOrigin = 0F
            val endBarOrigin = width
            val startBarCircleX = startBarOrigin + halfBarThicknessPx
            val endBarCircleX = endBarOrigin - halfBarThicknessPx
            val topBarOrigin = middleY - halfBarThicknessPx

            val barWidth = endBarOrigin - startBarOrigin
            val sectionWidth = barWidth / sectionsColors.size

            //  Provided boundaries might be nonlinear, so we need to compute the slopes
            //  of each section in order to compute the cursor position
            val sectionSlopes = sectionsBoundaries.zipWithNext { a, b -> (b - a) / sectionWidth }

            //  Compute the cursor position
            val cursorX = computeBarCursorX(startBarOrigin, endBarOrigin, sectionWidth, value, sectionsBoundaries, sectionSlopes)

            //
            //  Drawing part
            //

            //  Draw each ends of the bar
            drawCircle(
                color = sectionsColors.first(),
                center = Offset(startBarCircleX, middleY),
                radius = halfBarThicknessPx
            )
            drawCircle(
                color = sectionsColors.last(),
                center = Offset(endBarCircleX, middleY),
                radius = halfBarThicknessPx
            )

            //  Draw the first and last sections
            drawRect(
                color = sectionsColors.first(),
                topLeft = Offset(startBarCircleX, topBarOrigin),
                size = Size(sectionWidth - halfBarThicknessPx, barThicknessPx)
            )
            drawRect(
                color = sectionsColors.last(),
                topLeft = Offset(endBarCircleX - sectionWidth + halfBarThicknessPx, topBarOrigin),
                size = Size(sectionWidth - halfBarThicknessPx, barThicknessPx)
            )

            //  Draw all the middle sections
            for (i in 1 until sectionsColors.size - 1) {
                val sectionStart = startBarOrigin + sectionWidth * i
                drawRect(
                    color = sectionsColors[i],
                    topLeft = Offset(sectionStart, topBarOrigin),
                    size = Size(sectionWidth, barThicknessPx)
                )
            }
            //  Draw the cursor
            drawCircle(
                color = Color.White,
                center = Offset(cursorX, middleY),
                radius = halfCursorSizePx
            )
        }

        Text(
            modifier = Modifier
                .widthIn(min = 95.dp)
                .padding(start = 16.dp),
            text = value.toString(),
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
        )
    }
}


private fun computeBarCursorX(
    start: Float,
    end: Float,
    sectionWidth: Float,
    value: Float,
    sectionsBoundaries: List<Float>,
    sectionsSlopes: List<Float>
): Float {
    var cursorX: Float

    var finalLowerBound = 0

    while (finalLowerBound < sectionsBoundaries.size - 1 && sectionsBoundaries[finalLowerBound + 1] <= value) {
        finalLowerBound++
    }

    val upperBound = finalLowerBound + 1

    val slope: Float = if (upperBound < sectionsBoundaries.size) {
        sectionsBoundaries[upperBound] - sectionsBoundaries[finalLowerBound]
    } else {
        sectionsBoundaries.last() / sectionsBoundaries.size
    }

    cursorX = start
    cursorX += sectionWidth * finalLowerBound
    cursorX += ((value - sectionsBoundaries[finalLowerBound]) * sectionWidth / slope).toInt()

    if (cursorX < start) {
        cursorX = start
    } else if (cursorX > end) {
        cursorX = end
    }

    return cursorX
}

private fun findNearestInOrderedList(value: Float, list: List<Float>): Int {
    //  Dichotomic search
    var left = 0
    var right = list.size - 1

    while (left < right) {
        val middle = (left + right) / 2
        if (list[middle] < value) {
            left = middle + 1
        } else {
            right = middle
        }
    }

    return left
}

@Preview
@Composable
fun GaugeBarPreview() {
    GaugeBarView(
        value = 50f,
        labelText = "O²",
        sectionsBoundaries = listOf(-100f, -50f, 0f, 50f, 100f),
        sectionsColors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Yellow,
            Color.Red
        )
    )
}