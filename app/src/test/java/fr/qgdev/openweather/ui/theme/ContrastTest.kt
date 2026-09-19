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

package fr.qgdev.openweather.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    //  The dialogs' surface in the dark theme, as measured on a device: surface tinted by 10 dp.
    private val darkDialogSurface = Color(0xFF1A2738)

    /**
     * Test the faintest dark text on a dialog.
     * Test will not pass if it isn't lifted to a readable contrast.
     */
    @Test
    fun `the faintest dark text falls short on a dialog, and is lifted to a readable contrast`() {
        assertTrue(contrast(DarkWeatherPalette.textQuiet, darkDialogSurface) < MIN_TEXT_CONTRAST)
        val adjusted = DarkWeatherPalette.readableOn(darkDialogSurface)
        assertTrue(contrast(adjusted.textQuiet, darkDialogSurface) >= MIN_TEXT_CONTRAST)
    }

    /**
     * Test shades which are already readable.
     * Test will not pass if their colour is changed.
     */
    @Test
    fun `shades that are already readable keep their colour`() {
        val adjusted = DarkWeatherPalette.readableOn(darkDialogSurface)
        assertEquals(DarkWeatherPalette.textSecondary, adjusted.textSecondary)
    }

    /**
     * Test texts drawn on the own ground of the screens.
     * Test will not pass if a colour is changed.
     */
    @Test
    fun `on the screens' own ground nothing changes`() {
        assertEquals(DarkWeatherPalette.textSecondary, DarkWeatherPalette.readableOn(DarkWeatherPalette.screen).textSecondary)
        assertEquals(LightWeatherPalette.textMuted, LightWeatherPalette.readableOn(LightWeatherPalette.screen).textMuted)
    }
}
