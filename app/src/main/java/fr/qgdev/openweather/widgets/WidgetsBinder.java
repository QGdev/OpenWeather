
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

package fr.qgdev.openweather.widgets;

import static fr.qgdev.openweather.repositories.FormattingService.FormattingSpec.NO_UNIT_NO_SPACE;

import android.content.Context;
import android.util.SizeF;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.metrics.CurrentWeather;
import fr.qgdev.openweather.metrics.DailyWeatherForecast;
import fr.qgdev.openweather.metrics.HourlyWeatherForecast;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * WidgetsBinder
 * <p>
 * 	Used to bind data to a widget layout.
 * 	The binding will be done according to the widget type.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
public class WidgetsBinder {
	
	/**
	 * Bind data to a widget layout and return it
	 * The binding will be done according to the widget type
	 *
	 * @param context           the context of the application
	 * @param widgetType        the widget type to bind
	 * @param place             the place to bind to the widget
	 * @param formattingService the formatting service to format the data
	 * @return RemoteViews      the widget remote view with the data
	 */
	public static RemoteViews bindWidget(@NonNull Context context, @NonNull WidgetType widgetType, @NonNull Place place, @NonNull FormattingService formattingService) {
		RemoteViews views;
		
		switch (widgetType) {
			case STANDARD:
				views = bindStandardWidget(context, place, formattingService, true);
				break;
			case STANDARD_COMPACT:
				views = bindStandardWidget(context, place, formattingService, false);
				break;
			case MINIMAL:
			default:
				views = bindMinimalWidget(context, place, formattingService);
				break;
		}
		return views;
	}
	
	/**
	 * Bind data to a standard widget layout and return it
	 *
	 * @param context           the context of the application
	 * @param place             the place to bind to the widget
	 * @param formattingService the formatting service to format the data
	 * @return RemoteViews      the widget remote view with the data
	 */
	protected static RemoteViews bindStandardWidget(@NonNull Context context, @NonNull Place place, @NonNull FormattingService formattingService, boolean hasTheFourthHour) {
		// Construct the RemoteViews object
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_standard);
		
		// Fill the widget with the place data
		view.setTextViewText(R.id.city, place.getGeolocation().getCity());
		
		CurrentWeather currentWeather = place.getCurrentWeather();
		DailyWeatherForecast currentDayWeather = place.getDailyWeatherForecast(0);
		
		view.setTextViewText(R.id.temperature_value,
				  formattingService.getFloatFormattedTemperature(currentWeather.getTemperature(), NO_UNIT_NO_SPACE));
		view.setTextViewText(R.id.temperature_max_value,
				  formattingService.getIntFormattedTemperature(currentDayWeather.getTemperatureMaximum(), NO_UNIT_NO_SPACE));
		view.setTextViewText(R.id.temperature_min_value,
				  formattingService.getIntFormattedTemperature(currentDayWeather.getTemperatureMinimum(), NO_UNIT_NO_SPACE));
		
		view.setImageViewResource(R.id.weather_icon, getWeatherIcon(currentWeather.getWeatherCode(), currentWeather.isDaytime()));
		
		// Set the first letter to capital
		String weatherDescription = currentWeather.getWeatherDescription();
		weatherDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
		view.setTextViewText(R.id.weather_description, weatherDescription);
		
		// Search the next three hours until now to find the next three hours of forecast
		int nextThreeHoursIndex = 1;
		HourlyWeatherForecast[] hourlyWeatherForecasts = new HourlyWeatherForecast[4];
		boolean[] isDaytime = {false, false, false, false};
		
		Calendar tmpCalendar = Calendar.getInstance();
		tmpCalendar.setTimeInMillis(currentWeather.getSunriseDt());
		int sunRiseHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
		tmpCalendar.setTimeInMillis(currentWeather.getSunsetDt());
		int sunSetHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
		
		for (int i = 0; i < 4; i++) {
			HourlyWeatherForecast hourlyWeatherForecast = place.getHourlyWeatherForecast(nextThreeHoursIndex++);
			if (hourlyWeatherForecast == null)
				throw new NullPointerException("Hourly weather forecast is null");
			
			hourlyWeatherForecasts[i] = hourlyWeatherForecast;
			
			// Check if the current hour is daytime
			tmpCalendar.setTimeInMillis(hourlyWeatherForecast.getDt());
			int hour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
			isDaytime[i] = hour >= sunRiseHour && hour <= sunSetHour;
		}
		
		// First hour
		view.setTextViewText(R.id.forecast_1h_temperature_value,
				  formattingService.getIntFormattedTemperature(hourlyWeatherForecasts[0].getTemperature(), NO_UNIT_NO_SPACE));
		view.setImageViewResource(R.id.forecast_1h_weather_icon,
				  getWeatherIcon(hourlyWeatherForecasts[0].getWeatherCode(), isDaytime[0]));
		view.setTextViewText(R.id.forecast_1h_time,
				  formattingService.getFormattedShortHour(new Date(hourlyWeatherForecasts[0].getDt()),
							 place.getProperties().getTimeZone()));
		
