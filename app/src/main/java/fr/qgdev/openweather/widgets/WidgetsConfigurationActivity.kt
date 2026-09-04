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
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SizeF
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.WeatherPalette
import fr.qgdev.openweather.ui.theme.conditionSky
import fr.qgdev.openweather.ui.utils.countryNameFromCode
import fr.qgdev.openweather.ui.utils.isDaytime
import fr.qgdev.openweather.ui.viewmodel.identityKey
import fr.qgdev.openweather.widgets.WidgetsBinder.WidgetType
import kotlinx.coroutines.launch

/**
 * WidgetsConfigurationActivity
 *
 * Configures a widget: which place it shows and how transparent its sky is, with a live preview.
 *
 * Controls take the system accent through MaterialTheme; the weather palette is left to the sky and
 * to the state colours, which carry meaning.
 *
 * The window shows the wallpaper (see the WidgetConfig theme), which is how the preview sits on the
 * real wallpaper without the app ever reading it. Everything else is painted opaque, system bars
 * included: the screen's background is drawn with a hole where the preview is, so the wallpaper
 * appears there and only there.
 *
 * @author Quentin GOMES DOS REIS
 * @version 3
 * @see ComponentActivity
 */
class WidgetsConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED. This will cause the widget host to cancel out of the widget
        // placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        appWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // The preview starts at the size the widget currently has; the other sizes can be tried.
        val options = AppWidgetManager.getInstance(this).getAppWidgetOptions(appWidgetId)
        val currentType = WidgetType.fromSizeF(
            SizeF(
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).toFloat(),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).toFloat()
            )
        ) ?: WidgetType.MINIMAL
        val saved = WidgetsManager.getInstance(this).loadWidgetSettings(appWidgetId, null)

        lifecycleScope.launch {
            val places = PlaceRepository.getInstance(applicationContext).getPlaces()
            setContent {
                AppTheme {
                    WidgetConfigurationScreen(
                        places = places,
                        initialType = currentType,
                        initialPlaceKey = saved?.placeId,
                        initialTransparency = saved?.backgroundTransparency ?: 0,
                        initialShowDetails = saved?.showDetails ?: true,
                        onClose = { finish() },
                        onConfirm = { place, transparency, showDetails -> confirm(place, transparency, showDetails) }
                    )
                }
            }
        }
    }

    private fun confirm(place: Place, transparency: Int, showDetails: Boolean) {
        WidgetsManager.getInstance(this)
            .saveWidgetSettings(WidgetsSettings(place.identityKey, appWidgetId, transparency, showDetails))

        lifecycleScope.launch {
            // It is the responsibility of the configuration activity to update the app widget.
            WidgetsProvider.updateAppWidget(
                applicationContext,
                AppWidgetManager.getInstance(applicationContext),
                appWidgetId
            )

            // Make sure we pass back the original appWidgetId.
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }
}

/** Height taken in the preview card by its padding and caption, in dp. */
private const val PREVIEW_CAPTION_HEIGHT = 36 + 22

/** A size offered in the preview: a layout, shown at a size a launcher typically gives it. */
private class PreviewSize(val type: WidgetType, @param:StringRes val label: Int, val widthDp: Int, val heightDp: Int)

/**
 * The sizes offered in the preview, in the mock-up's order, each at a common phone size - three
 * columns by two rows, four by two, four by one - rather than at the smallest size its layout
 * accepts, which no launcher gives exactly.
 */
private val PREVIEW_SIZES = listOf(
    PreviewSize(WidgetType.STANDARD_COMPACT, R.string.widget_size_medium, 222, 190),
    PreviewSize(WidgetType.STANDARD, R.string.widget_size_large, 296, 190),
    PreviewSize(WidgetType.MINIMAL, R.string.widget_size_small, 296, 92)
)

