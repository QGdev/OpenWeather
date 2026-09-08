
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

import fr.qgdev.openweather.ui.theme.readableOn
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.compose.ui.graphics.toArgb
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.theme.DarkWeatherPalette
import fr.qgdev.openweather.ui.theme.LightWeatherPalette
import fr.qgdev.openweather.ui.theme.Sky
import fr.qgdev.openweather.ui.theme.WeatherPalette
import fr.qgdev.openweather.ui.theme.conditionSky
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.isDaytime
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

/**
 * WidgetsBinder
 *
 * Binds a place to a widget layout, according to the widget type.
 *
 * The widgets carry the same condition sky as the app's cards, graded by cloud cover: posed on any
 * wallpaper, the sky keeps the text readable without an opaque plate. RemoteViews cannot draw a
 * gradient from code, so the sky is painted into a small bitmap that the layout stretches.
 *
 * @author Quentin GOMES DOS REIS
 * @version 2
 */
object WidgetsBinder {

    /** Values older than this are labelled with their time, as on the cards. */
    private const val STALE_AFTER_MILLIS = 2 * 60 * 60 * 1000L

    //  Widths from which the one-row widget adds each block. The place comes at about three launcher
    //  columns on a phone and the range at four; the hours need more than a phone's five columns
    //  leave once the place is shown, so they come on wider screens and in landscape.
    private const val ROW_PLACE_MIN_WIDTH = 210f
    private const val ROW_RANGE_MIN_WIDTH = 280f
    private const val ROW_HOURS_MIN_WIDTH = 400f

    private val ROW_HOUR_SLOTS = listOf(
        Triple(R.id.row_hour_1_time, R.id.row_hour_1_icon, R.id.row_hour_1_temperature),
        Triple(R.id.row_hour_2_time, R.id.row_hour_2_icon, R.id.row_hour_2_temperature),
        Triple(R.id.row_hour_3_time, R.id.row_hour_3_icon, R.id.row_hour_3_temperature)
    )

    private val HOUR_SLOTS = listOf(
        Triple(R.id.hour_1_time, R.id.hour_1_icon, R.id.hour_1_temperature) to R.id.hour_1,
        Triple(R.id.hour_2_time, R.id.hour_2_icon, R.id.hour_2_temperature) to R.id.hour_2,
        Triple(R.id.hour_3_time, R.id.hour_3_icon, R.id.hour_3_temperature) to R.id.hour_3,
        Triple(R.id.hour_4_time, R.id.hour_4_icon, R.id.hour_4_temperature) to R.id.hour_4,
        Triple(R.id.hour_5_time, R.id.hour_5_icon, R.id.hour_5_temperature) to R.id.hour_5
    )

    /**
     * Bind data to a widget layout and return it.
     *
     * @param context           the context of the application
     * @param widgetType        the widget type to bind
     * @param place             the place to bind to the widget
     * @param formattingService the formatting service to format the data
     * @param backgroundTransparency how transparent the sky is, from 0 (opaque) to 100
     * @param widthDp           the width the widget is shown at, which decides what a one-row
     *                          widget has room for
     * @param showDetails       whether a one-row widget may show more than the temperature when
     *                          it has the room
     * @return the widget remote view with the data
     */
    @JvmStatic
    @JvmOverloads
    fun bindWidget(
        context: Context,
        widgetType: WidgetType,
        place: Place,
        formattingService: FormattingService,
        backgroundTransparency: Int = 0,
        widthDp: Float = widgetType.width.toFloat(),
        showDetails: Boolean = true
    ): RemoteViews {
        val isDark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        val basePalette = if (isDark) DarkWeatherPalette else LightWeatherPalette
        val current = place.currentWeather
        val sky = conditionSky(current.weatherCode, current.isDaytime(), current.cloudiness, isDark)
        //  The secondary texts and labels are tuned for an opaque sky, and some already fell short
        //  of a readable contrast there. They are adjusted to the ground actually under them: the
        //  sky, and the wallpaper showing through it as it turns transparent.
        val ground = widgetGround(sky, basePalette.textPrimary, dominantWallpaperColor(context), backgroundTransparency)
        val palette = basePalette.readableOn(ground)

        val views = RemoteViews(context.packageName, widgetType.layout)
        views.setImageViewBitmap(R.id.sky, skyBitmap(sky, widgetType))
        views.setInt(R.id.sky, "setImageAlpha", 255 * (100 - backgroundTransparency.coerceIn(0, 100)) / 100)

        val lastUpdate = place.properties.lastSuccessfulWeatherUpdateTime
        val isStale = lastUpdate > 0L && System.currentTimeMillis() - lastUpdate > STALE_AFTER_MILLIS
        views.setImageViewResource(R.id.frame, if (isStale) R.drawable.widget_sky_frame_stale else R.drawable.widget_sky_frame)
        val staleLabel = if (isStale) {
            context.getString(
                R.string.widget_values_from,
                formattingService.getFormattedTime(Date(lastUpdate), java.util.TimeZone.getDefault())
            )
        } else null

        when (widgetType) {
            WidgetType.STANDARD, WidgetType.STANDARD_COMPACT ->
                bindStandard(context, views, widgetType == WidgetType.STANDARD, place, formattingService, palette, staleLabel)

            WidgetType.MINIMAL -> bindMinimal(
                views, place, formattingService, palette, staleLabel,
                //  Details off, the row keeps to the icon and the temperature whatever its width.
                if (showDetails) widthDp else 0f
            )
        }
        return views
    }

