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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The redesign's own colours, alongside the Material scheme rather than inside it.
 *
 * Material's scheme follows the wallpaper when dynamic colour is on. Two families of colour here
 * cannot: a condition sky says which weather it is, and a state colour says whether something
 * failed - neither can be allowed to drift with someone's wallpaper. Everything that carries no
 * such meaning (Settings surfaces, the system bars, ordinary controls) keeps using
 * `MaterialTheme.colorScheme` and still follows the wallpaper.
 *
 * The canvas was drawn in dark only. The light values below are the same roles re-tuned for a light
 * ground: the same accent hues, text darkened rather than lightened, and skies re-mixed in
 * [ConditionSky] rather than simply inverted.
 */
@Immutable
data class WeatherPalette(
    val isDark: Boolean,

    /** Page behind the cards. */
    val screen: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textQuiet: Color,

    /** Primary action: the add button, links, the pull-to-refresh indicator. */
    val accent: Color,
    val onAccent: Color,

    /** Selected tab, alerts, quota warnings. */
    val amber: Color,
    /** Stale data, offline, a place whose update failed. */
    val orange: Color,
    /** Destructive actions and a refused API key. */
    val red: Color,
    /** Up to date, a valid key. */
    val green: Color,

    val outline: Color,
    val outlineStrong: Color,

    /** Ground for the stat tiles laid over a condition sky. */
    val tileScrim: Color,

    /** Air quality index 1 to 5, by index - `aqi[index - 1]`. */
    val aqi: List<Color>
) {
    /** The colour for an air quality index, tolerant of the 0 an absent measurement leaves behind. */
    fun aqiColor(index: Int): Color? = aqi.getOrNull(index - 1)
}

private val Accent = Color(0xFF5B9BD5)
private val Amber = Color(0xFFF5B547)
private val Orange = Color(0xFFE0704A)
private val Red = Color(0xFFD64848)
private val Green = Color(0xFF4FBF7B)

private val AqiDark = listOf(
    Color(0xFF5FD08C),  // 1 good
    Color(0xFFF5B547),  // 2 moderate
    Color(0xFFEC8560),  // 3 unhealthy for sensitive groups
    Color(0xFFE8706F),  // 4 unhealthy
    Color(0xFFB98AE0)   // 5 very unhealthy
)

//  Darkened for a light ground: the dark set is tuned for contrast against black and would sit too
//  pale on white, which matters here because the index colour is the whole signal.
private val AqiLight = listOf(
    Color(0xFF1F9D57),
    Color(0xFFB07407),
    Color(0xFFC1511F),
    Color(0xFFC62F2E),
    Color(0xFF7B37B5)
)

val DarkWeatherPalette = WeatherPalette(
    isDark = true,
    screen = Color(0xFF000000),
    textPrimary = Color(0xFFF5F8FB),
    textSecondary = Color(0xFFC6CFD9),
    textMuted = Color(0xFFA3B0BF),
    textQuiet = Color(0xFF7A879A),
    accent = Accent,
    onAccent = Color(0xFF06121F),
    amber = Amber,
    orange = Orange,
    red = Red,
    green = Green,
    outline = Color(0x1FFFFFFF),
    outlineStrong = Color(0x2EFFFFFF),
    tileScrim = Color(0x57040810),
    aqi = AqiDark
)

val LightWeatherPalette = WeatherPalette(
    isDark = false,
    screen = Color(0xFFF4F6F9),
    textPrimary = Color(0xFF0E1621),
    textSecondary = Color(0xFF3B4756),
    textMuted = Color(0xFF5A6675),
    textQuiet = Color(0xFF77838F),
    accent = Color(0xFF1F6FB8),
    onAccent = Color(0xFFFFFFFF),
    amber = Color(0xFFB07407),
    orange = Color(0xFFC1511F),
    red = Color(0xFFC62F2E),
    green = Color(0xFF1F9D57),
    outline = Color(0x1A0E1621),
    outlineStrong = Color(0x2E0E1621),
    tileScrim = Color(0x66FFFFFF),
    aqi = AqiLight
)

val LocalWeatherPalette = staticCompositionLocalOf { DarkWeatherPalette }