@Composable
private fun WidgetConfigurationScreen(
    places: List<Place>,
    initialType: WidgetType,
    initialPlaceKey: String?,
    initialTransparency: Int,
    initialShowDetails: Boolean,
    onClose: () -> Unit,
    onConfirm: (Place, Int, Boolean) -> Unit
) {
    val palette = LocalWeatherPalette.current
    val context = LocalContext.current
    val formattingService = remember { FormattingService.getInstance(context) }

    val showable = places.filter { it.canBeShownInWidget() }
    var selectedKey by remember {
        mutableStateOf(
            showable.firstOrNull { it.identityKey == initialPlaceKey }?.identityKey
                ?: showable.firstOrNull()?.identityKey
        )
    }
    val selected = showable.firstOrNull { it.identityKey == selectedKey }
    var previewSize by remember { mutableStateOf(PREVIEW_SIZES.first { it.type == initialType }) }
    var transparency by remember { mutableIntStateOf(initialTransparency) }
    var showDetails by remember { mutableStateOf(initialShowDetails) }
    var hole by remember { mutableStateOf(Rect.Zero) }
    val holeRadius = with(LocalDensity.current) { 22.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            //  The whole screen is painted, bars included, except the preview: that is where the
            //  window's wallpaper shows through.
            .drawBehind {
                val path = Path().apply { addRoundRect(RoundRect(hole, CornerRadius(holeRadius))) }
                clipPath(path, ClipOp.Difference) { drawRect(palette.screen) }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Header(onClose)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { hole = it.boundsInRoot() }
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, palette.outline, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviewCaption()
                //  The same height whatever the size shown, so switching sizes does not make the
                //  preview, and everything under it, jump: half the screen for the whole card, which
                //  leaves room to judge the widget against its wallpaper, and never less than the
                //  tallest widget.
                val tallest = PREVIEW_SIZES.maxOf { it.heightDp }
                val halfScreen = LocalConfiguration.current.screenHeightDp / 2 - PREVIEW_CAPTION_HEIGHT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxOf(tallest, halfScreen).dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected != null) {
                        WidgetPreview(selected, previewSize, transparency, showDetails, formattingService)
                    }
                }
            }

            //  The size chips and the notes sit below the preview, on the opaque screen: laid over
            //  the wallpaper they were at the mercy of whatever image is behind them, and often
            //  unreadable. Only the widget itself is shown on the wallpaper.
            SizeChips(previewSize) { previewSize = it }
            Note(
                text = stringResource(R.string.widget_resize_note),
                accent = palette.textQuiet
            )
            val threshold = rememberReadabilityThreshold(selected, palette)
            if (threshold != null && transparency >= threshold) {
                Note(
                    text = stringResource(R.string.widget_readability_warning, threshold),
                    accent = palette.orange
                )
            }

            TransparencySetting(transparency) { transparency = it }
            DetailsSetting(showDetails) { showDetails = it }

            Text(
                text = stringResource(R.string.widget_choose_city).uppercase(),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.05.sp,
                modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 9.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                places.forEach { place ->
                    PlaceRow(
                        place = place,
                        formattingService = formattingService,
                        selected = place.identityKey == selectedKey,
                        enabled = place.canBeShownInWidget(),
                        onClick = { selectedKey = place.identityKey }
                    )
                }
                if (places.isEmpty()) {
                    Text(
                        text = stringResource(R.string.error_no_places_registered),
                        color = palette.textMuted,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        ConfirmButton(enabled = selected != null) {
            selected?.let { onConfirm(it, transparency, showDetails) }
        }
    }
}

@Composable
private fun Header(onClose: () -> Unit) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .border(1.dp, palette.outlineStrong, RoundedCornerShape(11.dp))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", color = palette.textPrimary, fontSize = 15.sp)
        }
        Text(
            text = stringResource(R.string.widget_configure),
            color = palette.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Text laid on the wallpaper, which can be any colour: white with a shadow reads on all of them. */
private val OnWallpaper = TextStyle(
    color = Color.White.copy(alpha = 0.85f),
    shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 6f)
)

@Composable
private fun PreviewCaption() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 11.dp)) {
        Text(
            text = stringResource(R.string.widget_preview).uppercase(),
            style = OnWallpaper,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.1.sp,
            modifier = Modifier.weight(1f)
        )
        Text(text = stringResource(R.string.widget_preview_on_wallpaper), style = OnWallpaper, fontSize = 9.5.sp)
    }
}

/** The widget itself, built by the same binder the provider uses, at the chosen size. */
@Composable
private fun WidgetPreview(
    place: Place,
    size: PreviewSize,
    transparency: Int,
    showDetails: Boolean,
    formattingService: FormattingService
) {
    val density = LocalDensity.current
    AndroidView(
        factory = { FrameLayout(it) },
        update = { frame ->
            val view = WidgetsBinder.bindWidget(
                frame.context, size.type, place, formattingService,
                transparency, size.widthDp.toFloat(), showDetails
            ).apply(frame.context, frame)
            frame.removeAllViews()
            with(density) { frame.addView(view, size.widthDp.dp.roundToPx(), size.heightDp.dp.roundToPx()) }
        },
        modifier = Modifier.size(width = size.widthDp.dp, height = size.heightDp.dp)
    )
}

