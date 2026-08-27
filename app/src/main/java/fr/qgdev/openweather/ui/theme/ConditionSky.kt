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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The six skies every entry is painted with.
 *
 * The app knows eleven condition families (see the design brief), but eleven backgrounds would read
 * as eleven unrelated colours. They collapse into six hues instead, and intensity is carried by the
 * icon and the values rather than by a new colour: heavy rain and drizzle share the rain sky.
 *
 * Night is a family of its own only for a clear sky - a clear night is indigo where a clear midday
 * is amber. A cloudy night stays with the clouds: its hue is already cold and dark.
 */
enum class ConditionFamily {
    SUN, CLOUD, RAIN, STORM, SNOW, NIGHT
}

/**
 * Maps an OpenWeatherMap condition code to the sky it is painted with.
 *
 * The code ranges are those of the brief; [isDaytime] only ever moves a clear sky to [NIGHT].
 */
fun conditionFamily(weatherCode: Int, isDaytime: Boolean): ConditionFamily = when (weatherCode) {
    in 200..232 -> ConditionFamily.STORM
    in 300..321, in 500..531 -> ConditionFamily.RAIN
    in 600..622 -> ConditionFamily.SNOW
    in 701..781 -> ConditionFamily.CLOUD
    800 -> if (isDaytime) ConditionFamily.SUN else ConditionFamily.NIGHT
    in 801..804 -> ConditionFamily.CLOUD
    else -> ConditionFamily.CLOUD
}

/**
 * One sky: the wash itself, the glow that sits behind the weather icon, its outline, and the tint
 * the condition icon is drawn in.
 */
@Immutable
data class Sky(
    val start: Color,
    val middle: Color,
    val end: Color,
    val halo: Color,
    val border: Color,
    val icon: Color
) {
    /**
     * The diagonal wash. It fades to [end] at the bottom of the entry so that values keep their
     * contrast whatever the condition - the colour never reaches the numbers.
     */
    val wash: Brush get() = Brush.linearGradient(
        colors = listOf(start, middle, end),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    /** The condition's glow, placed off the top-right corner. */
    val haloBrush: Brush get() = Brush.radialGradient(
        colors = listOf(halo, Color.Transparent)
    )

}

private val DarkSkies = mapOf(
    ConditionFamily.SUN to Sky(
        start = Color(0xFF241D0E), middle = Color(0xFF3B2C12), end = Color(0xFF06090F),
        halo = Color(0x66FFC45C), border = Color(0x47F5B547), icon = Color(0xFFF5B547)
    ),
    ConditionFamily.CLOUD to Sky(
        start = Color(0xFF1B2438), middle = Color(0xFF2B3A56), end = Color(0xFF060B14),
        halo = Color(0x42FFC65E), border = Color(0x24FFFFFF), icon = Color(0xFFC3CDD9)
    ),
    ConditionFamily.RAIN to Sky(
        start = Color(0xFF0E2130), middle = Color(0xFF154055), end = Color(0xFF040D14),
        halo = Color(0x4D5AA9E6), border = Color(0x4D5AA9E6), icon = Color(0xFF7FB6E6)
    ),
    ConditionFamily.STORM to Sky(
        start = Color(0xFF1E1436), middle = Color(0xFF2E1C4C), end = Color(0xFF080610),
        halo = Color(0x4DA878E8), border = Color(0x4DA878E8), icon = Color(0xFFBE94E0)
    ),
    ConditionFamily.SNOW to Sky(
        start = Color(0xFF18262F), middle = Color(0xFF25404E), end = Color(0xFF060C11),
        halo = Color(0x42B4E0F4), border = Color(0x4DA8D6E8), icon = Color(0xFFC8DEF0)
    ),
    ConditionFamily.NIGHT to Sky(
        start = Color(0xFF0C1036), middle = Color(0xFF141A44), end = Color(0xFF02040B),
        halo = Color(0x4296A4FF), border = Color(0x427E8CFF), icon = Color(0xFF9BB2DE)
    )
)

//  Re-mixed rather than inverted: the dark skies are dark blues and browns that simply become mud
//  when lightened. Each keeps its hue, applied as a pale tint that fades to the page, and the icon
//  tint is darkened enough to stay legible against it.
private val LightSkies = mapOf(
    ConditionFamily.SUN to Sky(
        start = Color(0xFFFFE9C2), middle = Color(0xFFFFF3DE), end = Color(0xFFFCFBF7),
        halo = Color(0x59FFC45C), border = Color(0x3DB07407), icon = Color(0xFFB07407)
    ),
    ConditionFamily.CLOUD to Sky(
        start = Color(0xFFDCE4EE), middle = Color(0xFFEDF1F6), end = Color(0xFFFAFBFC),
        halo = Color(0x338A97A6), border = Color(0x24243441), icon = Color(0xFF4F6070)
    ),
    ConditionFamily.RAIN to Sky(
        start = Color(0xFFCFE3F3), middle = Color(0xFFE6F1F9), end = Color(0xFFF8FBFD),
        halo = Color(0x3D2E7FBF), border = Color(0x3D1F6FB8), icon = Color(0xFF1F6FB8)
    ),
    ConditionFamily.STORM to Sky(
        start = Color(0xFFE3D7F3), middle = Color(0xFFF0E9F9), end = Color(0xFFFBF9FD),
        halo = Color(0x3D7B37B5), border = Color(0x3D7B37B5), icon = Color(0xFF6A2E9E)
    ),
    ConditionFamily.SNOW to Sky(
        start = Color(0xFFDDEEF6), middle = Color(0xFFEEF7FB), end = Color(0xFFFAFDFE),
        halo = Color(0x3D5FA8C6), border = Color(0x3D3E7E99), icon = Color(0xFF3E7E99)
    ),
    ConditionFamily.NIGHT to Sky(
        start = Color(0xFFD9DCF2), middle = Color(0xFFEAECF8), end = Color(0xFFF9FAFD),
        halo = Color(0x3D4A5FA8), border = Color(0x3D3D4E8C), icon = Color(0xFF3D4E8C)
    )
)

/** The sky for this condition, in the theme currently in force. */
@Composable
@ReadOnlyComposable
fun ConditionFamily.sky(): Sky = sky(LocalWeatherPalette.current.isDark)

/** The sky for this condition, for callers that already know which theme they are drawing in. */
fun ConditionFamily.sky(isDark: Boolean): Sky =
    (if (isDark) DarkSkies else LightSkies).getValue(this)

