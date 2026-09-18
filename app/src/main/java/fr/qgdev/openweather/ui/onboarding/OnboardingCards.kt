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

package fr.qgdev.openweather.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.PlexMono
import kotlinx.coroutines.launch

/** An OpenWeatherMap key: 32 letters and digits. */
const val API_KEY_LENGTH = 32

/** Whether [key] has the shape of an OpenWeatherMap key; only the server can say it works. */
fun isWellFormedApiKey(key: String): Boolean =
    key.length == API_KEY_LENGTH && key.all { it.isLetterOrDigit() }

/**
 * The key step: what the key is for, how to get one, and where to put it. Shown by the onboarding,
 * and by the dialog a missing or malformed key opens from the list - in place of the one line that
 * sent the user to the settings to work it out alone.
 *
 * @param currentKey the key saved so far, if any: a malformed one is shown back to be corrected.
 */
@Composable
fun ApiKeyCard(
    currentKey: String,
    onSave: (String) -> Unit
) {
    val palette = LocalWeatherPalette.current
    val accent = MaterialTheme.colorScheme.primary
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var key by remember { mutableStateOf(currentKey) }
    val valid = isWellFormedApiKey(key)
    val save = { if (valid) onSave(key) }

    OnboardingCard {
        OnboardingBadge { color -> drawKey(color) }
        //  Titled after the key it was opened with, not the one being typed, so it holds still:
        //  asking for a first key, correcting a malformed one, or showing one that works.
        OnboardingTitle(
            stringResource(
                when {
                    currentKey.isEmpty() -> R.string.onboarding_key_title
                    isWellFormedApiKey(currentKey) -> R.string.onboarding_key_title_valid
                    else -> R.string.onboarding_key_title_fix
                }
            )
        )
        OnboardingBody(stringResource(R.string.onboarding_key_body))

        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                R.string.onboarding_key_step_account,
                R.string.onboarding_key_step_subscribe,
                R.string.onboarding_key_step_copy
            ).forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text(
                        text = "${index + 1}",
                        color = accent,
                        fontFamily = PlexMono,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .width(14.dp)
                            .padding(top = 1.dp)
                    )
                    Text(
                        text = stringResource(step),
                        color = palette.textSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        //  The field, with a paste action inside: the key is copied from the website, never typed.
        val fieldShape = RoundedCornerShape(14.dp)
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(fieldShape)
                .border(
                    1.dp,
                    if (valid) accent.copy(alpha = 0.45f) else palette.outlineStrong,
                    fieldShape
                )
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val fieldStyle = TextStyle(
                color = palette.textPrimary,
                fontFamily = PlexMono,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (key.isEmpty()) {
                    Text(
                        text = stringResource(R.string.onboarding_key_hint, API_KEY_LENGTH),
                        style = fieldStyle.copy(color = palette.textQuiet),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = key,
                    //  Spaces and line breaks come along with a copied key; they are never part of it.
                    onValueChange = { key = it.filterNot(Char::isWhitespace) },
                    singleLine = true,
                    textStyle = fieldStyle,
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = stringResource(R.string.action_paste),
                color = palette.textMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        scope.launch {
                            val pasted = clipboard.getClipEntry()?.clipData
                                ?.takeIf { it.itemCount > 0 }
                                ?.getItemAt(0)?.text?.toString()
                            if (pasted != null) key = pasted.filterNot(Char::isWhitespace)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        //  Counted while it is not whole, so a key cut short while copying is caught here rather
        //  than by a refused request.
        if (key.isNotEmpty() && !valid) {
            Row(
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(palette.amber)
                )
                Text(
                    text = when {
                        key.length < API_KEY_LENGTH ->
                            stringResource(R.string.onboarding_key_too_short, key.length, API_KEY_LENGTH)
                        key.length > API_KEY_LENGTH ->
                            stringResource(R.string.onboarding_key_too_long, key.length, API_KEY_LENGTH)
                        else -> stringResource(R.string.onboarding_key_not_alphanumeric)
                    },
                    color = palette.textSecondary,
                    fontSize = 11.5.sp
                )
            }
        }

        OnboardingButton(
            text = stringResource(R.string.onboarding_key_save),
            enabled = valid,
            onClick = save,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_key_open_website),
            color = palette.textMuted,
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { uriHandler.openUri(OPENWEATHERMAP_URL) }
                .padding(vertical = 8.dp)
        )
    }
}

/**
 * The first place: the onboarding's last step, and the middle of an empty list, where the add
 * button alone is easy to miss. [footer] takes what follows the button, such as "Later".
 */
@Composable
fun FirstPlaceCard(
    onAddPlace: () -> Unit,
    footer: @Composable ColumnScope.() -> Unit = {}
) {
    OnboardingCard {
        OnboardingBadge { color -> drawPin(color) }
        OnboardingTitle(stringResource(R.string.onboarding_place_title))
        OnboardingBody(stringResource(R.string.onboarding_place_body))
        OnboardingButton(
            text = stringResource(R.string.title_dialog_add_place),
            enabled = true,
            onClick = onAddPlace,
            modifier = Modifier.padding(top = 20.dp)
        )
        footer()
    }
}

private const val OPENWEATHERMAP_URL = "https://openweathermap.org/api"

/** The card: a faint wash of the accent fading into the screen, under an accent outline. */
@Composable
private fun OnboardingCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(24.dp)
    val wash = palette.accent.copy(alpha = 0.14f).compositeOver(palette.screen)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    0f to wash,
                    0.6f to palette.accent.copy(alpha = 0.05f).compositeOver(palette.screen),
                    1f to palette.screen
                )
            )
            .border(1.dp, palette.accent.copy(alpha = 0.3f), shape)
            .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 22.dp),
        content = content
    )
}