@Composable
private fun SizeChips(selected: PreviewSize, onSelect: (PreviewSize) -> Unit) {
    val palette = LocalWeatherPalette.current
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        PREVIEW_SIZES.forEach { size ->
            val isSelected = size == selected
            Text(
                text = stringResource(size.label),
                color = if (isSelected) colorScheme.onPrimary else palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(colorScheme.primary)
                        else Modifier.border(1.dp, palette.outlineStrong, CircleShape)
                    )
                    .clickable { onSelect(size) }
                    .padding(horizontal = 13.dp, vertical = 7.dp)
            )
        }
    }
}

/** A note on the screen's own ground, marked like the banner on the Places screen. */
@Composable
private fun Note(text: String, accent: Color) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.09f))
            .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(text = text, color = palette.textSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
    }
}

/**
 * The transparency from which the widget's text stops being readable on this wallpaper, or null
 * when it stays readable all the way.
 *
 * The wallpaper is never read. Android shares its dominant colour through WallpaperColors, which
 * needs no permission; the sky at a given transparency is mixed towards that colour, and the
 * threshold is the first step where the widget's main text falls below a 4.5:1 contrast - the
 * WCAG level for body text. It is an estimate: the colour under the widget may differ from the
 * dominant one, which is why this warns rather than prevents.
 */
@Composable
private fun rememberReadabilityThreshold(place: Place?, palette: WeatherPalette): Int? {
    val context = LocalContext.current
    return remember(place, palette) {
        val wallpaper = dominantWallpaperColor(context) ?: return@remember null
        val current = place?.currentWeather ?: return@remember null
        val sky = conditionSky(current.weatherCode, current.isDaytime(), current.cloudiness, palette.isDark)
        (0..100 step 5).firstOrNull { step ->
            val ground = lerp(sky.middle, wallpaper, step / 100f)
            contrast(palette.textPrimary, ground) < 4.5f
        }
    }
}

private fun dominantWallpaperColor(context: Context): Color? =
    WallpaperManager.getInstance(context)
        .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        ?.primaryColor
        ?.toArgb()
        ?.let { Color(it) }

private fun contrast(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return maxOf(la, lb) / minOf(la, lb)
}

/** Whether a one-row widget may show more than its temperature when it has the room. */
@Composable
private fun DetailsSetting(checked: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.widget_details), color = palette.textPrimary, fontSize = 13.5.sp)
            Text(
                text = stringResource(R.string.widget_details_summary),
                color = palette.textQuiet,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
private fun TransparencySetting(value: Int, onChange: (Int) -> Unit) {
    val palette = LocalWeatherPalette.current
    Column(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp)
    ) {
        Row {
            Text(
                text = stringResource(R.string.widget_transparency),
                color = palette.textPrimary,
                fontSize = 13.5.sp,
                modifier = Modifier.weight(1f)
            )
            Text(text = "$value %", color = palette.textMuted, fontSize = 12.sp)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = palette.outlineStrong
            )
        )
        //  The bounds are named: the slider alone does not say which way is which, and at 100 %
        //  the widget's sky disappears.
        Row {
            Text(
                text = stringResource(R.string.widget_transparency_opaque),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                modifier = Modifier.weight(1f)
            )
            Text(text = stringResource(R.string.widget_transparency_invisible), color = palette.textQuiet, fontSize = 10.5.sp)
        }
    }
}

@Composable
private fun PlaceRow(
    place: Place,
    formattingService: FormattingService,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (selected) Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape)
                else Modifier.border(1.dp, palette.outline, shape)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val alpha = if (enabled) 1f else 0.5f
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    if (selected) 5.dp else 1.6.dp,
                    if (selected) MaterialTheme.colorScheme.primary else palette.outlineStrong,
                    CircleShape
                )
        )
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            Text(
                text = place.geolocation.city,
                color = palette.textPrimary.copy(alpha = alpha),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            val country = countryNameFromCode(place.geolocation.countryCode)
            if (country.isNotEmpty()) {
                Text(
                    text = " · $country",
                    color = palette.textQuiet.copy(alpha = alpha),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
        //  A place without data is listed but cannot be chosen: a widget on it would show nothing.
        if (enabled) {
            Text(
                text = formattingService.getFloatFormattedTemperature(place.currentWeather.temperature, FormattingSpec.NO_UNIT_NO_SPACE),
                color = if (selected) palette.textPrimary else palette.textMuted,
                fontSize = 13.sp
            )
        } else {
            Text(text = stringResource(R.string.widget_no_data), color = palette.orange, fontSize = 10.5.sp)
        }
    }
}

@Composable
private fun ConfirmButton(enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(if (enabled) MaterialTheme.colorScheme.primary else palette.outlineStrong)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.action_confirm),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
