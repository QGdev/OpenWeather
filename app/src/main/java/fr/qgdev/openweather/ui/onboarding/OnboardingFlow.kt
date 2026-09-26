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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.ui.components.dialogs.AddPlaceDialog
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.PlexMono
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel

/** The onboarding's steps, in order. */
private enum class OnboardingStep { WELCOME, API_KEY, FIRST_PLACE }

/**
 * The first run, before the tabs: what the app is, the key it needs, and a first place.
 *
 * Shown while the onboarding version is unset - a fresh install, or the data cleared - and again
 * from the settings on request. [onFinished] records its completion; the key is saved as soon as
 * its step is passed, so leaving at the last step loses nothing.
 */
@Composable
fun OnboardingFlow(
    settingsRepository: SettingsRepository,
    placeViewModel: PlaceViewModel,
    onFinished: () -> Unit
) {
    val settings by settingsRepository.settingsFlow.collectAsState()
    val places by placeViewModel.placesState.collectAsState()
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val step = OnboardingStep.entries[stepIndex]

    //  Back steps back through the onboarding rather than leaving the app from its middle.
    BackHandler(enabled = stepIndex > 0) { stepIndex-- }

    OnboardingBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            OnboardingTopBar(
                stepIndex = stepIndex,
                stepCount = OnboardingStep.entries.size,
                onBack = if (stepIndex > 0) ({ stepIndex-- }) else null
            )
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "onboardingStep"
            ) { shown ->
                when (shown) {
                    OnboardingStep.WELCOME -> WelcomeStep(onStart = { stepIndex++ })
                    OnboardingStep.API_KEY -> CenteredStep {
                        ApiKeyCard(
                            currentKey = settings.apiKey.orEmpty(),
                            onSave = { key ->
                                settingsRepository.setApiKey(key)
                                stepIndex++
                            },
                            oneCallVersion = settings.oneCallVersion,
                            onOneCallVersionChanged = { settingsRepository.setOneCallVersion(it) }
                        )
                    }
                    OnboardingStep.FIRST_PLACE -> FirstPlaceStep(
                        placeViewModel = placeViewModel,
                        hasPlaces = !places.isNullOrEmpty(),
                        onFinished = onFinished
                    )
                }
            }
        }
    }
}

/** The splash screen's sky, so the first run carries on from it rather than cutting to black. */
@Composable
private fun OnboardingBackground(content: @Composable () -> Unit) {
    val palette = LocalWeatherPalette.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to lerp(palette.screen, palette.accent, 0.14f),
                    0.46f to lerp(palette.screen, palette.accent, 0.06f),
                    1f to palette.screen
                )
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-70).dp, y = (-60).dp)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        listOf(palette.accent.copy(alpha = 0.22f), palette.accent.copy(alpha = 0f))
                    )
                )
        )
        content()
    }
}

/** Where the user is - one bar per step - and, past the first, the way back. */
@Composable
private fun OnboardingTopBar(stepIndex: Int, stepCount: Int, onBack: (() -> Unit)?) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp)) {
            if (onBack != null) {
                //  The dialogs' bordered button, pointing back rather than closing.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(11.dp))
                        .border(1.dp, palette.outlineStrong, RoundedCornerShape(11.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(stepCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (index == stepIndex) 22.dp else 14.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= stepIndex) MaterialTheme.colorScheme.primary
                            else palette.outlineStrong
                        )
                )
            }
        }
        Spacer(modifier = Modifier.size(34.dp))
    }
}

/** A step made of one card, held in the middle of the screen and scrolling above the keyboard. */
@Composable
private fun CenteredStep(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .heightIn(min = minHeight)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

/** What the app is, in the three things that set it apart, before anything is asked. */
@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    val palette = LocalWeatherPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.size(84.dp)
            )
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                color = palette.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 22.dp)
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                color = palette.textQuiet,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Column(
                modifier = Modifier.padding(top = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                listOf(
                    R.string.onboarding_welcome_private_title to R.string.onboarding_welcome_private_body,
                    R.string.onboarding_welcome_key_title to R.string.onboarding_welcome_key_body,
                    R.string.onboarding_welcome_free_title to R.string.onboarding_welcome_free_body
                ).forEachIndexed { index, (title, body) ->
                    WelcomePoint(number = index + 1, title = stringResource(title), body = stringResource(body))
                }
            }
        }
        OnboardingButton(
            text = stringResource(R.string.onboarding_welcome_start),
            enabled = true,
            onClick = onStart,
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
        )
    }
}

/** One of the welcome's points, numbered in the accent as the key's steps are. */
@Composable
private fun WelcomePoint(number: Int, title: String, body: String) {
    val palette = LocalWeatherPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "$number",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = PlexMono,
            fontSize = 12.sp,
            modifier = Modifier
                .width(14.dp)
                .padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = body,
                color = palette.textSecondary,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

/**
 * The last step: a first place, through the usual search. Choosing one ends the onboarding - its
 * download carries on in the list - and it can be put off.
 */
@Composable
private fun FirstPlaceStep(
    placeViewModel: PlaceViewModel,
    hasPlaces: Boolean,
    onFinished: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    var searching by rememberSaveable { mutableStateOf(false) }

    CenteredStep {
        FirstPlaceCard(onAddPlace = { searching = true }) {
            //  Places already there - the onboarding seen again from the settings - make this the
            //  end rather than a postponement.
            Text(
                text = stringResource(
                    if (hasPlaces) R.string.onboarding_finish else R.string.onboarding_later
                ),
                color = palette.textMuted,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onFinished)
                    .padding(vertical = 8.dp)
            )
        }
    }

    if (searching) {
        AddPlaceDialog(
            placeViewModel = placeViewModel,
            onDismissRequest = { searching = false },
            onPlaceChosen = onFinished
        )
    }
}
