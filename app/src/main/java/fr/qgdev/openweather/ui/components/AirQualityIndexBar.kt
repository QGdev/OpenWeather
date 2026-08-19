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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.ui.theme.AppTheme

@Composable
fun AirQualityIndexBar(
    modifier: Modifier = Modifier,
    value: Int = 1,
    labelText: String = "N/A",
    mainColor: Color = Color.Gray,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    subtextColor: Color = Color.Gray
) {
    val innerElementMargin = 5.dp
    val barThickness = 50.dp
    val textLabelSize = 45.sp
    val textValueSize = 50.sp
    val subtextValueSize = 20.sp

    val halfBarThickness = barThickness / 2
    val innerElementRadius = halfBarThickness - innerElementMargin

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barThickness)
    ) {

        //  Convert every dp to px
        val innerElementMarginPx = innerElementMargin.toPx()
        val barThicknessPx = barThickness.toPx()
        val textLabelSizePx = textLabelSize.toPx()
        val textValueSizePx = textValueSize.toPx()
        val subtextValueSizePx = subtextValueSize.toPx()
        val halfBarThicknessPx = halfBarThickness.toPx()
        val innerElementRadiusPx = innerElementRadius.toPx()
        val innerMargin = innerElementMarginPx

        //  Get the global dimensions
        val width = size.width

        //  Calculate the index background dimensions
        val indexXOrigin = innerElementMarginPx
        val indexYOrigin = innerElementMarginPx
        val indexWidth = innerElementRadiusPx * 2
        val indexHeight = indexWidth

        //  Calculate the bar dimensions
        val barXOrigin = innerElementMarginPx * 5 + indexWidth
        val barYOrigin = innerElementMarginPx
        val barWidth = innerElementRadiusPx * 2
        val barLength = width - barXOrigin - innerElementMarginPx


        //  Draw the background bar
        drawRoundRect(
            color = mainColor,
            topLeft = Offset(0F, 0F),
            size = Size(size.width, barThicknessPx),
            cornerRadius = CornerRadius(halfBarThicknessPx)
        )

        //  Draw the background circle for the value
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(indexXOrigin, indexYOrigin),
            size = Size(indexWidth, indexHeight),
            cornerRadius = CornerRadius(innerElementRadiusPx)
        )

        //  Draw the rounded rectangle for the value description
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(barXOrigin, barYOrigin),
            size = Size(barLength, barWidth),
            cornerRadius = CornerRadius(halfBarThicknessPx)
        )

        //  Draw all of the text elements
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                color = textColor.toArgb()
                textSize = textValueSize.value
                textAlign = android.graphics.Paint.Align.CENTER
            }

            //  Draw the value
            canvas.nativeCanvas.drawText(
                value.toString(),
                indexXOrigin + innerElementRadiusPx,
                indexYOrigin + innerElementRadiusPx + textValueSize.value / 6,
                paint
            )

            paint.textSize = subtextValueSize.value
            paint.color = subtextColor.toArgb()

            //  Draw the AQI label
            canvas.nativeCanvas.drawText(
                "AQI",
                indexXOrigin + innerElementRadiusPx,
                indexYOrigin + innerElementRadiusPx + textValueSize.value / 1.25f,
                paint
            )

            paint.textSize = textLabelSize.value
            paint.color = textColor.toArgb()

            val labelX = (barXOrigin + barXOrigin + barLength) / 2

            //  Draw the label
            canvas.nativeCanvas.drawText(
                labelText,
                labelX,
                halfBarThicknessPx + textLabelSize.value / 3,
                paint
            )
        }
    }
    
}

@Preview
@Composable
fun AqiBarViewPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //  Preview the different AQI values
            //  Colors are based on the AQI scale
            AirQualityIndexBar(
                value = 1,
                labelText = "N/A",
                mainColor = Color(0xFF00FF00),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
            AirQualityIndexBar(
                value = 50,
                labelText = "Good",
                mainColor = Color(0xFF00FF00),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
            AirQualityIndexBar(
                value = 100,
                labelText = "Moderate",
                mainColor = Color(0xFFFFFF00),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
            AirQualityIndexBar(
                value = 150,
                labelText = "Unhealthy for sensitive groups",
                mainColor = Color(0xFFFF7F00),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
            AirQualityIndexBar(
                value = 200,
                labelText = "Unhealthy",
                mainColor = Color(0xFFFF0000),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
            AirQualityIndexBar(
                value = 300,
                labelText = "Very unhealthy",
                mainColor = Color(0xFF7F007F),
                backgroundColor = Color.White,
                textColor = Color.Black,
                subtextColor = Color.Gray
            )
        }

    }
}
