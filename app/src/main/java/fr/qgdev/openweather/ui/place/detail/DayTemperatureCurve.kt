/*
 *  Copyright (c) 2019 - 2026
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

package fr.qgdev.openweather.ui.place.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A day's temperature through its four periods, actual against feels-like.
 *
 * The daily forecast carries eight temperatures for a single day - morning, midday, evening and
 * night, each with a feels-like - and as eight numbers in a column they say very little. Drawn,
 * they say the one thing worth knowing: where the day peaks, and how far the wind and humidity pull
 * the felt temperature away from the measured one.
 *
 * Four points is too few to interpolate honestly, so the line is straight between them rather than
 * smoothed: a curve would invent a mid-afternoon maximum the data never claimed. The feels-like
 * line is dashed, so the two are told apart without relying on colour alone.
 *
 * Values arrive already converted to the user's unit; [format] only renders them.
 */
@Composable
internal fun DayTemperatureCurve(
    actual: List<Float>,
    feelsLike: List<Float>,
    periodLabels: List<String>,
    format: (Float) -> String,
    accent: Color,
    valueColor: Color,
    secondaryColor: Color,
    pointFill: Color,
    modifier: Modifier = Modifier
) {
    if (actual.size != 4 || feelsLike.size != 4) return

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(CURVE_HEIGHT)
    ) {
        val top = TOP_INSET.toPx()
        val bottom = BOTTOM_INSET.toPx()
        val sideInset = SIDE_INSET.toPx()

        val all = actual + feelsLike
        val low = all.min()
        val high = all.max()
        val span = (high - low).takeIf { it > 0.01f } ?: 1f
        //  Padded above and below so the labels above the highest point and beneath the lowest one
        //  have room, and a flat day does not draw as a line jammed against an edge.
        val padding = span * 0.38f
        val step = (size.width - sideInset * 2) / 3f

        fun x(index: Int) = sideInset + index * step
        fun y(value: Float) =
            bottom - ((value - (low - padding)) / (span + padding * 2)) * (bottom - top)

        fun pathOf(values: List<Float>) = Path().apply {
            values.forEachIndexed { index, value ->
                if (index == 0) moveTo(x(index), y(value)) else lineTo(x(index), y(value))
            }
        }

        //  The area under the measured temperature, fading out downwards.
        drawPath(
            path = pathOf(actual).apply {
                lineTo(x(3), bottom)
                lineTo(x(0), bottom)
                close()
            },
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                startY = top,
                endY = bottom
            )
        )

        drawPath(
            path = pathOf(feelsLike),
            color = secondaryColor,
            style = Stroke(
                width = 1.6.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                )
            )
        )

        drawPath(
            path = pathOf(actual),
            color = accent,
            style = Stroke(width = 2.2.dp.toPx())
        )

        actual.forEachIndexed { index, value ->
            drawCircle(
                color = pointFill,
                radius = 3.4.dp.toPx(),
                center = Offset(x(index), y(value))
            )
            drawCircle(
                color = accent,
                radius = 3.4.dp.toPx(),
                center = Offset(x(index), y(value)),
                style = Stroke(width = 2.dp.toPx())
            )

            //  Each label on the outer side of its pair: measured above and felt below while the
            //  day feels cooler than it is, the other way round when it feels warmer. Both were
            //  drawn on the same side before, and met between the two lines whenever they ran
            //  close together.
            val feltIsAbove = feelsLike[index] > value
            val actualTop =
                if (feltIsAbove) y(value) + 7.dp.toPx()
                else y(value) - 10.dp.toPx() - 12.sp.toPx()
            val feltTop =
                if (feltIsAbove) y(feelsLike[index]) - 8.dp.toPx() - 10.5.sp.toPx()
                else y(feelsLike[index]) + 5.dp.toPx()

            drawCentredText(
                textMeasurer, format(value), x(index), actualTop, valueColor, 12.sp.value
            )
            drawCentredText(
                textMeasurer, format(feelsLike[index]), x(index), feltTop,
                secondaryColor, 10.5.sp.value
            )
            drawCentredText(
                textMeasurer, periodLabels.getOrElse(index) { "" }, x(index),
                size.height - 13.dp.toPx(), secondaryColor, 10.5.sp.value
            )
        }
    }
}

/** Draws one short label centred on [centreX]; the measurer gives the width to offset by. */
private fun DrawScope.drawCentredText(
    textMeasurer: TextMeasurer,
    text: String,
    centreX: Float,
    topY: Float,
    color: Color,
    fontSize: Float
) {
    val style = TextStyle(color = color, fontSize = fontSize.sp)
    val measured = textMeasurer.measure(text, style)
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(centreX - measured.size.width / 2f, topY)
    )
}

private val CURVE_HEIGHT = 132.dp
private val TOP_INSET = 30.dp
private val BOTTOM_INSET = 96.dp
private val SIDE_INSET = 34.dp
