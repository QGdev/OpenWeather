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

package fr.qgdev.openweather.splash

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import fr.qgdev.openweather.MainActivity
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.repositories.SettingsRepository
import fr.qgdev.openweather.ui.format.FormattingService
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SplashScreenActivity
 *
 * Shown for at least [SPLASH_TIMEOUT], the time it takes to read the stored data, before the main
 * activity. It carries the app's identity and the credits the data sources require, so they are
 * seen without crowding About.
 *
 * No weather image: choosing a condition before the data is read would mean picking one at random.
 *
 * @author Quentin GOMES DOS REIS
 * @version 2
 * @see AppCompatActivity
 */
class SplashScreenActivity : AppCompatActivity() {

    /** Loading steps finished, out of [LOADING_STEPS]. */
    private var stepsDone by mutableIntStateOf(0)

    /** Share of [SPLASH_TIMEOUT] elapsed. */
    private var timeElapsed by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        //  Status bar icons matched to the theme, as in MainActivity.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                //  The bar follows the clock, but never gets ahead of the loading: if reading the
                //  data takes longer than a second, it stops where the loading actually is instead
                //  of looping as if something were happening.
                SplashScreen(progress = minOf(timeElapsed, stepsDone / LOADING_STEPS.toFloat()))
            }
        }

        lifecycleScope.launch {
            // Warm up the singletons and the DataStore cache during the splash delay
            val preloadJob = launch(Dispatchers.IO) {
                SettingsRepository.getInstance(applicationContext)
                withContext(Dispatchers.Main) { stepsDone++ }
                FormattingService.getInstance(applicationContext)
                withContext(Dispatchers.Main) { stepsDone++ }
                PlaceRepository.getInstance(applicationContext).placesFlow.first()
                withContext(Dispatchers.Main) { stepsDone++ }
            }
            val start = SystemClock.elapsedRealtime()
            while (isActive && timeElapsed < 1f) {
                delay(FRAME_MILLIS)
                timeElapsed = ((SystemClock.elapsedRealtime() - start) / SPLASH_TIMEOUT.toFloat()).coerceAtMost(1f)
            }
            preloadJob.join() // waits when loading takes longer than 1 s (rare)
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val SPLASH_TIMEOUT = 1000L
        private const val LOADING_STEPS = 3
        private const val FRAME_MILLIS = 16L
    }
}

@Composable
private fun SplashScreen(progress: Float) {
    val palette = LocalWeatherPalette.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                //  A night sky lifting towards the top, drawn from the palette so a light theme
                //  gets the same ground in its own tones.
                Brush.verticalGradient(
                    0f to lerp(palette.screen, palette.accent, 0.14f),
                    0.46f to lerp(palette.screen, palette.accent, 0.06f),
                    1f to palette.screen
                )
            )
    ) {
        //  Two glows off the edges: a cool one where the light comes from, a faint warm one below.
        Box(
            modifier = Modifier
                .offset(x = (-70).dp, y = (-60).dp)
                .size(340.dp)
                .background(Brush.radialGradient(listOf(palette.accent.copy(alpha = 0.22f), palette.accent.copy(alpha = 0f))))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = (-120).dp)
                .size(300.dp)
                .background(Brush.radialGradient(listOf(palette.amber.copy(alpha = 0.10f), palette.amber.copy(alpha = 0f))))
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.size(108.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = palette.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.splash_tagline),
                    color = palette.textQuiet,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(132.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        Text(
            text = stringResource(R.string.splash_attribution),
            color = palette.textQuiet,
            fontSize = 10.5.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, bottom = 34.dp)
        )
    }
}