    private fun bindStandard(
        context: Context,
        views: RemoteViews,
        large: Boolean,
        place: Place,
        formattingService: FormattingService,
        palette: WeatherPalette,
        staleLabel: String?
    ) {
        val current = place.currentWeather
        val today = place.getDailyForecastList(0)
        val timeZone = SimpleTimeZone(place.properties.timeOffset, "")

        views.setTextViewText(R.id.city, place.geolocation.city)
        views.setTextColor(R.id.city, palette.textPrimary.toArgb())

        //  A stale widget says so where the condition would be: it is the line read right after the
        //  city, and the condition is the part of the reading least worth trusting when old.
        if (staleLabel != null) {
            views.setTextViewText(R.id.condition, staleLabel)
            views.setTextColor(R.id.condition, palette.orange.toArgb())
        } else {
            views.setTextViewText(R.id.condition, current.weatherDescription.replaceFirstChar { it.titlecase(Locale.getDefault()) })
            views.setTextColor(R.id.condition, palette.textSecondary.toArgb())
        }

        views.setTextViewText(
            R.id.temperature,
            temperatureWithSmallDecimals(
                formattingService.getFloatFormattedTemperature(current.temperature, FormattingSpec.NO_UNIT_NO_SPACE),
                if (staleLabel != null) palette.textMuted else palette.textPrimary,
                palette
            )
        )
        views.setTextColor(R.id.temperature, (if (staleLabel != null) palette.textMuted else palette.textPrimary).toArgb())
        views.setTextViewText(R.id.temperature_max, formattingService.getFloatFormattedTemperature(today.temperatureMaximum, FormattingSpec.NO_UNIT_NO_SPACE))
        views.setTextColor(R.id.temperature_max, palette.textPrimary.toArgb())
        views.setTextViewText(R.id.temperature_min, formattingService.getFloatFormattedTemperature(today.temperatureMinimum, FormattingSpec.NO_UNIT_NO_SPACE))
        views.setTextColor(R.id.temperature_min, palette.textSecondary.toArgb())
        views.setInt(R.id.temperature_max_icon, "setColorFilter", palette.textPrimary.toArgb())
        views.setInt(R.id.temperature_min_icon, "setColorFilter", palette.textSecondary.toArgb())

        //  The three named measures of the previous widget, and the wind, on the large one only.
        val metricsVisibility = if (large) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.metrics, metricsVisibility)
        views.setViewVisibility(R.id.metrics_divider, metricsVisibility)
        if (large) {
            bindMetric(
                views, R.id.metric_1_label, R.id.metric_1_value, palette,
                context.getString(R.string.title_pressure),
                formattingService.getFormattedPressure(current.pressure.toFloat(), FormattingSpec.NO_UNIT_NO_SPACE),
                " " + formattingService.pressureUnitLabel
            )
            bindMetric(
                views, R.id.metric_2_label, R.id.metric_2_value, palette,
                context.getString(R.string.title_temperature_feelslike),
                formattingService.getFloatFormattedTemperature(current.temperatureFeelsLike, FormattingSpec.NO_UNIT_NO_SPACE).removeSuffix("°"),
                "°"
            )
            bindMetric(
                views, R.id.metric_3_label, R.id.metric_3_value, palette,
                context.getString(R.string.title_humidity),
                formattingService.getIntFormattedPercentage(current.humidity, FormattingSpec.NO_UNIT_NO_SPACE),
                " %"
            )
            //  The wind as the cards print it: the speed, then its direction in the unit's place.
            bindMetric(
                views, R.id.metric_4_label, R.id.metric_4_value, palette,
                context.getString(R.string.label_wind),
                formattingService.getFloatFormattedSpeed(current.windSpeed, FormattingSpec.NO_UNIT_NO_SPACE),
                " " + formattingService.getFormattedDirection(current.windDirection, current.isWindDirectionReadable)
            )
        }
        views.setInt(R.id.metrics_divider, "setBackgroundColor", palette.outline.toArgb())
        views.setInt(R.id.hours_divider, "setBackgroundColor", palette.outline.toArgb())

