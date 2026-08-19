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

package fr.qgdev.openweather.ui.components.forecasts.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.ui.components.ClassicGrid
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val THICKNESS_RATIO = 0.02F
private const val SIZE_RATIO = 0.75F


@Composable
fun WindDirectionIndicator(
    modifier: Modifier = Modifier,
    windDirection: Int,
    color: Color = Color.White
) {
    Canvas(
        modifier = modifier
    ) {

        val sideLength = min(size.height, size.width)
        val halfSideLength = size.minDimension / 2

        val topPointAngle = (windDirection - 90) % 360
        val endBottomPointAngle = (topPointAngle + 120) % 360
        val startBottomPointAngle = (topPointAngle + 240) % 360

        val radiusSize = halfSideLength * SIZE_RATIO
        val drawThickness = sideLength * THICKNESS_RATIO

        val topPointX = (center.x + radiusSize * cos(Math.toRadians(topPointAngle.toDouble()))).toFloat()
        val topPointY = (center.y + radiusSize * sin(Math.toRadians(topPointAngle.toDouble()))).toFloat()

        val endBottomPointX = (center.x + radiusSize * cos(Math.toRadians(endBottomPointAngle.toDouble()))).toFloat()
        val endBottomPointY = (center.y + radiusSize * sin(Math.toRadians(endBottomPointAngle.toDouble()))).toFloat()

        val startBottomPointX = (center.x + radiusSize * cos(Math.toRadians(startBottomPointAngle.toDouble()))).toFloat()
        val startBottomPointY = (center.y + radiusSize * sin(Math.toRadians(startBottomPointAngle.toDouble()))).toFloat()

        val middlePoint = center
        val topPointOffset = Offset(topPointX, topPointY)
        val endBottomPointOffset = Offset(endBottomPointX, endBottomPointY)
        val startBottomPointOffset = Offset(startBottomPointX, startBottomPointY)

        drawLine(
            color = color,
            start = topPointOffset,
            end = middlePoint,
            strokeWidth = drawThickness,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = endBottomPointOffset,
            end = middlePoint,
            strokeWidth = drawThickness,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = startBottomPointOffset,
            end = middlePoint,
            strokeWidth = drawThickness,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = endBottomPointOffset,
            end = topPointOffset,
            strokeWidth = drawThickness,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = startBottomPointOffset,
            end = topPointOffset,
            strokeWidth = drawThickness,
            cap = StrokeCap.Round
        )
    }
}

@Preview
@Composable
private fun WindDirectionIndicatorPreview() {
    ClassicGrid(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        columns = 20
    ) {
        for (i in 0 until 360) {
            WindDirectionIndicator(
                modifier = Modifier
                    .height(20.dp)
                    .width(50.dp),
                windDirection = i*10
            )
        }

    }
}

@Preview
@Composable
private fun WindDirectionIndicatorPreview1() {
    WindDirectionIndicator(
        modifier = Modifier
            .height(50.dp)
            .width(50.dp),
        windDirection = 0
    )
}