/** The rounded square holding the card's icon, drawn in the system accent. */
@Composable
private fun OnboardingBadge(draw: DrawScope.(androidx.compose.ui.graphics.Color) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(38.dp)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(19.dp)) { draw(accent) }
    }
}

@Composable
private fun OnboardingTitle(text: String) {
    Text(
        text = text,
        color = LocalWeatherPalette.current.textPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
private fun OnboardingBody(text: String) {
    Text(
        text = text,
        color = LocalWeatherPalette.current.textSecondary,
        fontSize = 12.5.sp,
        lineHeight = 19.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/** The card's one action, a full-width pill in the system accent; faded until it can be used. */
@Composable
internal fun OnboardingButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** The mock-up's key, on a 24-unit grid: a ring, its shaft and one tooth. */
private fun DrawScope.drawKey(color: androidx.compose.ui.graphics.Color) {
    val unit = size.minDimension / 24f
    val stroke = Stroke(width = 1.9f * unit, cap = StrokeCap.Round)
    drawCircle(color, radius = 4f * unit, center = Offset(8.5f * unit, 12f * unit), style = stroke)
    drawLine(color, Offset(12.5f * unit, 12f * unit), Offset(21f * unit, 12f * unit), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(18f * unit, 12f * unit), Offset(18f * unit, 15.5f * unit), stroke.width, StrokeCap.Round)
}

/** A map pin on the same grid, the Places tab's own sign. */
private fun DrawScope.drawPin(color: androidx.compose.ui.graphics.Color) {
    val unit = size.minDimension / 24f
    val stroke = Stroke(width = 1.9f * unit, cap = StrokeCap.Round)
    val pin = androidx.compose.ui.graphics.Path().apply {
        moveTo(12f * unit, 21f * unit)
        cubicTo(12f * unit, 21f * unit, 5f * unit, 14.5f * unit, 5f * unit, 9.5f * unit)
        cubicTo(5f * unit, 5.6f * unit, 8.1f * unit, 2.5f * unit, 12f * unit, 2.5f * unit)
        cubicTo(15.9f * unit, 2.5f * unit, 19f * unit, 5.6f * unit, 19f * unit, 9.5f * unit)
        cubicTo(19f * unit, 14.5f * unit, 12f * unit, 21f * unit, 12f * unit, 21f * unit)
        close()
    }
    drawPath(pin, color, style = stroke)
    drawCircle(color, radius = 2.5f * unit, center = Offset(12f * unit, 9.5f * unit), style = stroke)
}