        //  Five hours on the large widget: 320 dp hold five columns, and five hours cross midnight
        //  often enough to be worth it. Three on the medium one.
        val hourCount = if (large) 5 else 3
        HOUR_SLOTS.forEachIndexed { index, (ids, container) ->
            val hour = place.hourlyForecastListList.getOrNull(index + 1)
            if (index >= hourCount || hour == null) {
                views.setViewVisibility(container, View.GONE)
                return@forEachIndexed
            }
            views.setViewVisibility(container, View.VISIBLE)
            val (timeId, iconId, temperatureId) = ids
            views.setTextViewText(timeId, formattingService.getFormattedShortHour(Date(hour.dt), timeZone))
            views.setTextColor(timeId, palette.textQuiet.toArgb())
            views.setImageViewResource(iconId, getDrawableResIdFromWeatherCode(hour.weatherCode, place.isDaytimeAt(hour)))
            views.setTextViewText(temperatureId, formattingService.getIntFormattedTemperature(hour.temperature, FormattingSpec.NO_UNIT_NO_SPACE))
            views.setTextColor(temperatureId, palette.textPrimary.toArgb())
        }
    }

    private fun bindMinimal(
        views: RemoteViews,
        place: Place,
        formattingService: FormattingService,
        palette: WeatherPalette,
        staleLabel: String?,
        widthDp: Float
    ) {
        val current = place.currentWeather
        val color = if (staleLabel != null) palette.textMuted else palette.textPrimary

        views.setImageViewResource(R.id.weather_icon, getDrawableResIdFromWeatherCode(current.weatherCode, current.isDaytime()))
        views.setTextViewText(
            R.id.temperature,
            temperatureWithSmallDecimals(
                formattingService.getFloatFormattedTemperature(current.temperature, FormattingSpec.NO_UNIT_NO_SPACE),
                color,
                palette
            )
        )
        views.setTextColor(R.id.temperature, color.toArgb())
        views.setViewVisibility(R.id.stale, if (staleLabel != null) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.stale, staleLabel.orEmpty())
        views.setTextColor(R.id.stale, palette.orange.toArgb())

        //  What the row has room for, added in order of use: where it is, how the day goes, then
        //  what comes next.
        val showPlace = widthDp >= ROW_PLACE_MIN_WIDTH
        val showRange = widthDp >= ROW_RANGE_MIN_WIDTH
        val showHours = widthDp >= ROW_HOURS_MIN_WIDTH
        views.setViewVisibility(R.id.row_place, if (showPlace) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.row_range, if (showRange) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.row_hours, if (showHours) View.VISIBLE else View.GONE)

        if (showPlace) {
            views.setTextViewText(R.id.city, place.geolocation.city)
            views.setTextColor(R.id.city, palette.textPrimary.toArgb())
            views.setTextViewText(R.id.condition, current.weatherDescription.replaceFirstChar { it.titlecase(Locale.getDefault()) })
            views.setTextColor(R.id.condition, palette.textSecondary.toArgb())
        }
        if (showRange) {
            val today = place.getDailyForecastList(0)
            views.setTextViewText(R.id.temperature_max, formattingService.getFloatFormattedTemperature(today.temperatureMaximum, FormattingSpec.NO_UNIT_NO_SPACE))
            views.setTextColor(R.id.temperature_max, palette.textPrimary.toArgb())
            views.setTextViewText(R.id.temperature_min, formattingService.getFloatFormattedTemperature(today.temperatureMinimum, FormattingSpec.NO_UNIT_NO_SPACE))
            views.setTextColor(R.id.temperature_min, palette.textSecondary.toArgb())
            views.setInt(R.id.temperature_max_icon, "setColorFilter", palette.textPrimary.toArgb())
            views.setInt(R.id.temperature_min_icon, "setColorFilter", palette.textSecondary.toArgb())
        }
        if (showHours) {
            val timeZone = SimpleTimeZone(place.properties.timeOffset, "")
            ROW_HOUR_SLOTS.forEachIndexed { index, (timeId, iconId, temperatureId) ->
                val hour = place.hourlyForecastListList.getOrNull(index + 1) ?: return@forEachIndexed
                views.setTextViewText(timeId, formattingService.getFormattedShortHour(Date(hour.dt), timeZone))
                views.setTextColor(timeId, palette.textQuiet.toArgb())
                views.setImageViewResource(iconId, getDrawableResIdFromWeatherCode(hour.weatherCode, place.isDaytimeAt(hour)))
                views.setTextViewText(temperatureId, formattingService.getIntFormattedTemperature(hour.temperature, FormattingSpec.NO_UNIT_NO_SPACE))
                views.setTextColor(temperatureId, palette.textPrimary.toArgb())
            }
        }
    }

    private fun bindMetric(
        views: RemoteViews,
        labelId: Int,
        valueId: Int,
        palette: WeatherPalette,
        label: String,
        value: String,
        unit: String
    ) {
        views.setTextViewText(labelId, label)
        views.setTextColor(labelId, palette.textQuiet.toArgb())
        val text = SpannableString(value + unit)
        text.setSpan(RelativeSizeSpan(10f / 14f), value.length, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(ForegroundColorSpan(palette.textSecondary.toArgb()), value.length, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        views.setTextViewText(valueId, text)
        views.setTextColor(valueId, palette.textPrimary.toArgb())
    }

    /**
     * "13,9°" with the decimals and the degree sign at half size, as the cards print it: the whole
     * degrees are what is read from across the room.
     */
    private fun temperatureWithSmallDecimals(
        formatted: String,
        color: androidx.compose.ui.graphics.Color,
        palette: WeatherPalette
    ): CharSequence {
        val separator = formatted.indexOfFirst { it == ',' || it == '.' }
        if (separator < 0) return formatted
        val text = SpannableString(formatted)
        text.setSpan(RelativeSizeSpan(0.5f), separator, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(ForegroundColorSpan(color.toArgb()), 0, separator, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val degree = formatted.indexOf('°')
        if (degree >= 0) {
            text.setSpan(ForegroundColorSpan(palette.textSecondary.toArgb()), degree, degree + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return text
    }

    /** Whether an hour falls between the sunrise and sunset of its own day. */
    private fun Place.isDaytimeAt(hour: HourlyForecast): Boolean =
        dailyForecastListList.any { hour.dt in it.sunriseDt..it.sunsetDt }

    /**
     * The sky, painted small: a diagonal wash from the top-left corner and the condition's glow off
     * the top-right one. The layout stretches it to the widget, which a gradient survives.
     */
    private fun skyBitmap(sky: Sky, widgetType: WidgetType): Bitmap {
        val width = 96
        val height = (width * widgetType.height / widgetType.width).coerceAtLeast(24)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(sky.start.toArgb(), sky.middle.toArgb(), sky.end.toArgb()),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.shader = RadialGradient(
            width * 0.9f, height * -0.1f, width * 0.45f,
            sky.halo.toArgb(), android.graphics.Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    /**
     * Will list all the widgets types available with their layout and size.
     * This is needed in order to find the best widget type that can fit in the given size.
     */
    enum class WidgetType(
        val id: String,
        @LayoutRes val layout: Int,
        /** Width in dp. */
        val width: Int,
        /** Height in dp. */
        val height: Int
    ) {
        //  The smallest size each layout's content fits in, not the mock-up's sizes: launcher cells
        //  come in steps, and a four-column widget on a phone is about 296 dp wide. At 320 dp the
        //  large layout was out of reach, and a two-row widget showed the medium one with half its
        //  height empty.
        STANDARD("STANDARD", R.layout.widget_standard, 290, 180),
        STANDARD_COMPACT("STANDARD_COMPACT", R.layout.widget_standard, 210, 125),
        MINIMAL("MINIMAL", R.layout.widget_minimal_linear, 140, 50);

        /** Widget type size in dp. */
        val sizeF: SizeF get() = SizeF(width.toFloat(), height.toFloat())

        fun getPxWidth(context: Context): Int = dpToPx(context, width.toFloat()).toInt()

        fun getPxHeight(context: Context): Int = dpToPx(context, height.toFloat()).toInt()

        override fun toString(): String = id

        companion object {
            /** The largest widget type that fits in the given size, or null when none does. */
            @JvmStatic
            fun fromSizeF(size: SizeF): WidgetType? =
                entries.firstOrNull { it.width <= size.width && it.height <= size.height }

            /** The widget type matching the given id, or null when none does. */
            @JvmStatic
            fun fromString(id: String?): WidgetType? =
                entries.firstOrNull { it.id.equals(id, ignoreCase = true) }

            private fun dpToPx(context: Context, dp: Float): Float =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }
    }
}
