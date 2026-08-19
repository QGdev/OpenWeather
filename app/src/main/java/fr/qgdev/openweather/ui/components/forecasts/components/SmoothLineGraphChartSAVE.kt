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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp


@Composable
fun SmoothLineGraphChartSAVE(
    modifier: Modifier = Modifier,
    values: List<Float>,
    max: Float = values.maxOrNull() ?: 0f,
    min: Float = values.minOrNull() ?: 0f,
    color: Color,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    style: DrawStyle = Stroke(5f), debug: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Internal padding to avoid stroke clipping at top/bottom edges
        // (matches Java ForecastView: top = 4F, bottom = height - 4F)
        val padding = 4f
        val drawTop = padding
        val drawBottom = height - padding
        val drawHeight = drawBottom - drawTop

        val step = width / (values.size - 1)
        val path = Path()

        fun yForValue(value: Float): Float {
            return if (max != min) {
                drawBottom - (value - min) / (max - min) * drawHeight
            } else {
                drawBottom - drawHeight / 2f
            }
        }

        path.moveTo(0f, yForValue(values[0]))

        if (values.size > 1) {
            for (i in 0 until values.size - 1) {
                val x1 = i * step
                val y1 = yForValue(values[i])

                val x2 = (i + 1) * step
                val y2 = yForValue(values[i + 1])

                val controlX1 = x1 * 0.7f + x2 * 0.3f
                val controlX2 = x1 * 0.3f + x2 * 0.7f

                val controlX = (controlX1 + controlX2) / 2
                val controlY = (y1 + y2) / 2

                path.quadraticBezierTo(
                    controlX1,
                    y1,
                    controlX,
                    controlY
                )

                path.quadraticBezierTo(
                    controlX2,
                    y2,
                    x2,
                    y2
                )


                if (debug) {
                    //  Draw control line
                    drawLine(
                        start = Offset(x1, y1),
                        end = Offset(controlX1, y1),
                        color = Color.Red
                    )

                    //  Draw control line
                    drawLine(
                        start = Offset(x2, y2),
                        end = Offset(controlX2, y2),
                        color = Color.Red
                    )

                    //  Draw points
                    drawCircle(
                        color = Color.Red,
                        center = Offset(x1, y1),
                        radius = 20f
                    )

                    //  Draw control points
                    drawCircle(
                        color = Color.Green,
                        center = Offset(controlX1, y1),
                        radius = 10f
                    )

                    drawCircle(
                        color = Color.Green,
                        center = Offset(controlX2, y2),
                        radius = 10f
                    )
                }



            }
        }

        drawPath(
            path = path,
            color = color,
            style = style,

        )
    }
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview() {
    var values = emptyList<Float>()
    for (i in 0..10) {
        values += (Math.sin(i.toDouble() / 1.5)).toFloat()
    }

    SmoothLineGraphChartSAVE(
        //  Sinusoidal values
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.cornerPathEffect(1000f)
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview2() {
    var values = listOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview3() {
    var values = listOf(10f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview4() {
    var values = listOf(5f, 4f, 3f, 2f, 1f, 2f, 3f, 4f, 5f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview5() {
    var values = listOf(1f, 2f, 3f, 4f, 5f, 4f, 3f, 2f, 1f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview6() {
    var values = listOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f, 5f, 5f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview7() {
    var values = listOf(5f, 5f, 4f, 4f, 3f, 3f, 2f, 2f, 1f, 1f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview8() {
    var values = listOf(1f, 0f, 2f, 1f, 3f, 2f, 4f, 3f, 5f, 4f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview9() {
    var values = listOf(4f, 5f, 3f, 4f, 2f, 3f, 1f, 2f, 0f, 1f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview
@Composable
fun SmoothLineGraphChartSAVEPreview10() {
    var values = listOf(1f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 9f)

    SmoothLineGraphChartSAVE(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}