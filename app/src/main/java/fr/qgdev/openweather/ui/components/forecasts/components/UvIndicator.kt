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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.R
import kotlin.math.cos
import kotlin.math.sin

private const val NUMBER_OF_RAYS = 16
private const val THICKNESS_RATIO = 0.02F
private const val FONT_SIZE_RATIO = 0.2F

/**
 *  UV index indicator
 *      Used to display the UV index with a sun icon and rays as an Android Compose component
 *      The UV index is displayed in the middle of the sun icon
 *      The rays are displayed around the sun icon with a length proportional to the UV index
 *
 *  @param modifier: Modifier of the component
 *  @param textMeasurer: TextMeasurer used to measure the text size (optional)
 *  @param uvIndex: UV index to display (ideally between 0 and 15)
 */
@Composable
fun UvIndicator(
    modifier: Modifier = Modifier,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    uvIndex: Int
) {
    val color = colorResource(when {
        uvIndex < 3 -> R.color.colorUvLow
        uvIndex < 6 -> R.color.colorUvModerate
        uvIndex < 8 -> R.color.colorUvHigh
        uvIndex < 11 -> R.color.colorUvVeryHigh
        else -> R.color.colorUvExtreme
    })

    Canvas(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // Get padding

        val halfSideLength = size.minDimension / 2
        val circleRadius = halfSideLength / 2 - 3

        val drawThickness = size.minDimension * THICKNESS_RATIO

        val startRadius = circleRadius + drawThickness * 3
        //  Stop radius is the maximum radius relative to uvIndex
        val stopRadius = when {
            uvIndex < 11 -> startRadius + uvIndex * (halfSideLength - 4F - startRadius) / 11F
            else -> startRadius + (halfSideLength - 4F - startRadius)
        }

        //  Draw sun rays
        if (uvIndex != 0) {
            val deltaAngle = 2 * Math.PI / NUMBER_OF_RAYS
            for (ray in 0 until NUMBER_OF_RAYS) {
                val angle = ray * deltaAngle
                val cosAngle = cos(angle).toFloat()
                val sinAngle = sin(angle).toFloat()

                drawLine(
                    start = Offset(
                        center.x + cosAngle * startRadius,
                        center.y + sinAngle * startRadius
                    ),
                    end = Offset(center.x + cosAngle * stopRadius, center.y + sinAngle * stopRadius),
                    color = color,
                    strokeWidth = drawThickness,
                    cap = StrokeCap.Round
                )
            }
        }

        //  Draw the uv index
        val fontSize = (size.minDimension * FONT_SIZE_RATIO).toSp()
        val text = uvIndex.toString()
        val textLayoutResult = textMeasurer.measure(
            text = text,
            style = TextStyle(
                fontSize = fontSize
            )
        )
        val textSize = textLayoutResult.size

        drawCircle(
            color = color,
            center = center,
            radius = circleRadius,
            style = Stroke(drawThickness)
        )

        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset(
                x = center.x - textSize.width / 2,
                y = center.y - textSize.height / 2
            ),
            style = TextStyle(
                color = color,
                fontSize = fontSize
            )
        )



    }
}

@Composable
private fun PreviewGrid(
    modifier: Modifier,
) {
    Column {
        Row {
            UvIndicator(
                modifier = modifier,
                uvIndex = 0
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 1
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 2
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 3
            )
        }

        Row {
            UvIndicator(
                modifier = modifier,
                uvIndex = 4
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 5
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 6
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 7
            )
        }
        Row {
            UvIndicator(
                modifier = modifier,
                uvIndex = 8
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 9
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 10
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 11
            )
        }
        Row {
            UvIndicator(
                modifier = modifier,
                uvIndex = 12
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 13
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 14
            )
            UvIndicator(
                modifier = modifier,
                uvIndex = 15
            )
        }
    }
}

@Preview
@Composable
private fun UvIndicatorPreviewBig() {
    val modifier = Modifier
        .width(200.dp)
        .height(200.dp)

    PreviewGrid(modifier)
}

@Preview
@Composable
private fun UvIndicatorPreviewMedium() {
    val modifier = Modifier
        .width(75.dp)
        .height(75.dp)

    PreviewGrid(modifier)
}

@Preview
@Composable
private fun UvIndicatorPreviewSmall() {
    val modifier = Modifier
        .width(25.dp)
        .height(25.dp)

    PreviewGrid(modifier)
}