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

package fr.qgdev.openweather.ui.components.forecasts.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.R

/**
 * MoonPhaseIndicator
 *
 * Draws a moon phase icon on a canvas, mirroring the Java DailyForecastGraphView.drawMoonPhase().
 * moonPhase values follow the OWM convention:
 *   0.0 / 1.0 = new moon, 0.25 = first quarter, 0.5 = full moon, 0.75 = last quarter
 *
 * @param modifier    Modifier for sizing/placement
 * @param moonPhase   Moon phase coefficient in [0.0, 1.0]
 */
@Composable
fun MoonPhaseIndicator(
    modifier: Modifier = Modifier,
    moonPhase: Float
) {
    val lightColor  = colorResource(R.color.colorMoonLight)
    val shadowColor = colorResource(R.color.colorMoonShadow)

    Canvas(modifier = modifier) {
        val halfSide     = size.minDimension / 2f
        val circleRadius = halfSide / 2f - 3f
        val cx           = size.width  / 2f
        val cy           = size.height / 2f

        // Full lit moon base
        drawCircle(
            color  = lightColor,
            radius = circleRadius,
            center = Offset(cx, cy),
            style  = Fill
        )
        // Outline
        drawCircle(
            color  = shadowColor,
            radius = circleRadius + 1f,
            center = Offset(cx, cy),
            style  = Stroke(width = 5f)
        )

        // Full moon → no shadow needed
        if (moonPhase == 0.5f) return@Canvas

        drawMoonShadow(moonPhase, cx, cy, circleRadius, shadowColor)
    }
}

/**
 * Draws the shadow portion of the moon phase by building a single Path of overlapping
 * vertical-chord arcs, translated directly from the original Java column-by-column algorithm.
 */
private fun DrawScope.drawMoonShadow(
    phase: Float,
    cx: Float,
    cy: Float,
    radius: Float,
    shadowColor: Color
) {
    val left     = cx - radius
    val right    = cx + radius
    val top      = cy - radius
    val bottom   = cy + radius
    val diameter = (radius * 2).toInt()

    //  dX is the terminator x-offset (0 = new moon boundary, diameter = opposite boundary)
    val dX = (diameter * ((phase * 2f) % 1f)).toInt()

    val startX: Int
    val stopX:  Int

    if (phase < 0.5f) {
        //  Waxing (right side illuminated): shadow from terminator to right edge then left
        startX = dX
        stopX  = diameter
    } else {
        //  Waning (left side illuminated): shadow from left edge to terminator
        startX = 0
        stopX  = dX
    }

    val path = Path()

    for (i in startX..stopX) {
        val xi = left + i
        if (xi < cx) {
            //  Left half of the terminator: right-opening semicircle arc
            val w = right - i - xi
            if (w > 0f) {
                path.addArc(
                    oval                = Rect(xi, top, right - i, bottom),
                    startAngleDegrees   = -90f,
                    sweepAngleDegrees   = 180f
                )
            }
        } else {
            //  Right half: left-opening semicircle arc
            val arcLeft  = right - i
            val arcRight = xi
            val w = arcRight - arcLeft
            if (w > 0f) {
                path.addArc(
                    oval                = Rect(arcLeft, top, arcRight, bottom),
                    startAngleDegrees   = 90f,
                    sweepAngleDegrees   = 180f
                )
            }
        }
    }

    drawPath(
        path  = path,
        color = shadowColor,
        style = Stroke(width = 1f)
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun MoonPhaseNewMoonPreview() {
    MoonPhaseIndicator(modifier = Modifier.width(80.dp).height(80.dp), moonPhase = 0f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun MoonPhaseFirstQuarterPreview() {
    MoonPhaseIndicator(modifier = Modifier.width(80.dp).height(80.dp), moonPhase = 0.25f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun MoonPhaseFullMoonPreview() {
    MoonPhaseIndicator(modifier = Modifier.width(80.dp).height(80.dp), moonPhase = 0.5f)
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun MoonPhaseLastQuarterPreview() {
    MoonPhaseIndicator(modifier = Modifier.width(80.dp).height(80.dp), moonPhase = 0.75f)
}

