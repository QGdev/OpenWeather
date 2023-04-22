package fr.qgdev.openweather.customview;

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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.metrics.DailyWeatherForecast;
import fr.qgdev.openweather.metrics.HourlyWeatherForecast;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.utils.ParameterizedCallable;

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
	
	private int columnWidth;
	
	private List<HourlyWeatherForecast> hourlyWeatherForecastList;
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
	 * generateIsDayTimeArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastList)
	 * <p>
	 * Used to generate dayTime array which describe if it is day time for each hours of hourlyWeatherForecastList
	 * </p>
	 *
	 * @param hourlyWeatherForecastList ArrayList of HourlyWeatherForecasts
	 * @param dailyWeatherForecastList  ArrayList of DailyWeatherForecasts
	 * @return The generated array
	 */
	private boolean[] generateIsDayTimeArray(@NonNull List<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull List<DailyWeatherForecast> dailyWeatherForecastList) {
		
		HourlyWeatherForecast hourlyWeatherForecast;
		DailyWeatherForecast dailyWeatherForecast;
		boolean[] isDayTime = new boolean[hourlyWeatherForecastList.size()];
		long previousItemDay, currentItemDay;
		int dayIndex = 0;
		Calendar calendar;
		
		//  Start by the beginning of each arraylist
		
		hourlyWeatherForecast = hourlyWeatherForecastList.get(0);
		dailyWeatherForecast = dailyWeatherForecastList.get(0);
		
		//  Initialization of calendar
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(hourlyWeatherForecast.getDt());
		
		/*  Initialization of the previousItemDay
		 *       It is the ID of a day in a year, each day have an unique ID.
		 *       The day, 21th August 2021 will doesn't have the same ID as a 21th August 2020 or 2019.
		 *       The ID is composed of:
		 *           YYYYDDD
		 *               -   YYYY    :   Year of the day (2021...)
		 *               -   DDD     :   Day number in the whole year (223 for the 21th august)
		 * */
		previousItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;
		
		
		for (int index = 0; index < hourlyWeatherForecastList.size(); index++) {
			
			hourlyWeatherForecast = hourlyWeatherForecastList.get(index);
			calendar.setTimeInMillis(hourlyWeatherForecast.getDt());
			
			currentItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;
			
			//  New day detected, switching to the new day by incrementing the counter (dayIndex) by one and updating dailyWeatherForecast variable
			if (previousItemDay < currentItemDay) {
				previousItemDay = currentItemDay;
				dayIndex++;
				dailyWeatherForecast = dailyWeatherForecastList.get(dayIndex);
			}
			
			isDayTime[index] = dailyWeatherForecast.getSunrise() < hourlyWeatherForecast.getDt() && hourlyWeatherForecast.getDt() < dailyWeatherForecast.getSunset();
		}

		return isDayTime;
	}
	
	
	/**
	 * hourlyWeatherForecastArrayToSelectedAttributeFloatArray(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull String selectedAttribute) throws NoSuchFieldException, IllegalAccessException
	 * <p>
	 * Used to get an array of selected attribute of an ArrayList of HourlyWeatherForecast objects
	 * </p>
	 *
	 * @param hourlyWeatherForecastList The HourlyWeatherForecast arrayList
	 * @param attributeGetter           Used to provide the needed attribute
	 * @return The created array filled with all attributes
	 */
	private float[] hourlyWeatherForecastArrayToSelectedAttributeFloatArray(@NonNull List<HourlyWeatherForecast> hourlyWeatherForecastList, ParameterizedCallable<HourlyWeatherForecast, Number> attributeGetter) {
		float[] returnedAttributeArray = new float[hourlyWeatherForecastList.size()];
		
		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = attributeGetter.call(hourlyWeatherForecastList.get(index)).floatValue();
		}
		
		return returnedAttributeArray;
	}
	
	
	/**
	 * initialization(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to initialize attributes used to draw a view
	 * </p>
	 *
	 * @param hourlyWeatherForecastList ArrayList of HourlyWeatherForecast
	 * @param dailyWeatherForecastList  ArrayList of DailyWeatherForecasts
	 * @param timeZone                  TimeZone of the place
	 * @param unitsFormattingService    FormattingService of the application to format dates
	 */
	public void initialization(@NonNull List<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull List<DailyWeatherForecast> dailyWeatherForecastList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone) {
		this.columnWidth = dpToPx(90);
		float[] firstCurve, secondCurve;
		
		this.width = hourlyWeatherForecastList.size() * columnWidth;
		this.height = dpToPx(820);
		
		this.hourlyWeatherForecastList = hourlyWeatherForecastList;
		
		formattingService = unitsFormattingService;
		
		this.timeZone = timeZone;
		
		isDayTime = generateIsDayTimeArray(hourlyWeatherForecastList, Collections.unmodifiableList(dailyWeatherForecastList));
		
		
		try {
			//  Temperatures graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getTemperature);
			secondCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getTemperatureFeelsLike);
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Humidity graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getHumidity);
			this.humidityGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), tertiaryGraphPaint);
			
			//  Pressure graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getPressure);
			this.pressureGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), primaryGraphPaint);
			
			//  Wind speeds graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getWindSpeed);
			secondCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getWindGustSpeed);
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Precipitations graph
			this.precipitationsGraph = generateBitmapPrecipitationsGraphPath(
					  hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getRain),
					  hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getSnow),
					  hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastList, HourlyWeatherForecast::getPop),
					  this.width, dpToPx(40), tertiaryGraphPaint, primaryGraphPaint, popBarGraphPaint);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
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
	 * drawStructureAndDate(@NonNull Canvas canvas, @NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to draw principal elements of the view such as date, day moments and separators
	 * </p>
	 *
	 * @param canvas                    Elements will be drawn on it
	 * @param hourlyWeatherForecastList Where the name of the day will be drawn on y axis
	 * @param timeZone                  The timeZone of the place
	 */
	private void drawStructureAndDate(@NonNull Canvas canvas, @NonNull List<HourlyWeatherForecast> hourlyWeatherForecastList, @NonNull TimeZone timeZone) {
		byte previousItemDay = 0;
		byte currentItemDay;
		int xDiv = 0;
		int dateFirstLineY, dateSecondLineY, hourLineY, halfColumnWidth;
		Calendar calendar;
		Date date;
		
		calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTimeInMillis(hourlyWeatherForecastList.get(0).getDt());
		
		dateFirstLineY = dpToPx(15);
		dateSecondLineY = dpToPx(35);
		hourLineY = dpToPx(60);
		halfColumnWidth = columnWidth / 2;
		
		for (int index = 0; index < hourlyWeatherForecastList.size(); index++) {
			
			date = new Date(hourlyWeatherForecastList.get(index).getDt());
			
			calendar.setTimeInMillis(hourlyWeatherForecastList.get(index).getDt());
			currentItemDay = BigDecimal.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).byteValue();
			
			//  New day detected, draw day div and date
			if (previousItemDay != currentItemDay) {
				previousItemDay = currentItemDay;
				canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.datePaint);
				canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), xDiv + 10F, dateFirstLineY, this.datePaint);
				canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), xDiv + 10F, dateSecondLineY, this.datePaint);
			}
			//  Draw hour div
			else {
				canvas.drawLine(xDiv, 120, xDiv, canvas.getHeight(), this.structurePaint);
			}
			//  Draw hour
			canvas.drawText(formattingService.getFormattedHour(date, timeZone), xDiv + halfColumnWidth, hourLineY, this.structurePaint);
			
			xDiv += columnWidth;
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
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.getTemperature(), false), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.getTemperatureFeelsLike(), false), middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
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
		canvas.drawText(formattingService.getFormattedPressure(currentHourlyWeatherForecast.getPressure(), false), middleOfColumnX, y, this.primaryPaint);
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
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.getDewPoint(), false), middleOfColumnX, y, this.primaryPaint);
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
		canvas.drawText(formattingService.getFloatFormattedDistance(currentHourlyWeatherForecast.getVisibility(), true), middleOfColumnX, y, this.primaryPaint);
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
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.getWindSpeed(), true), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.getWindGustSpeed(), true), middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
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
		drawWindDirectionIcon(canvas, windDirection, left + dpToPx(5), top, width - dpToPx(15));

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
	 * @return A Bitmap with the generated curve, with the wanted height and width and in the ARGB_8888 format
	 */
	private Bitmap generateBitmap1CurvesGraphPath(float[] curveData, @Px int width, @Px int height, @NonNull Paint curvePaint) {
		//  Initializing graph path
		Path curvePath = new Path();

		//  Searching for max and min values in array
		float minValue = curveData[0],
				maxValue = curveData[0];

		for (float curveDatum : curveData) {
			if (curveDatum > maxValue) maxValue = curveDatum;
			if (curveDatum < minValue) minValue = curveDatum;
		}
		
		//  Find the value to adjust all values so that the minimum is 0
		float addValueMinTo0 = minValue * (-1F);
		//  Find scale factor of Y axis
		float scaleFactorY = 1 / (maxValue + addValueMinTo0);
		
		//  Doing Bezier curve calculations for each curve
		float connectionPointsX, point1X, point1Y, point2Y, point2X;
		float columnWidth = width / (float) curveData.length;
		float halfColumnWidth = columnWidth / 2F;
		//  To avoid curve trimming
		float top = 4F;
		float bottom = height - 4F;
		float drawHeight = bottom - top;
		
		//  Clear each paths
		curvePath.reset();
		
		//  If min and max values are the same, the resulting curve is a flat curve
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1X = halfColumnWidth;
			point1Y = bottom - drawHeight * (curveData[0] + addValueMinTo0) * scaleFactorY;
			
			curvePath.moveTo(0, point1Y);
			
			for (int index = 1; index < curveData.length; index++) {
				
				//  Position calculations
				point2X = index * this.columnWidth + halfColumnWidth;
				point2Y = bottom - drawHeight * (curveData[index] + addValueMinTo0) * scaleFactorY;
				
				//  Middle connection point
				connectionPointsX = (point1X + point2X) / 2F;
				
				//  Extending each curves
				curvePath.cubicTo(connectionPointsX, point1Y, connectionPointsX, point2Y, point2X, point2Y);
				
				//  Moving to new points to old points
				point1X = point2X;
				point1Y = point2Y;
			}
			
			curvePath.lineTo(width, point1Y);
			curvePath.moveTo(width, point1Y);
		} else {
			curvePath.moveTo(0, drawHeight);

			curvePath.lineTo(0, drawHeight);
			curvePath.lineTo(width, drawHeight);

			curvePath.moveTo(width, drawHeight);
		}
		// Close path
		curvePath.close();

		//  Generating returned Bitmap
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.getRain(), true), middleOfColumnX, y, this.tertiaryPaint);
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.getSnow(), true), middleOfColumnX, y + dpToPx(25), this.primaryPaint);
		
		canvas.drawText(String.format("%d %%", BigDecimal.valueOf(currentHourlyWeatherForecast.getPop() * 100).intValue()), middleOfColumnX, y + dpToPx(50), this.secondaryPaint);

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
		int halfWidthX = columnWidth / 2, drawableX = halfWidthX - dpToPx(25);
		
		drawStructureAndDate(canvas, hourlyWeatherForecastList, timeZone);
		
		for (int index = 0; index < hourlyWeatherForecastList.size(); index++) {
			currentHourlyWeatherForecast = hourlyWeatherForecastList.get(index);
			
			drawWeatherConditionIcons(canvas, currentHourlyWeatherForecast.getWeatherCode(), dpToPx(70), drawableX, dpToPx(50), dpToPx(50), isDayTime[index]);
			
			drawTemperatures(canvas, currentHourlyWeatherForecast, dpToPx(140), halfWidthX);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.getHumidity()), halfWidthX, dpToPx(245), this.tertiaryPaint);
			drawPressure(canvas, currentHourlyWeatherForecast, dpToPx(310), halfWidthX);
			
			drawUvIndex(canvas, currentHourlyWeatherForecast.getUvIndex(), drawableX, dpToPx(360), dpToPx(50));
			
			drawDewPoint(canvas, currentHourlyWeatherForecast, dpToPx(435), halfWidthX);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.getCloudiness()), halfWidthX, dpToPx(465), this.primaryPaint);
			drawVisibility(canvas, currentHourlyWeatherForecast, dpToPx(495), halfWidthX);
			
			drawWindSpeed(canvas, currentHourlyWeatherForecast, dpToPx(530), halfWidthX);
			drawWindDirection(canvas, currentHourlyWeatherForecast.getWindDirection(), dpToPx(620), drawableX, dpToPx(50));
			
			drawPrecipitations(canvas, currentHourlyWeatherForecast, dpToPx(720), halfWidthX);
			
			halfWidthX += columnWidth;
			drawableX += columnWidth;
		}

		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(175), null);
		canvas.drawBitmap(this.humidityGraph, 0, dpToPx(255), null);
		canvas.drawBitmap(this.pressureGraph, 0, dpToPx(320), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(565), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(780), null);
	}
}