		// Second hour
		view.setTextViewText(R.id.forecast_2h_temperature_value,
				  formattingService.getIntFormattedTemperature(hourlyWeatherForecasts[1].getTemperature(), NO_UNIT_NO_SPACE));
		view.setImageViewResource(R.id.forecast_2h_weather_icon,
				  getWeatherIcon(hourlyWeatherForecasts[1].getWeatherCode(), isDaytime[1]));
		view.setTextViewText(R.id.forecast_2h_time,
				  formattingService.getFormattedShortHour(new Date(hourlyWeatherForecasts[1].getDt()),
							 place.getProperties().getTimeZone()));
		
		// Third hour
		view.setTextViewText(R.id.forecast_3h_temperature_value,
				  formattingService.getIntFormattedTemperature(hourlyWeatherForecasts[2].getTemperature(), NO_UNIT_NO_SPACE));
		view.setImageViewResource(R.id.forecast_3h_weather_icon,
				  getWeatherIcon(hourlyWeatherForecasts[2].getWeatherCode(), isDaytime[2]));
		view.setTextViewText(R.id.forecast_3h_time,
				  formattingService.getFormattedShortHour(new Date(hourlyWeatherForecasts[2].getDt()),
							 place.getProperties().getTimeZone()));
		
		// Fourth hour
		// Can be hidden if the widget is too small
		if (!hasTheFourthHour) {
			view.setViewVisibility(R.id.forecast_4h, View.GONE);
			return view;
		}
		view.setTextViewText(R.id.forecast_4h_temperature_value,
				  formattingService.getIntFormattedTemperature(hourlyWeatherForecasts[3].getTemperature(), NO_UNIT_NO_SPACE));
		view.setImageViewResource(R.id.forecast_4h_weather_icon,
				  getWeatherIcon(hourlyWeatherForecasts[3].getWeatherCode(), isDaytime[3]));
		view.setTextViewText(R.id.forecast_4h_time,
				  formattingService.getFormattedShortHour(new Date(hourlyWeatherForecasts[3].getDt()),
							 place.getProperties().getTimeZone()));
		
