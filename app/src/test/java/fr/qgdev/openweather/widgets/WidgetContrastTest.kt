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

package fr.qgdev.openweather.widgets

import fr.qgdev.openweather.ui.theme.readableOn
import fr.qgdev.openweather.ui.theme.contrast
import fr.qgdev.openweather.ui.theme.MIN_TEXT_CONTRAST
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import fr.qgdev.openweather.ui.theme.ConditionFamily
import fr.qgdev.openweather.ui.theme.DarkWeatherPalette
import fr.qgdev.openweather.ui.theme.LightWeatherPalette
import fr.qgdev.openweather.ui.theme.sky
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetContrastTest {

    private val lightWallpaper = Color(0xFF87B5E0)   // a clear sky photo
    private val darkPalette = DarkWeatherPalette

    @Test
    fun `labels reach a readable contrast on every opaque sky`() {
        for (palette in listOf(DarkWeatherPalette, LightWeatherPalette)) {
            ConditionFamily.entries.forEach { family ->
                val ground = widgetGround(family.sky(palette.isDark), palette.textPrimary, null, 0)
                val adjusted = palette.readableOn(ground)
                assertTrue(
                    "$family, dark=${palette.isDark}",
                    contrast(adjusted.textQuiet, ground) >= MIN_TEXT_CONTRAST
                )
            }
        }
    }

    @Test
    fun `a colour that is already readable is left alone`() {
        val ground = Color.Black
        assertEquals(darkPalette.textSecondary, readableOn(ground, darkPalette.textSecondary, darkPalette.textPrimary))
    }

    @Test
    fun `secondary texts get closer to the primary text as the sky turns transparent`() {
        val sky = ConditionFamily.CLOUD.sky(isDark = true)
        val opaque = darkPalette.readableOn(widgetGround(sky, darkPalette.textPrimary, lightWallpaper, 0))
        val seeThrough = darkPalette.readableOn(widgetGround(sky, darkPalette.textPrimary, lightWallpaper, 40))
        assertTrue(seeThrough.textQuiet.luminance() > opaque.textQuiet.luminance())
    }

    @Test
    fun `when nothing less will do, a secondary text becomes the primary text`() {
        val ground = Color(0xFFE8EEF4)   // white text cannot reach 4.5:1 here
        assertEquals(darkPalette.textPrimary, readableOn(ground, darkPalette.textQuiet, darkPalette.textPrimary))
    }
}
