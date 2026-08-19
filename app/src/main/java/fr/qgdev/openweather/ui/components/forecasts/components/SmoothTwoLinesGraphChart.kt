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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


@Composable
fun SmoothTwoLinesGraphChart(
    modifier: Modifier = Modifier,
    valuesA: FloatArray,
    valuesB: FloatArray,
    max: Float = 0f,
    min: Float = 0f,
    colorA: Color,
    colorB: Color,
    styleA: DrawStyle = Stroke(5f),
    styleB: DrawStyle = Stroke(
        width = 5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
    ),
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    outerValuesOnlyForCompute: Boolean = true,
    debug: Boolean = false
) {
    Box(
        modifier = modifier,
    ) {
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxSize(),
            values = valuesA,
            max = max,
            min = min,
            color = colorA,
            style = styleA,
            outerValuesOnlyForCompute = outerValuesOnlyForCompute,
            debug = debug
        )
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxSize(),
            values = valuesB,
            max = max,
            min = min,
            color = colorB,
            style = styleB,
            outerValuesOnlyForCompute = outerValuesOnlyForCompute,
            debug = debug
        )
    }
}

@Preview
@Composable
private fun SmoothTwoLineGraphChartPreview() {
    var valuesA = emptyList<Float>()
    var valuesB = emptyList<Float>()
    for (i in 0..5) {
        valuesA += sin(i.toDouble()).toFloat()
        valuesB += cos(i.toDouble()).toFloat()
    }

    val max = max(valuesA.maxOrNull() ?: 0f, valuesB.maxOrNull() ?: 0f)
    val min = min(valuesA.minOrNull() ?: 0f, valuesB.minOrNull() ?: 0f)

    SmoothTwoLinesGraphChart(
        //  Sinusoidal values
        valuesA = valuesA.toFloatArray(),
        valuesB = valuesB.toFloatArray(),
        max = max + 10,
        min = min - 10,
        colorA = Color.Blue,
        colorB = Color.Red,
        styleA = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(10f),
        ),
        styleB = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(10f),
        ),
        debug = false
    )
}