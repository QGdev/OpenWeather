
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

package fr.qgdev.openweather.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/*
 * Keeping secondary texts readable on whatever they end up drawn on.
 *
 * The palette's secondary shades are tuned for the screens' own ground. Drawn elsewhere - a
 * dialog's lighter surface, a widget whose sky lets the wallpaper through - the faintest of them
 * can fall under a readable contrast, so they are moved towards the primary text just enough.
 */

/** WCAG's minimum contrast for body text, which the app's small labels are. */
const val MIN_TEXT_CONTRAST = 4.5f

/** WCAG contrast ratio between two colours, from 1 to 21. */
fun contrast(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return maxOf(la, lb) / minOf(la, lb)
}

/**
 * [color] moved towards [strongest] just enough to reach [MIN_TEXT_CONTRAST] on [ground].
 *
 * A secondary text keeps its own shade while the ground allows it, so the hierarchy between the
 * texts holds; as the ground gets harder it moves towards the primary text, and ends as the primary
 * text when nothing less will do.
 */
fun readableOn(ground: Color, color: Color, strongest: Color): Color {
    if (contrast(color, ground) >= MIN_TEXT_CONTRAST) return color
    for (step in 1..10) {
        val candidate = lerp(color, strongest, step / 10f)
        if (contrast(candidate, ground) >= MIN_TEXT_CONTRAST) return candidate
    }
    return strongest
}

/** The palette with its secondary texts made readable on [ground]. */
fun WeatherPalette.readableOn(ground: Color): WeatherPalette = copy(
    textSecondary = readableOn(ground, textSecondary, textPrimary),
    textMuted = readableOn(ground, textMuted, textPrimary),
    textQuiet = readableOn(ground, textQuiet, textPrimary)
)
