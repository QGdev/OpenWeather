package fr.qgdev.openweather.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;

/**
 * HourlyForecastGraphView
 * <p>
 * Used to generate graphics for hourly forecasts
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see ForecastView
 */
public class HourlyForecastGraphView extends ForecastView {

	private static final String TAG = HourlyForecastGraphView.class.getSimpleName();
	private final Logger logger = Logger.getLogger(TAG);

	private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
	private boolean[] isDayTime;


	/**
	 * HourlyForecastGraphView Constructor
	 * <p>
	 * Just build HourlyForecastGraphView object only with context
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 */
	public HourlyForecastGraphView(@NonNull Context context) {
		super(context);
	}


	/**
	 * HourlyForecastGraphView Constructor
	 * <p>
	 * Just build HourlyForecastGraphView object only with Context and AttributeSet
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 * @param attrs   AttributeSet for the GraphView
	 */
	public HourlyForecastGraphView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}


	/**
	 * generateIsDayTimeArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList)
	 * <p>
	 * Used to generate dayTime array which describe if it is day time for each hours of hourlyWeatherForecastArrayList
	 * </p>
	 *
	 * @param hourlyWeatherForecastArrayList ArrayList of HourlyWeatherForecasts
	 * @param dailyWeatherForecastArrayList  ArrayList of DailyWeatherForecasts
	 * @return The generated array
	 */
	private boolean[] generateIsDayTimeArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {

		HourlyWeatherForecast hourlyWeatherForecast;
		DailyWeatherForecast dailyWeatherForecast;
		boolean[] isDayTime = new boolean[hourlyWeatherForecastArrayList.size()];
		long previousItemDay, currentItemDay;
		int dayIndex = 0;
		Calendar calendar;

		//  Start by the beginning of each arraylist

		hourlyWeatherForecast = hourlyWeatherForecastArrayList.get(0);
		dailyWeatherForecast = dailyWeatherForecastArrayList.get(0);

		//  Initialization of calendar
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(hourlyWeatherForecast.dt);

		/*  Initialization of the previousItemDay
		 *       It is the ID of a day in a year, each day have an unique ID.
		 *       The day, 21th August 2021 will doesn't have the same ID as a 21th August 2020 or 2019.
		 *       The ID is composed of:
		 *           YYYYDDD
		 *               -   YYYY    :   Year of the day (2021...)
		 *               -   DDD     :   Day number in the whole year (223 for the 21th august)
		 * */
		previousItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;


		for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {

			hourlyWeatherForecast = hourlyWeatherForecastArrayList.get(index);
			calendar.setTimeInMillis(hourlyWeatherForecast.dt);

			currentItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;

			//  New day detected, switching to the new day by incrementing the counter (dayIndex) by one and updating dailyWeatherForecast variable
			if (previousItemDay < currentItemDay) {
				previousItemDay = currentItemDay;
				dayIndex++;
				dailyWeatherForecast = dailyWeatherForecastArrayList.get(dayIndex);
			}

			isDayTime[index] = dailyWeatherForecast.sunrise < hourlyWeatherForecast.dt && hourlyWeatherForecast.dt < dailyWeatherForecast.sunset;
		}

		return isDayTime;
	}


	/**
	 * hourlyWeatherForecastArrayToSelectedAttributeFloatArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull String selectedAttribute) throws NoSuchFieldException, IllegalAccessException
	 * <p>
	 * Used to get an array of selected attribute of an ArrayList of HourlyWeatherForecast objects
	 * </p>
	 *
	 * @param hourlyWeatherForecastArrayList The HourlyWeatherForecast arrayList
	 * @param selectedAttribute              The name of the wanted attribute
	 * @return The created array filled with all attributes
	 * @throws NoSuchFieldException   When the wanted attribute doesn't exist
	 * @throws IllegalAccessException When the wanted attributed is not a float
	 */
	private float[] hourlyWeatherForecastArrayToSelectedAttributeFloatArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull String selectedAttribute) throws NoSuchFieldException, IllegalAccessException {
		float[] returnedAttributeArray = new float[hourlyWeatherForecastArrayList.size()];
		Field classField = HourlyWeatherForecast.class.getDeclaredField(selectedAttribute);

		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = classField.getFloat(hourlyWeatherForecastArrayList.get(index));
		}

		return returnedAttributeArray;
	}


	/**
	 * initialization(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to initialize attributes used to draw a view
	 * </p>
	 *
	 * @param hourlyWeatherForecastArrayList ArrayList of HourlyWeatherForecast
	 * @param dailyWeatherForecastArrayList  ArrayList of DailyWeatherForecasts
	 * @param timeZone                       TimeZone of the place
	 * @param unitsFormattingService         FormattingService of the application to format dates
	 */
	public void initialization(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone) {

		this.COLUMN_WIDTH = dpToPx(90);
		float[] firstCurve, secondCurve;

		this.width = hourlyWeatherForecastArrayList.size() * COLUMN_WIDTH;
		this.height = dpToPx(850);

		this.hourlyWeatherForecastArrayList = hourlyWeatherForecastArrayList;

		formattingService = unitsFormattingService;

		this.timeZone = timeZone;

		isDayTime = generateIsDayTimeArray(hourlyWeatherForecastArrayList, (ArrayList<DailyWeatherForecast>) dailyWeatherForecastArrayList.clone());


		try {
			//  Temperatures graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "temperature");
			secondCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "temperatureFeelsLike");
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);

			//  Humidity graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "humidity");
			this.humidityGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), tertiaryGraphPaint);

			//  Pressure graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "pressure");
			this.pressureGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), primaryGraphPaint);

			//  Wind speeds graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "windSpeed");
			secondCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "windGustSpeed");
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);

			//  Precipitations graph
			this.precipitationsGraph = generateBitmapPrecipitationsGraphPath(
					hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "rain"),
					hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "snow"),
					hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "pop"),
					this.width, dpToPx(40), tertiaryGraphPaint, primaryGraphPaint, popBarGraphPaint);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}

	/**
	 * setMinimumHeight(@Px int minHeight)
	 * <p>
	 * Used to set minimum Height of the view
	 * </p>
	 *
	 * @param minHeight Minimum Height in pixel
	 * @see View
	 */
	@Override
	public void setMinimumHeight(@Px int minHeight) {
		super.setMinimumHeight(minHeight);
	}


	/**
	 * setMinimumWidth(@Px int minWidth)
	 * <p>
	 * Used to set minimum Width of the view
	 * </p>
	 *
	 * @param minWidth Minimum Height in pixel
	 * @see View
	 */
	@Override
	public void setMinimumWidth(@Px int minWidth) {
		super.setMinimumWidth(minWidth);
	}


	/**
	 * onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	 * <p>
	 * Used to set Measured Dimensions<br>
	 * IT IS A DUMB METHOD, DOESN'T TAKE CARE OF PARAMETERS<br>
	 * JUST TAKE CURRENT WIDTH AND CURRENT HEIGHT OF THE VIEW
	 * </p>
	 *
	 * @param widthMeasureSpec  Minimum Width in pixel
	 * @param heightMeasureSpec Minimum Height in pixel
	 * @apiNote DUMB METHOD, PARAMETERS ARE IGNORED !
	 * @see View
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(this.width, this.height);
	}


	/**
	 * drawStructureAndDate(@NonNull Canvas canvas, @NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to draw principal elements of the view such as date, day moments and separators
	 * </p>
	 *
	 * @param canvas                         Elements will be drawn on it
	 * @param hourlyWeatherForecastArrayList Where the name of the day will be drawn on y axis
	 * @param timeZone                       The timeZone of the place
	 */
	private void drawStructureAndDate(@NonNull Canvas canvas, @NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull TimeZone timeZone) {

		byte previousItemDay = 0, currentItemDay;
		int x_div = 0;
		int dateFirstLineY, dateSecondLineY, hourLineY, halfColumnWidth;
		Calendar calendar;
		Date date;

		calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTimeInMillis(hourlyWeatherForecastArrayList.get(0).dt);

		dateFirstLineY = dpToPx(15);
		dateSecondLineY = dpToPx(35);
		hourLineY = dpToPx(60);
		halfColumnWidth = COLUMN_WIDTH / 2;

		for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {

			date = new Date(hourlyWeatherForecastArrayList.get(index).dt);

			calendar.setTimeInMillis(hourlyWeatherForecastArrayList.get(index).dt);
			currentItemDay = BigDecimal.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).byteValue();

			//  New day detected, draw day div and date
			if (previousItemDay != currentItemDay) {
				previousItemDay = currentItemDay;
				canvas.drawLine(x_div, 0, x_div, canvas.getHeight(), this.datePaint);
				canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), x_div + 10, dateFirstLineY, this.datePaint);
				canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), x_div + 10, dateSecondLineY, this.datePaint);
			}
			//  Draw hour div
			else {
				canvas.drawLine(x_div, 120, x_div, canvas.getHeight(), this.structurePaint);
			}
			//  Draw hour
			canvas.drawText(formattingService.getFormattedHour(date, timeZone), x_div + halfColumnWidth, hourLineY, this.structurePaint);

			x_div += COLUMN_WIDTH;
		}
	}


	/**
	 * drawTemperatures(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw temperatures and feel like temperatures
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where temperatures will be drawn on the y axis
	 * @param middleOfColumnX              Where temperatures will be drawn on the x axis
	 */
	private void drawTemperatures(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		//Temperatures
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperature, false), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperatureFeelsLike, false), middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
	}


	/**
	 * drawPressure(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw pressure
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where pressure will be drawn on the y axis
	 * @param middleOfColumnX              Where pressure will be drawn on the x axis
	 */
	private void drawPressure(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFormattedPressure(currentHourlyWeatherForecast.pressure, false), middleOfColumnX, y, this.primaryPaint);
	}


	/**
	 * drawDewPoint(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw dewPoint
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where dewPoint will be drawn on the y axis
	 * @param middleOfColumnX              Where dewPoint will be drawn on the x axis
	 */
	private void drawDewPoint(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.dewPoint, false), middleOfColumnX, y, this.primaryPaint);
	}


	/**
	 * drawVisibility(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw visibility distance
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where visibility distance will be drawn on the y axis
	 * @param middleOfColumnX              Where visibility distance will be drawn on the x axis
	 */
	private void drawVisibility(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedDistance(currentHourlyWeatherForecast.visibility, true), middleOfColumnX, y, this.primaryPaint);
	}


	/**
	 * drawWindSpeed(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw wind speed
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where wind speed will be drawn on the y axis
	 * @param middleOfColumnX              Where wind speed will be drawn on the x axis
	 */
	private void drawWindSpeed(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windSpeed, true), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windGustSpeed, true), middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
	}


	/**
	 * drawWindDirection(@NonNull Canvas canvas, short windDirection, @Px int top, @Px int left, @Px int width)
	 * <p>
	 * Used to draw direction in cardinal point and in degrees
	 * </p>
	 *
	 * @param canvas        Elements will be drawn on it
	 * @param windDirection Where data will be taken
	 * @param top           Where elements will be drawn on the y axis
	 * @param left          Where elements will be drawn on the x axis
	 * @param width         Width of each contained elements
	 */
	private void drawWindDirection(@NonNull Canvas canvas, short windDirection, @Px int top, @Px int left, @Px int width) {
		int middle = width / 2;
		drawWindDirectionIcon(canvas, windDirection, left + dpToPx(5), top, width - dpToPx(10));

		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(windDirection), left + middle, top + width + dpToPx(5), this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(windDirection), left + middle, top + width + dpToPx(25), this.primaryPaint);
	}


	/**
	 * generateBitmap1CurvesGraphPath(float[] curveData, @Px int width, @Px int height, @NonNull Paint curvePaint)
	 * <p>
	 * Used to generate bitmap containing graph of one set of data
	 * </p>
	 *
	 * @param curveData  Array of numerical values that will be used to draw the curve
	 * @param width      Width of the wanted graph
	 * @param height     Height of the wanted graph
	 * @param curvePaint Paint that will be used to draw curve
	 * @return A Bitmap with the generated curve, with the wanted height and width and in the ARGB_4444 format
	 */
	private Bitmap generateBitmap1CurvesGraphPath(float[] curveData, @Px int width, @Px int height, @NonNull Paint curvePaint) {

		//  Initializing graph path
		Path curvePath = new Path();

		//  Searching for max and min values in array
		float minValue = curveData[0],
				maxValue = curveData[0];

		for (int index = 0; index < curveData.length; index++) {
			if (curveData[index] > maxValue) maxValue = curveData[index];
			if (curveData[index] < minValue) minValue = curveData[index];
		}

		//  Find the value to adjust all values so that the minimum is 0
		float addValueMinTo0 = minValue * (-1);
		//  Find scale factor of Y axis
		float scaleFactorY = 1 / (maxValue + addValueMinTo0);

		//  Doing Bezier curve calculations for each curve
		float connectionPointsX, point1_X, point1_Y, point2_Y, point2_X;
		float columnWidth = width / curveData.length,
				halfColumnWidth = columnWidth / 2F,
				//  To avoid curve trimming
				top = 4,
				bottom = height - 4,
				drawHeight = bottom - top;

		//  Clear each paths
		curvePath.reset();

		//  If min and max values are the same, the resulting curve is a flat curve
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1_X = halfColumnWidth;
			point1_Y = bottom - drawHeight * (curveData[0] + addValueMinTo0) * scaleFactorY;

			curvePath.moveTo(0, point1_Y);

			for (int index = 1; index < curveData.length; index++) {

				//  Position calculations
				point2_X = index * COLUMN_WIDTH + halfColumnWidth;
				point2_Y = bottom - drawHeight * (curveData[index] + addValueMinTo0) * scaleFactorY;

				//  Middle connection point
				connectionPointsX = (point1_X + point2_X) / 2F;

				//  Extending each curves
				curvePath.cubicTo(connectionPointsX, point1_Y, connectionPointsX, point2_Y, point2_X, point2_Y);

				//  Moving to new points to old points
				point1_X = point2_X;
				point1_Y = point2_Y;
			}

			curvePath.lineTo(width, point1_Y);
			curvePath.moveTo(width, point1_Y);
		} else {
			curvePath.moveTo(0, drawHeight);

			curvePath.lineTo(0, drawHeight);
			curvePath.lineTo(width, drawHeight);

			curvePath.moveTo(width, drawHeight);
		}
		// Close path
		curvePath.close();

		//  Generating returned Bitmap
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawPath(curvePath, curvePaint);

		return returnedBitmap;
	}


	/**
	 * drawPrecipitations((@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw precipitations (rain, snow, pop)
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyWeatherForecast Where data will be taken
	 * @param y                            Where precipitations will be drawn on the y axis
	 * @param middleOfColumnX              Where precipitations will be drawn on the x axis
	 */
	private void drawPrecipitations(@NonNull Canvas canvas, @NonNull HourlyWeatherForecast currentHourlyWeatherForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.rain, true), middleOfColumnX, y, this.tertiaryPaint);
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.snow, true), middleOfColumnX, y + dpToPx(25), this.primaryPaint);

		canvas.drawText(String.format("%d %%", BigDecimal.valueOf(currentHourlyWeatherForecast.pop * 100).intValue()), middleOfColumnX, y + dpToPx(50), this.secondaryPaint);

	}


	/**
	 * onDraw(@NonNull Canvas canvas)
	 * <p>
	 * Called to generate view
	 * </p>
	 *
	 * @param canvas The canvas that will be displayed on screen
	 * @see android.view.View
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);

		HourlyWeatherForecast currentHourlyWeatherForecast;
		int halfWidthX = COLUMN_WIDTH / 2, drawableX = halfWidthX - dpToPx(25);

		drawStructureAndDate(canvas, hourlyWeatherForecastArrayList, timeZone);

		for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {
			currentHourlyWeatherForecast = hourlyWeatherForecastArrayList.get(index);

			drawWeatherConditionIcons(canvas, currentHourlyWeatherForecast.weatherCode, dpToPx(70), drawableX, dpToPx(50), dpToPx(50), isDayTime[index]);

			drawTemperatures(canvas, currentHourlyWeatherForecast, dpToPx(140), halfWidthX);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.humidity), halfWidthX, dpToPx(245), this.tertiaryPaint);
			drawPressure(canvas, currentHourlyWeatherForecast, dpToPx(315), halfWidthX);

			drawUvIndex(canvas, currentHourlyWeatherForecast.uvIndex, drawableX, dpToPx(370), dpToPx(50));

			drawDewPoint(canvas, currentHourlyWeatherForecast, dpToPx(445), halfWidthX);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.cloudiness), halfWidthX, dpToPx(475), this.primaryPaint);
			drawVisibility(canvas, currentHourlyWeatherForecast, dpToPx(505), halfWidthX);

			drawWindSpeed(canvas, currentHourlyWeatherForecast, dpToPx(540), halfWidthX);
			drawWindDirection(canvas, currentHourlyWeatherForecast.windDirection, dpToPx(625), drawableX, dpToPx(50));

			drawPrecipitations(canvas, currentHourlyWeatherForecast, dpToPx(725), halfWidthX);

			halfWidthX += COLUMN_WIDTH;
			drawableX += COLUMN_WIDTH;
		}

		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(175), null);
		canvas.drawBitmap(this.humidityGraph, 0, dpToPx(260), null);
		canvas.drawBitmap(this.pressureGraph, 0, dpToPx(330), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(575), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(785), null);
	}
}
