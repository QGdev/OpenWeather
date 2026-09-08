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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import fr.qgdev.openweather.R

/*
 * The redesign's two typefaces, bundled rather than downloaded: Google's downloadable fonts go
 * through Play services, and the app promises that only a place's coordinates and the search text
 * leave the phone. Both are under the SIL Open Font License 1.1; their licence files ship in
 * assets/licenses, and the fonts are used unmodified - IBM Plex reserves its name for unmodified
 * versions, which rules out subsetting it.
 */

/** Figtree, © 2022 The Figtree Project Authors. A variable font: every weight is one file. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val Figtree = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(
            R.font.figtree,
            weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
        )
    }
)

/** IBM Plex Mono, © 2017 IBM Corp., for what the redesign sets as raw data: keys, versions, dumps. */
val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium)
)

/** Material's type scale, sizes unchanged, set in Figtree. */
val AppTypography: Typography = Typography().run {
    fun TextStyle.inFigtree() = copy(fontFamily = Figtree)
    Typography(
        displayLarge = displayLarge.inFigtree(),
        displayMedium = displayMedium.inFigtree(),
        displaySmall = displaySmall.inFigtree(),
        headlineLarge = headlineLarge.inFigtree(),
        headlineMedium = headlineMedium.inFigtree(),
        headlineSmall = headlineSmall.inFigtree(),
        titleLarge = titleLarge.inFigtree(),
        titleMedium = titleMedium.inFigtree(),
        titleSmall = titleSmall.inFigtree(),
        bodyLarge = bodyLarge.inFigtree(),
        bodyMedium = bodyMedium.inFigtree(),
        bodySmall = bodySmall.inFigtree(),
        labelLarge = labelLarge.inFigtree(),
        labelMedium = labelMedium.inFigtree(),
        labelSmall = labelSmall.inFigtree()
    )
}
