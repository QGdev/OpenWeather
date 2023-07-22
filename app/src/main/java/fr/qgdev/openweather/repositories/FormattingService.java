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

package fr.qgdev.openweather.repositories;

import android.content.Context;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.repositories.settings.SettingsManager;

public class FormattingService {
	
	private final Context context;
	private final SettingsManager settingsManager;
	
	private TemperatureConversion temperatureConversion;
	private MeasureConversion measureConversion;
	private PressureConversion pressureConversion;
	private DirectionConversion directionConversion;
	
	//  Temperature format specifier for int or float format
	private String temperatureUnitSymbol;
	private String temperatureFormatSpecifierInt;
	private String temperatureFormatSpecifierFloat;
	
	//  Short distance format specifier for int or float format
	private String shortDistanceUnitSymbol;
	private String shortDistanceFormatSpecifierInt;
	private String shortDistanceFormatSpecifierFloat;
	
	//  Distance format specifier for int or float format
	private String distanceUnitSymbol;
	private String distanceFormatSpecifierInt;
	private String distanceFormatSpecifierFloat;
	
	//  Speed format specifier for int or float format
	private String speedUnitSymbol;
	private String speedFormatSpecifierInt;
	private String speedFormatSpecifierFloat;
	
	//  Pressure format specifier
	private String pressureUnitSymbol;
	private String pressureFormatSpecifier;
	
	//  TimeHour format specifier
	private SimpleDateFormat hourFormat;
	private SimpleDateFormat timeFormat;
	private SimpleDateFormat shortDayNameFormat;
	private SimpleDateFormat dayMonthFormat;
	private SimpleDateFormat fullTimeHourFormat;
	
	public FormattingService(Context context, SettingsManager settingsManager) {
		this.context = context;
		this.settingsManager = settingsManager;
		update();
	}
	
	public void update() {
		temperatureUnitInit();
		measureUnitInit();
		pressureUnitInit();
		directionUnitInit();
		timeDateFormatInit();
	}
	
	//  Temperature conversion
	private float toCelsius(float temperature) {
		return temperature - 273.15F;
	}
	
	private float toFahrenheit(float temperature) {
		return toCelsius(temperature) * (9F / 5F) + 32;
	}
	
	private float toKelvin(float temperature) {
		return temperature;
	}
	
	//  Measure conversion
	////    Distance, Length
	private float distanceToMiles(float distance) {
		return distance * 0.000621371F;
	}
	
	private float distanceToInches(float distance) {
		return distance * 0.0393701F;
	}
	
	private float distanceToMetric(float distance) {
		return distance / 1000F;
	}
	
	////    Speed
	private float speedToImperial(float speed) {
		return speed * 2.23694F;
	}
	
	private float speedToMetric(float speed) {
		return speed * 3.6F;
	}
	
	//  Pressure conversion
	private float toHpa(float pressure) {
		return pressure;
	}
	
	private float toMbar(float pressure) {
		return pressure;
	}
	
	private float toPsi(float pressure) {
		return pressure * 0.0145038F;
	}
	
	private float toInhg(float pressure) {
		return pressure * 0.02953F;
	}
	
	//  Wind direction conversion
	private String toDegrees(short direction) {
		return String.format(settingsManager.getDefaultLocale(), "%d°", direction);
	}
	
	private String toCardinal(short direction) {
		int[] directions = {
				  R.string.wind_direction_north,
				  R.string.wind_direction_northnortheast,
				  R.string.wind_direction_northeast,
				  R.string.wind_direction_eastnortheast,
				  R.string.wind_direction_east,
				  R.string.wind_direction_eastsoutheast,
				  R.string.wind_direction_southeast,
				  R.string.wind_direction_southsoutheast,
				  R.string.wind_direction_south,
				  R.string.wind_direction_southsouthwest,
				  R.string.wind_direction_southwest,
				  R.string.wind_direction_westsouthwest,
				  R.string.wind_direction_west,
				  R.string.wind_direction_westnorthwest,
				  R.string.wind_direction_northwest,
				  R.string.wind_direction_northnorthwest};
		
		
		int index = (int) ((direction + 11.25) / 22.5) % 16;
		
		if (index >= 0 && index < directions.length) {
			return context.getResources().getString(directions[index]);
		}
		
		return "N/A";
	}
	
	public float convertTemperature(float temperature) {
		return this.temperatureConversion.temperatureConversion(temperature);
	}
	
	public float convertDistance(float distance) {
		return this.measureConversion.distanceConversion(distance);
	}
	
	public float convertShortDistance(float distance) {
		return this.measureConversion.shortDistanceConversion(distance);
	}
	
	public float convertSpeed(float speed) {
		return this.measureConversion.speedConversion(speed);
	}
	
	public float convertPressure(float pressure) {
		return this.pressureConversion.pressureConversion(pressure);
	}
	
	public String convertDirection(short direction, boolean isReadable) {
		if (isReadable && direction >= 0 && direction <= 360)
			return this.directionConversion.directionConversion(direction);
		return "N/A";
	}
	