		return view;
	}
	
	/**
	 * Binds the minimal widget with the given place.
	 *
	 * @param context           the context
	 * @param place             the place
	 * @param formattingService the formatting service
	 * @return RemoteViews       The widget remote view with the data
	 */
	protected static RemoteViews bindMinimalWidget(@NonNull Context context, @NonNull Place place, @NonNull FormattingService formattingService) {
		// Construct the RemoteViews object
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_minimal_linear);
		
		// Fill the widget with the place data
		CurrentWeather currentWeather = place.getCurrentWeather();
		
		view.setTextViewText(R.id.temperature_value,
				  formattingService.getFloatFormattedTemperature(currentWeather.getTemperature(), NO_UNIT_NO_SPACE));
		
		view.setImageViewResource(R.id.weather_icon, getWeatherIcon(currentWeather.getWeatherCode(), currentWeather.isDaytime()));
		
		return view;
	}
	
	/**
	 * Returns the weather icon id for the given weather code and if it is daytime or not.
	 *
	 * @param weatherCode the weather code
	 * @param isDaytime   true if it is daytime, false otherwise
	 * @return int        the weather icon id
	 */
	private static int getWeatherIcon(int weatherCode, boolean isDaytime) {
		final int weatherIconId;
		
		switch (weatherCode) {
			
			//  Thunderstorm Group
			case 210:
			case 211:
			case 212:
			case 221:
				weatherIconId = R.drawable.thunderstorm_flat;
				break;
			
			//  Drizzle and Rain (Light)
			case 300:
			case 310:
			case 500:
			case 501:
			case 520:
				if (isDaytime) {
					weatherIconId = R.drawable.rain_and_sun_flat;
				}
				//  Night
				else {
					weatherIconId = R.drawable.rainy_night_flat;
				}
				break;
			
			//Drizzle and Rain (Moderate)
			case 301:
			case 302:
			case 311:
			case 313:
			case 321:
			case 511:
			case 521:
			case 531:
				weatherIconId = R.drawable.rain_flat;
				break;
			
			//Drizzle and Rain (Heavy)
			case 312:
			case 314:
			case 502:
			case 503:
			case 504:
			case 522:
				weatherIconId = R.drawable.heavy_rain_flat;
				break;
			
			//  Snow
			case 600:
			case 601:
			case 620:
			case 621:
				if (isDaytime) {
					weatherIconId = R.drawable.snow_flat;
				}
				//  Night
				else {
					weatherIconId = R.drawable.snow_and_night_flat;
				}
				break;
			
			case 602:
			case 622:
				weatherIconId = R.drawable.snow_flat;
				break;
			
			case 611:
			case 612:
			case 613:
			case 615:
			case 616:
				weatherIconId = R.drawable.sleet_flat;
				break;
			
			//  Atmosphere
			case 701:
			case 711:
			case 721:
			case 731:
			case 741:
			case 751:
			case 761:
			case 762:
			case 771:
			case 781:
				if (isDaytime) {
					weatherIconId = R.drawable.fog_flat;
				}
				//  Night
				else {
					weatherIconId = R.drawable.fog_and_night_flat;
				}
				break;
			
			//  Sky
			case 800:
				//  Day
				if (isDaytime) {
					weatherIconId = R.drawable.sun_flat;
				}
				//  Night
				else {
					weatherIconId = R.drawable.moon_phase_flat;
				}
				break;
			
			case 801:
			case 802:
			case 803:
				if (isDaytime) {
					weatherIconId = R.drawable.clouds_and_sun_flat;
				}
				//  Night
				else {
					weatherIconId = R.drawable.cloudy_night_flat;
				}
				break;
			
			case 804:
				weatherIconId = R.drawable.cloudy_flat;
				break;
			
			//  Default
			//	Thunderstorm and rain
			default:
			case 200:
			case 201:
			case 202:
			case 230:
			case 231:
			case 232:
				weatherIconId = R.drawable.storm_flat;
				break;
		}
		
		return weatherIconId;
	}
	
	/**
	 * Will list all the widgets types available with their layout and size
	 * This is needed in order to find the best widget type that can fit in the given size
	 */
	protected enum WidgetType {
		STANDARD("STANDARD", R.layout.widget_standard, 320, 180),
		STANDARD_COMPACT("STANDARD_COMPACT", R.layout.widget_standard, 280, 180),
		
		MINIMAL("MINIMAL", R.layout.widget_minimal_linear, 190, 80);
		
		private final String id;
		private final int layout;
		private final int width;
		private final int height;
		
		/**
		 * Create a new widget type
		 *
		 * @param id     Widget type id
		 * @param layout Widget type layout as a layout resource id
		 * @param width  Widget type width in dp
		 * @param height Widget type height in dp
		 */
		WidgetType(@NonNull String id, @LayoutRes int layout, int width, int height) {
			if (id.isBlank() || id.isEmpty())
				throw new IllegalArgumentException("Id must not be blank or empty");
			if (width < 0) throw new IllegalArgumentException("Width must be positive");
			if (height < 0) throw new IllegalArgumentException("Height must be positive");
			
			this.id = id;
			this.layout = layout;
			this.width = width;
			this.height = height;
		}
		
		/**
		 * Will return the largest widget type that can fit in the given SizeF
		 *
		 * @param size SizeF to check
		 * @return WidgetType   Largest widget type that can fit in the given SizeF
		 */
		public static WidgetType fromSizeF(SizeF size) {
			//	Get the the largest widget type that can fit in the given size
			for (WidgetType type : WidgetType.values()) {
				if (type.width <= size.getWidth() && type.height <= size.getHeight()) {
					return type;
				}
			}
			return null;
		}
		
		/**
		 * Will return the widget type that match the given id
		 *
		 * @param id Id to check
		 * @return WidgetType   Widget type that match the given id or null if none match
		 */
		public static WidgetType fromString(String id) {
			for (WidgetType type : WidgetType.values()) {
				if (type.id.equalsIgnoreCase(id)) {
					return type;
				}
			}
			return null;
		}
		
		private static float dpToPx(Context context, float dp) {
			return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
		}
		
		/**
		 * Get the widget type id
		 *
		 * @return String   Widget type id
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * Get the widget type layout
		 *
		 * @return int   Widget type layout
		 */
		public int getLayout() {
			return layout;
		}
		
		/**
		 * Get the widget type width
		 *
		 * @return int   Widget type width
		 */
		public int getWidth() {
			return width;
		}
		
		/**
		 * Get the widget type height
		 *
		 * @return int   Widget type height
		 */
		public int getHeight() {
			return height;
		}
		
		/**
		 * Get the widget type width in pixels
		 *
		 * @param context Android context
		 * @return int   Widget type width in pixels
		 */
		public int getPxWidth(Context context) {
			return (int) dpToPx(context, width);
		}
		
		/**
		 * Get the widget type height in pixels
		 *
		 * @param context Android context
		 * @return int   Widget type height in pixels
		 */
		public int getPxHeight(Context context) {
			return (int) dpToPx(context, height);
		}
		
		/**
		 * Get the widget type SizeF (width and height)
		 *
		 * @return SizeF   Widget type SizeF
		 */
		public SizeF getSizeF() {
			return new SizeF(width, height);
		}
		
		/**
		 * Convert the widget type to a string
		 *
		 * @return String   Widget type as a string
		 */
		@Override
		public String toString() {
			return id;
		}
	}
}
