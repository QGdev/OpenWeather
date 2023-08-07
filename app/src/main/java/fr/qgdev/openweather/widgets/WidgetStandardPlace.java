
/*
 *  Copyright (c) 2019 - 2023
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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.metrics.CurrentWeather;
import fr.qgdev.openweather.metrics.DailyWeatherForecast;
import fr.qgdev.openweather.metrics.HourlyWeatherForecast;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetStandardPlaceConfigureActivity WidgetStandardPlaceInfoConfigureActivity}
 */
public class WidgetStandardPlace extends AppWidgetProvider {
	
	/**
	 * Update a specific widget
	 *
	 * @param context          the context of the application
	 * @param appWidgetManager the widget manager to update the widget
	 * @param repository       the repository to get the data
	 * @param appWidgetId      the widget id of the widget to update
	 */
	protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, AppRepository repository,
													  int appWidgetId) {
		
		Integer placeID = WidgetStandardPlaceConfigureActivity.loadWidgetSettings(context, appWidgetId);
		
		if (placeID == null) {
			return;
		}
		
		repository.getPlaceFromPlaceIdLiveData(placeID).observeForever(place -> {
			if (place == null) {
				return;
			}
			
			// Construct the RemoteViews object
			RemoteViews view = bindStandardWidget(context,
					  place,
					  repository.getFormattingService());
			
			// Instruct the widget manager to update the widget
			appWidgetManager.updateAppWidget(appWidgetId, view);
		});
	}
	
	/**
	 * Bind data to a standard widget layout and return it
	 *
	 * @param context           the context of the application
	 * @param place             the place to bind to the widget
	 * @param formattingService the formatting service to format the data
	 * @return the widget remote view with the data
	 */
	private static RemoteViews bindStandardWidget(Context context, Place place, FormattingService formattingService) {
		// Construct the RemoteViews object
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_standard_place);
		
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
		tmpCalendar.setTimeInMillis(currentWeather.getSunrise());
		int sunRiseHour = tmpCalendar.get(Calendar.HOUR_OF_DAY);
		tmpCalendar.setTimeInMillis(currentWeather.getSunset());
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
	 * Returns the weather icon id for the given weather code and if it is daytime or not.
	 *
	 * @param weatherCode The weather code
	 * @param isDaytime   True if it is daytime, false otherwise
	 * @return The weather icon id
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
	 * Update the widget with the given data
	 *
	 * @param context          The context
	 * @param appWidgetManager The widget manager
	 * @param appWidgetIds     All the widget ids
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		AppRepository repository = new AppRepository(context);
		
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, repository, appWidgetId);
		}
	}
	
	/**
	 * dpToPx(@NonNull Context context, float dip)
	 * <p>
	 * Just a DP to PX converter method
	 * </p>
	 *
	 * @param context Application context in order to access to resources
	 * @param dip     DP value that you want to convert
	 * @return The DP converted value into PX
	 */
	private int dpToPx(@NonNull Context context, float dip) {
		return (int) TypedValue.applyDimension(
				  TypedValue.COMPLEX_UNIT_DIP,
				  dip,
				  context.getResources().getDisplayMetrics());
	}
	
	/**
	 * Will handle receiving broadcast intents sent
	 *
	 * @param context The Context in which the receiver is running.
	 * @param intent  The Intent being received.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		ComponentName thisWidget = new ComponentName(context.getApplicationContext(), WidgetStandardPlace.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		if (appWidgetIds != null && appWidgetIds.length > 0) {
			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}
	
	
	/**
	 * Called when the widget is deleted from the home screen.
	 *
	 * @param context      The context
	 * @param appWidgetIds The app widget id
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds) {
			WidgetStandardPlaceConfigureActivity.deleteWidgetSettings(context, appWidgetId);
		}
	}
}