	private SimpleDateFormat getSimpleDateFormatForTimeZone(SimpleDateFormat simpleDateFormat, TimeZone timeZone) {
		SimpleDateFormat clonedSimpleDateFormat = (SimpleDateFormat) simpleDateFormat.clone();
		clonedSimpleDateFormat.setTimeZone(timeZone);
		return clonedSimpleDateFormat;
	}
	
	//  Temperature formatting
	////    Int
	public String getIntFormattedTemperature(float temperature, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  temperatureFormatSpecifierInt,
				  BigDecimal.valueOf(convertTemperature(temperature)).intValue(),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? temperatureUnitSymbol : "");
	}
	
	////    Float
	public String getFloatFormattedTemperature(float temperature, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  temperatureFormatSpecifierFloat,
				  convertTemperature(temperature),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? temperatureUnitSymbol : "");
	}
	
	//  Short Distance formatting
	////    Int
	public String getIntFormattedShortDistance(float shortDistance, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  shortDistanceFormatSpecifierInt,
				  BigDecimal.valueOf(convertShortDistance(shortDistance)).intValue(),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? shortDistanceUnitSymbol : "");
	}
	
	////    Float
	public String getFloatFormattedShortDistance(float shortDistance, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  shortDistanceFormatSpecifierFloat,
				  convertShortDistance(shortDistance),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? shortDistanceUnitSymbol : "");
	}
	
	//  Distance formatting
	////    Int
	public String getIntFormattedDistance(float distance, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  distanceFormatSpecifierInt,
				  BigDecimal.valueOf(convertDistance(distance)).intValue(),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? distanceUnitSymbol : "");
	}
	
	////    Float
	public String getFloatFormattedDistance(float distance, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  distanceFormatSpecifierFloat,
				  convertDistance(distance),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? distanceUnitSymbol : "");
	}
	
	//  Speed formatting
	////    Int
	public String getIntFormattedSpeed(float speed, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  speedFormatSpecifierInt,
				  BigDecimal.valueOf(convertSpeed(speed)).intValue(),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? speedUnitSymbol : "");
	}
	
	////    Float
	public String getFloatFormattedSpeed(float speed, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  speedFormatSpecifierFloat,
				  convertSpeed(speed),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? speedUnitSymbol : "");
	}
	
	//  Pressure formatting
	public String getFormattedPressure(float pressure, FormattingSpec formattingSpec) {
		return String.format(settingsManager.getDefaultLocale(),
				  pressureFormatSpecifier,
				  convertPressure(pressure),
				  (formattingSpec.value % 2 == 1) ? " " : "",
				  (formattingSpec.value >= 2) ? pressureUnitSymbol : "");
	}
	
	private void temperatureUnitInit() {
		temperatureFormatSpecifierInt = "%d%s°%s";
		temperatureFormatSpecifierFloat = "%.1f%s°%s";
		
		switch (settingsManager.getTemperatureSetting()) {
			case FAHRENHEIT: {
				temperatureUnitSymbol = "F";
				this.temperatureConversion = this::toFahrenheit;
				break;
			}
			case KELVIN: {
				temperatureUnitSymbol = "K";
				this.temperatureConversion = this::toKelvin;
				break;
			}
			default:    //  Default case is using Celsius unit
			case CELSIUS: {
				temperatureUnitSymbol = "C";
				this.temperatureConversion = this::toCelsius;
				break;
			}
		}
	}
	
	//  Direction formatting
	////    Selected choice
	public String getFormattedDirection(short direction, boolean isReadable) {
		return convertDirection(direction, isReadable);
	}
	
	////    Cardinal points
	public String getFormattedDirectionInCardinalPoints(short direction) {
		if (direction >= 0 && direction <= 360)
			return toCardinal(direction);
		else return "N/A";
	}
	
	////    Degrees direction
	public String getFormattedDirectionInDegrees(short direction) {
		if (direction >= 0 && direction <= 360)
			return toDegrees(direction);
		else return "N/A";
	}
	
	//  TimeHour formatting
	////    Hour
	public String getFormattedHour(Date date, TimeZone timeZone) {
		return getSimpleDateFormatForTimeZone(this.hourFormat, timeZone).format(date);
	}
	
	////    Time
	public String getFormattedTime(Date date, TimeZone timeZone) {
		return getSimpleDateFormatForTimeZone(this.timeFormat, timeZone).format(date);
	}
	
	////    Day short name formatting
	public String getFormattedShortDayName(Date date, TimeZone timeZone) {
		return getSimpleDateFormatForTimeZone(this.shortDayNameFormat, timeZone).format(date);
	}
	
	////    Day month formatting
	public String getFormattedDayMonth(Date date, TimeZone timeZone) {
		return getSimpleDateFormatForTimeZone(this.dayMonthFormat, timeZone).format(date);
	}
	
	////    Full time hour  formatting
	public String getFormattedFullTimeHour(Date date, TimeZone timeZone) {
		return getSimpleDateFormatForTimeZone(this.fullTimeHourFormat, timeZone).format(date);
	}
	
	private void measureUnitInit() {
		shortDistanceFormatSpecifierInt = "%d%s%s";
		shortDistanceFormatSpecifierFloat = "%.2f%s%s";
		distanceFormatSpecifierInt = "%d%s%s";
		distanceFormatSpecifierFloat = "%.1f%s%s";
		speedFormatSpecifierInt = "%d%s%s";
		speedFormatSpecifierFloat = "%.1f%s%s";
		
		switch (settingsManager.getMeasureSetting()) {
			case IMPERIAL: {
				shortDistanceUnitSymbol = "in";
				distanceUnitSymbol = "mi";
				speedUnitSymbol = "mph";
				
				this.measureConversion = new MeasureConversion() {
					@Override
					public float distanceConversion(float distance) {
						return distanceToMiles(distance);
					}
					
					@Override
					public float shortDistanceConversion(float distance) {
						return distanceToInches(distance);
					}
					
					@Override
					public float speedConversion(float speed) {
						return speedToImperial(speed);
					}
				};
				break;
			}
			default:    //  Default case is using metric units
			case METRIC: {
				shortDistanceUnitSymbol = "mm";
				distanceUnitSymbol = "km";
				speedUnitSymbol = "km/h";
				
				this.measureConversion = new MeasureConversion() {
					@Override
					public float distanceConversion(float distance) {
						return distanceToMetric(distance);
					}
					
					@Override
					public float shortDistanceConversion(float distance) {
						return distance;
					}
					
					@Override
					public float speedConversion(float speed) {
						return speedToMetric(speed);
					}
				};
				break;
			}
		}
	}
	
	private void pressureUnitInit() {
		//  Pressure
		switch (settingsManager.getPressureSetting()) {
			case BAROMETRIC: {
				pressureFormatSpecifier = "%.0f%s%s";
				pressureUnitSymbol = "mBar";
				this.pressureConversion = this::toMbar;
				break;
			}
			case POUNDS_SQUARE_INCH: {
				pressureFormatSpecifier = "%.2f%s%s";
				pressureUnitSymbol = "psi";
				this.pressureConversion = this::toPsi;
				break;
			}
			case INCH_MERCURY: {
				pressureFormatSpecifier = "%.2f%s%s";
				pressureUnitSymbol = "inHg";
				this.pressureConversion = this::toInhg;
				break;
			}
			default:    //  Default case is using pascal unit
			case HECTOPASCAL: {
				pressureFormatSpecifier = "%.0f%s%s";
				pressureUnitSymbol = "hPa";
				this.pressureConversion = this::toHpa;
				break;
			}
		}
	}
	
	public enum FormattingSpec {
		NO_UNIT_NO_SPACE(0),
		NO_UNIT_BUT_SPACE(1),   // Actually only useful for temperature formatting
		UNIT_BUT_NO_SPACE(2),
		UNIT_AND_SPACE(3);
		
		public final int value;
		
		FormattingSpec(int value) {
			this.value = value;
		}
	}
	
	private void directionUnitInit() {
		//  Direction
		switch (settingsManager.getWindDirectionSetting()) {
			case ANGULAR: {
				this.directionConversion = this::toDegrees;
				break;
			}
			default:    //  Default case is using cardinal
			case CARDINAL_POINTS: {
				this.directionConversion = this::toCardinal;
				break;
			}
		}
	}
	
	private void timeDateFormatInit() {
		
		Locale defaultLocale = settingsManager.getDefaultLocale();
		//  timeDate
		switch (settingsManager.getTimeSetting()) {
			case TWELVE_HOURS: {
				this.hourFormat = new SimpleDateFormat("KK:00 a", defaultLocale);
				this.timeFormat = new SimpleDateFormat("KK:mm a", defaultLocale);
				this.fullTimeHourFormat = new SimpleDateFormat("dd/MM/yy KK:mm a", defaultLocale);
				
				break;
			}
			
			default:    //  Default case is using 24 hours format
			case TWENTY_FOUR_HOURS: {
				this.hourFormat = new SimpleDateFormat("HH:00", defaultLocale);
				this.timeFormat = new SimpleDateFormat("HH:mm", defaultLocale);
				this.fullTimeHourFormat = new SimpleDateFormat("dd/MM/yy HH:mm", defaultLocale);
				
				break;
			}
		}
		this.shortDayNameFormat = new SimpleDateFormat("EE", defaultLocale);
		this.dayMonthFormat = new SimpleDateFormat("dd/MM", defaultLocale);
	}
	
	private interface TemperatureConversion {
		//  Converting for temperature
		float temperatureConversion(float temperature);
	}
	
	private interface MeasureConversion {
		//  Converting for short distances like precipitations quantities
		float shortDistanceConversion(float distance);
		
		//  Converting for distances like visibility
		float distanceConversion(float distance);
		
		//  Converting speed like wind speeds
		float speedConversion(float speed);
	}
	
	private interface PressureConversion {
		//  Converting for pressure
		float pressureConversion(float pressure);
	}
	
	private interface DirectionConversion {
		//  Converting for direction
		String directionConversion(short direction);
	}
}
