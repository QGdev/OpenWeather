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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SmoothLineGraphChart(
    modifier: Modifier = Modifier,
    values: FloatArray,
    max: Float = values.maxOrNull() ?: 0f,
    min: Float = values.minOrNull() ?: 0f,
    color: Color,
    style: DrawStyle = Stroke(5f),
    outerValuesOnlyForCompute: Boolean = true,
    debug: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()
        .clipToBounds()) {
        val width = size.width
        val height = size.height

        // Internal padding to avoid stroke clipping at top/bottom edges
        val padding = 4f
        val drawTop = padding
        val drawBottom = height - padding
        val drawHeight = drawBottom - drawTop

        val step = width / (values.size - 2)
        val path = Path()

        var posX = if (outerValuesOnlyForCompute) -(step/2f) else 0f


        fun yForValue(value: Float): Float {
            return if (max != min) {
                drawBottom - (value - min) / (max - min) * drawHeight
            } else {
                drawBottom - drawHeight / 2f
            }
        }

        path.moveTo(posX, yForValue(values[0]))

        if (values.size > 1) {
            for (i in 0 until values.size - 1) {
                val x1 = posX
                val y1 = yForValue(values[i])

                val x2 = posX + step
                val y2 = yForValue(values[i + 1])

                val controlX1 = x1 * 0.7f + x2 * 0.3f
                val controlX2 = x1 * 0.3f + x2 * 0.7f

                val controlX = (controlX1 + controlX2) / 2
                val controlY = (y1 + y2) / 2

                posX = x2

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
fun SmoothLineGraphChartPreview() {
    var values = FloatArray(11)
    for (i in 0..10) {
        values[i] = (Math.sin(i.toDouble() / 1.5f)).toFloat()
    }

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview2() {
    var values = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview3() {
    var values = floatArrayOf(10f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview4() {
    var values = floatArrayOf(5f, 4f, 3f, 2f, 1f, 2f, 3f, 4f, 5f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview5() {
    var values = floatArrayOf(1f, 2f, 3f, 4f, 5f, 4f, 3f, 2f, 1f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview6() {
    var values = floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f, 5f, 5f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview7() {
    var values = floatArrayOf(5f, 5f, 4f, 4f, 3f, 3f, 2f, 2f, 1f, 1f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview8() {
    var values = floatArrayOf(1f, 0f, 2f, 1f, 3f, 2f, 4f, 3f, 5f, 4f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview9() {
    var values = floatArrayOf(4f, 5f, 3f, 4f, 2f, 3f, 1f, 2f, 0f, 1f)

    SmoothLineGraphChart(
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
fun SmoothLineGraphChartPreview10() {
    var values = floatArrayOf(1f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f, 9f)

    SmoothLineGraphChart(
        values = values,
        color = Color.Blue,
        style = Stroke(
            width = 10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Preview(device = "spec:width=600dp,height=400dp,dpi=440")
@Composable
fun SmoothLineGraphChartPreviewCouples1() {
    // sin
    val values = listOf(0f, 0.84f, 0.91f, 0.14f, -0.76f, -0.96f, -0.28f, 0.66f, 0.99f, 0.41f)
    val valuesA = values.subList(0, values.size / 2 + 1).toFloatArray()
    val valuesB = values.subList(values.size / 2 -1, values.size).toFloatArray()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxHeight()
                .width(300.dp)
                .align(Alignment.CenterStart),
            values = valuesA,
            max = 1f,
            min = -1f,
            color = Color.Blue,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(1000f)
            ),
            debug = true
        )
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxHeight()
                .width(300.dp)
                .align(Alignment.TopEnd),
            values = valuesB,
            max = 1f,
            min = -1f,
            color = Color.Red,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(1000f)
            ),
            debug = true
        )
    }
}

@Preview(device = "spec:width=900dp,height=400dp,dpi=440")
@Composable
fun SmoothLineGraphChartPreviewCouples2() {
    // sin
    val values = listOf(0f, 0.84f, 0.91f, 0.14f, -0.76f, -0.96f, -0.28f, 0.66f, 0.99f, 0.41f, 0.84f, 0.91f, 0.14f, -0.76f, -0.96f, -0.28f, 0.66f, 0.99f, 0.41f)
    val midAIdx = values.size / 3
    val midBIdx = values.size * 2 / 3
    val valuesA = values.subList(0, midAIdx + 1).toFloatArray()
    val valuesB = values.subList(midAIdx -1, midBIdx + 1).toFloatArray()
    val valuesC = values.subList(midBIdx -1, values.size).toFloatArray()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxHeight()
                .width(300.dp)
                .align(Alignment.CenterStart),
            values = valuesA,
            max = 1f,
            min = -1f,
            color = Color.Blue,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(1000f)
            ),
            debug = true
        )
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxHeight()
                .width(300.dp)
                .align(Alignment.Center),
            values = valuesB,
            max = 1f,
            min = -1f,
            color = Color.Red,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(1000f)
            ),
            debug = true
        )
        SmoothLineGraphChart(
            modifier = Modifier.fillMaxHeight()
                .width(300.dp)
                .align(Alignment.CenterEnd),
            values = valuesC,
            max = 1f,
            min = -1f,
            color = Color.Green,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(1000f)
            ),
            debug = true
        )
    }
}
