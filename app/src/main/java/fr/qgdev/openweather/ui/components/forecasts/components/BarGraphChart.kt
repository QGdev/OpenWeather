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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlin.math.PI

private const val BAR_THICKNESS_RATIO = 0.4f

@Composable
fun BarGraphChart(
    modifier: Modifier = Modifier,
    values: FloatArray,
    max: Float = values.maxOrNull() ?: 0f,
    min: Float = values.minOrNull() ?: 0f,
    color: Color,
    style: DrawStyle = Fill
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Internal padding to avoid clipping at edges (matches SmoothLineGraphChart)
        val padding = 4f
        val drawTop = padding
        val drawBottom = height - padding
        val drawHeight = drawBottom - drawTop

        val step = width / values.size
        val barWidth = step * BAR_THICKNESS_RATIO
        val barMargin = (step - barWidth) / 2

        for (i in values.indices) {
            val x = i * step + barMargin
            val barHeight = if (max != min) {
                (values[i] - min) / (max - min) * drawHeight
            } else {
                0f
            }
            val y = drawBottom - barHeight

            drawRect(
                color = color,
                style = style,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Preview
@Composable
private fun BarGraphChartPreview() {
    var values = FloatArray(10)
    for (i in 0..100) {
        values[i] = (Math.sin(i.toDouble() / (PI*5)).toFloat())
    }

    BarGraphChart(
        //  Sinusoidal values
        values = values,
        color = Color.Blue,
    )
}

@Preview
@Composable
private fun BarGraphChartPreview2() {
    var values = floatArrayOf(0f, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f)

    BarGraphChart(
        values = values,
        color = Color.Blue
    )
}

@Preview
@Composable
private fun BarGraphChartPreview3() {
    var values = floatArrayOf(90f)

    BarGraphChart(
        values = values,
        color = Color.Blue,
        min = 0f,
        max = 100f
    )
}