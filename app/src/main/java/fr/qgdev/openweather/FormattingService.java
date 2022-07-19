package fr.qgdev.openweather;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormattingService {

	private final Context context;
	private TemperatureConversion temperatureConversion;
	private MeasureConversion measureConversion;
	private PressureConversion pressureConversion;
	private DirectionConversion directionConversion;
	//  Pressure format specifier for int or float format
	private String temperatureFormatSpecifierInt;
	private String temperatureFormatSpecifierFloat;
	//  Short distance format specifier for int or float format
	private String shortDistanceFormatSpecifierInt;
	private String shortDistanceFormatSpecifierFloat;
	//  Distance format specifier for int or float format
	private String distanceFormatSpecifierInt;
	private String distanceFormatSpecifierFloat;
	//  Speed format specifier for int or float format
	private String speedFormatSpecifierInt;
	private String speedFormatSpecifierFloat;
	//  Pressure format specifier
	private String pressureFormatSpecifier;
	//  TimeHour format specifier
	private SimpleDateFormat hourFormat;
	private SimpleDateFormat timeFormat;
	private SimpleDateFormat shortDayNameFormat;
	private SimpleDateFormat dayMonthFormat;
	private SimpleDateFormat fullTimeHourFormat;

	public FormattingService(Context context) {
		this.context = context;

		SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(context);

		temperatureUnitInit(userPref);
		measureUnitInit(userPref);
		pressureUnitInit(userPref);
		directionUnitInit(userPref);
		timeDateFormatInit(userPref);
	}

	//  Temperature conversion
	private float toCelsius(float temperature) {
		return temperature - 273.15F;
	}

	private float toFahrenheit(float temperature) {
		return toCelsius(temperature) * (9F / 5F) + 32;
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
		return String.format("%d°", direction);
	}

	private String toCardinal(short direction) {
		//  N
		if (direction > 348.75 || direction < 11.25)
			return context.getResources().getString(R.string.wind_direction_north);
			//  NNE
		else if (direction >= 11.25 && direction < 33.75)
			return context.getResources().getString(R.string.wind_direction_northnortheast);
			//  NE
		else if (direction >= 33.75 && direction <= 56.25)
			return context.getResources().getString(R.string.wind_direction_northeast);

			//  ENE
		else if (direction > 56.25 && direction <= 78.75)
			return context.getResources().getString(R.string.wind_direction_eastnortheast);
			//  E
		else if (direction > 78.75 && direction < 101.25)
			return context.getResources().getString(R.string.wind_direction_east);
			//  ESE
		else if (direction >= 101.25 && direction < 123.75)
			return context.getResources().getString(R.string.wind_direction_eastsoutheast);

			//  SE
		else if (direction >= 123.75 && direction <= 146.25)
			return context.getResources().getString(R.string.wind_direction_southeast);
			// SSE
		else if (direction > 146.25 && direction <= 168.75)
			return context.getResources().getString(R.string.wind_direction_southsoutheast);
			//  S
		else if (direction > 168.75 && direction < 191.25)
			return context.getResources().getString(R.string.wind_direction_south);
			//  SSW
		else if (direction >= 191.25 && direction < 213.75)
			return context.getResources().getString(R.string.wind_direction_southsouthwest);
			//  SW
		else if (direction >= 213.75 && direction <= 236.25)
			return context.getResources().getString(R.string.wind_direction_southwest);

			//  WSW
		else if (direction > 236.25 && direction <= 258.75)
			return context.getResources().getString(R.string.wind_direction_westsouthwest);
			//  W
		else if (direction > 258.75 && direction < 281.25)
			return context.getResources().getString(R.string.wind_direction_west);
			//  WNW
		else if (direction >= 281.25 && direction < 303.75)
			return context.getResources().getString(R.string.wind_direction_westnorthwest);

			//  NW
		else if (direction >= 303.75 && direction <= 326.25)
			return context.getResources().getString(R.string.wind_direction_northwest);
			//  NNW
		else if (direction > 326.25 && direction <= 348.75)
			return context.getResources().getString(R.string.wind_direction_northnorthwest);

		else return "N/A";
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
		if (direction >= 0 && direction <= 360 && isReadable)
			return this.directionConversion.directionConversion(direction);
		else return "N/A";
	}

	private SimpleDateFormat getSimpleDateFormatForTimeZone(SimpleDateFormat simpleDateFormat, TimeZone timeZone) {
		SimpleDateFormat clonedSimpleDateFormat = (SimpleDateFormat) simpleDateFormat.clone();
		clonedSimpleDateFormat.setTimeZone(timeZone);
		return clonedSimpleDateFormat;
	}

	//  Temperature formatting
	////    Int
	public String getIntFormattedTemperature(float temperature, boolean spaceBetween) {
		return String.format(temperatureFormatSpecifierInt, BigDecimal.valueOf(convertTemperature(temperature)).intValue(), (spaceBetween) ? " " : "");
	}

	////    Float
	public String getFloatFormattedTemperature(float temperature, boolean spaceBetween) {
		return String.format(temperatureFormatSpecifierFloat, convertTemperature(temperature), (spaceBetween) ? " " : "");
	}

	//  Short Distance formatting
	////    Int
	public String getIntFormattedShortDistance(float shortDistance, boolean spaceBetween) {
		return String.format(shortDistanceFormatSpecifierInt, BigDecimal.valueOf(convertShortDistance(shortDistance)).intValue(), (spaceBetween) ? " " : "");
	}

	////    Float
	public String getFloatFormattedShortDistance(float shortDistance, boolean spaceBetween) {
		return String.format(shortDistanceFormatSpecifierFloat, convertShortDistance(shortDistance), (spaceBetween) ? " " : "");
	}

	//  Distance formatting
	////    Int
	public String getIntFormattedDistance(float distance, boolean spaceBetween) {
		return String.format(distanceFormatSpecifierInt, BigDecimal.valueOf(convertDistance(distance)).intValue(), (spaceBetween) ? " " : "");
	}

	////    Float
	public String getFloatFormattedDistance(float distance, boolean spaceBetween) {
		return String.format(distanceFormatSpecifierFloat, convertDistance(distance), (spaceBetween) ? " " : "");
	}

	//  Speed formatting
	////    Int
	public String getIntFormattedSpeed(float speed, boolean spaceBetween) {
		return String.format(speedFormatSpecifierInt, BigDecimal.valueOf(convertSpeed(speed)).intValue(), (spaceBetween) ? " " : "");
	}

	////    Float
	public String getFloatFormattedSpeed(float speed, boolean spaceBetween) {
		return String.format(speedFormatSpecifierFloat, convertSpeed(speed), (spaceBetween) ? " " : "");
	}

	//  Pressure formatting
	public String getFormattedPressure(float pressure, boolean spaceBetween) {
		return String.format(pressureFormatSpecifier, convertPressure(pressure), (spaceBetween) ? " " : "");
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

	private void temperatureUnitInit(SharedPreferences userPref) {
		//  Temperature
		switch (userPref.getString("temperature_unit", "")) {
			case "fahrenheit": {
				temperatureFormatSpecifierInt = "%d%s°F";
				temperatureFormatSpecifierFloat = "%.1f%s°F";

				this.temperatureConversion = this::toFahrenheit;
				break;
			}
			default:    //  Default case is using Celsius unit
			{
				temperatureFormatSpecifierInt = "%d%s°C";
				temperatureFormatSpecifierFloat = "%.1f%s°C";

				this.temperatureConversion = this::toCelsius;
				break;
			}
		}
	}

	private void measureUnitInit(SharedPreferences userPref) {
		//  Measure
		switch (userPref.getString("measure_unit", "")) {
			case "imperial": {
				shortDistanceFormatSpecifierInt = "%d%sin.";
				shortDistanceFormatSpecifierFloat = "%.2f%sin.";
				distanceFormatSpecifierInt = "%d%smi.";
				distanceFormatSpecifierFloat = "%.1f%smi.";
				speedFormatSpecifierInt = "%d%smph";
				speedFormatSpecifierFloat = "%.1f%smph";

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
			{
				shortDistanceFormatSpecifierInt = "%d%smm";
				shortDistanceFormatSpecifierFloat = "%.1f%smm";
				distanceFormatSpecifierInt = "%d%skm";
				distanceFormatSpecifierFloat = "%.1f%skm";
				speedFormatSpecifierInt = "%d%skm/h";
				speedFormatSpecifierFloat = "%.1f%skm/h";
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

	private void pressureUnitInit(SharedPreferences userPref) {
		//  Pressure
		switch (userPref.getString("pressure_unit", "")) {
			case "mbar": {
				pressureFormatSpecifier = "%.0f%smBar";
				this.pressureConversion = this::toMbar;
				break;
			}
			case "psi": {
				pressureFormatSpecifier = "%.2f%spsi";
				this.pressureConversion = this::toPsi;
				break;
			}
			case "inhg": {
				pressureFormatSpecifier = "%.2f%sinHg";
				this.pressureConversion = this::toInhg;
				break;
			}
			default:    //  Default case is using pascal unit
			{
				pressureFormatSpecifier = "%.0f%shPa";
				this.pressureConversion = this::toHpa;
				break;
			}
		}
	}

	private void directionUnitInit(SharedPreferences userPref) {
		//  Direction
		switch (userPref.getString("direction_unit", "")) {
			case "angular": {
				this.directionConversion = this::toDegrees;
				break;
			}
			default:    //  Default case is using cardinal
			{
				this.directionConversion = this::toCardinal;
				break;
			}
		}
	}

	private void timeDateFormatInit(SharedPreferences userPref) {
		//  timeDate
		switch (userPref.getString("time_format", "")) {
			case "12": {
				this.hourFormat = new SimpleDateFormat("KK:00 a");
				this.timeFormat = new SimpleDateFormat("KK:mm a");
				this.fullTimeHourFormat = new SimpleDateFormat("dd/MM/yy KK:mm a");

				break;
			}

			default:    //  Default case is using 24 hours format
			{
				this.hourFormat = new SimpleDateFormat("HH:00");
				this.timeFormat = new SimpleDateFormat("HH:mm");
				this.fullTimeHourFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

				break;
			}
		}
		this.shortDayNameFormat = new SimpleDateFormat("EE");
		this.dayMonthFormat = new SimpleDateFormat("dd/MM");
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
