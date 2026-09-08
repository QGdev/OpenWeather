
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

package fr.qgdev.openweather.widgets

import android.app.WallpaperManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import fr.qgdev.openweather.ui.theme.Sky
import fr.qgdev.openweather.ui.theme.contrast

/**
 * Keeping a widget's text readable over a sky the user may have made transparent.
 *
 * The wallpaper is never read. Android shares its dominant colour through WallpaperColors, which
 * needs no permission, and the ground under the text is estimated as the sky mixed towards that
 * colour by the transparency. It is an estimate: the part of the wallpaper under the widget may
 * differ from its dominant colour.
 */

/** When the wallpaper's colours are unknown, a mid grey: the hardest ground for either text colour. */
private val UNKNOWN_WALLPAPER = Color(0xFF808080)

internal fun dominantWallpaperColor(context: Context): Color? =
    WallpaperManager.getInstance(context)
        .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        ?.primaryColor
        ?.toArgb()
        ?.let { Color(it) }

/**
 * The ground under the widget's text: the sky's lighter or darker end, whichever is harder on the
 * text, mixed towards the wallpaper as the sky turns transparent.
 */
internal fun widgetGround(sky: Sky, text: Color, wallpaper: Color?, transparency: Int): Color {
    val skyGround = listOf(sky.start, sky.middle).minBy { contrast(text, it) }
    return lerp(skyGround, wallpaper ?: UNKNOWN_WALLPAPER, transparency.coerceIn(0, 100) / 100f)
}
