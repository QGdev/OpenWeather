/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.toberemoved.customview;

import static fr.qgdev.openweather.repositories.FormattingService.FormattingSpec.UNIT_AND_SPACE;
import static fr.qgdev.openweather.repositories.FormattingService.FormattingSpec.UNIT_BUT_NO_SPACE;

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
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.data.models.DailyForecast;
import fr.qgdev.openweather.data.models.HourlyForecast;
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
	
	private List<HourlyForecast> HourlyForecastList;
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
		if (!isInEditMode()) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
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
		if (!isInEditMode()) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}
	
	
	/**
	 * generateIsDayTimeArray(@NonNull ArrayList<HourlyForecast> HourlyForecastList, @NonNull ArrayList<DailyForecast> DailyForecastList)
	 * <p>
	 * Used to generate dayTime array which describe if it is day time for each hours of HourlyForecastList
	 * </p>
	 *
	 * @param HourlyForecastList ArrayList of HourlyForecasts
	 * @param DailyForecastList  ArrayList of DailyForecasts
	 * @return The generated array
	 */
	private boolean[] generateIsDayTimeArray(@NonNull List<HourlyForecast> HourlyForecastList, @NonNull List<DailyForecast> DailyForecastList) {
		
		HourlyForecast HourlyForecast;
		DailyForecast DailyForecast;
		boolean[] isDayTimeArray = new boolean[HourlyForecastList.size()];
		long previousItemDay;
		long currentItemDay;
		int dayIndex = 0;
		Calendar calendar;
		
		//  Start by the beginning of each arraylist
		
		HourlyForecast = HourlyForecastList.get(0);
		DailyForecast = DailyForecastList.get(0);
		
		//  Initialization of calendar
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(HourlyForecast.getDt());
		
		/*  Initialization of the previousItemDay
		 *       It is the ID of a day in a year, each day have an unique ID.
		 *       The day, 21th August 2021 will doesn't have the same ID as a 21th August 2020 or 2019.
		 *       The ID is composed of:
		 *           YYYYDDD
		 *               -   YYYY    :   Year of the day (2021...)
		 *               -   DDD     :   Day number in the whole year (223 for the 21th august)
		 * */
		previousItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;
		
		
		for (int index = 0; index < HourlyForecastList.size(); index++) {
			
			HourlyForecast = HourlyForecastList.get(index);
			calendar.setTimeInMillis(HourlyForecast.getDt());
			
			currentItemDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000L;
			
			//  New day detected, switching to the new day by incrementing the counter (dayIndex) by one and updating DailyForecast variable
			if (previousItemDay < currentItemDay) {
				previousItemDay = currentItemDay;
				dayIndex++;
				DailyForecast = DailyForecastList.get(dayIndex);
			}
			
			isDayTimeArray[index] = DailyForecast.getSunriseDt() < HourlyForecast.getDt() && HourlyForecast.getDt() < DailyForecast.getSunsetDt();
		}
		
		return isDayTimeArray;
	}
	
	
	/**
	 * HourlyForecastArrayToSelectedAttributeFloatArray(@NonNull ArrayList<HourlyForecast> HourlyForecastList, @NonNull String selectedAttribute) throws NoSuchFieldException, IllegalAccessException
	 * <p>
	 * Used to get an array of selected attribute of an ArrayList of HourlyForecast objects
	 * </p>
	 *
	 * @param HourlyForecastList The HourlyForecast arrayList
	 * @param attributeGetter           Used to provide the needed attribute
	 * @return The created array filled with all attributes
	 */
	private float[] HourlyForecastArrayToSelectedAttributeFloatArray(@NonNull List<HourlyForecast> HourlyForecastList, ParameterizedCallable<HourlyForecast, Number> attributeGetter) {
		float[] returnedAttributeArray = new float[HourlyForecastList.size()];
		
		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = attributeGetter.call(HourlyForecastList.get(index)).floatValue();
		}
		
		return returnedAttributeArray;
	}
	
	
	/**
	 * initialization(@NonNull ArrayList<HourlyForecast> HourlyForecastArrayList, @NonNull ArrayList<DailyForecast> DailyForecastArrayList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to initialize attributes used to draw a view
	 * </p>
	 *
	 * @param HourlyForecastList ArrayList of HourlyForecast
	 * @param DailyForecastList  ArrayList of DailyForecasts
	 * @param timeZone                  TimeZone of the place
	 * @param unitsFormattingService    FormattingService of the application to format dates
	 */
	public void initialization(@NonNull List<HourlyForecast> HourlyForecastList, @NonNull List<DailyForecast> DailyForecastList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone) {
		this.columnWidth = dpToPx(90);
		float[] firstCurve;
		float[] secondCurve;
		
		this.width = HourlyForecastList.size() * columnWidth;
		this.height = dpToPx(820);
		
		this.HourlyForecastList = HourlyForecastList;
		
		formattingService = unitsFormattingService;
		
		this.timeZone = timeZone;
		
		isDayTime = generateIsDayTimeArray(HourlyForecastList, Collections.unmodifiableList(DailyForecastList));
		
		
		try {
			//  Temperatures graph
			firstCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getTemperature);
			secondCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getTemperatureFeelsLike);
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Humidity graph
			firstCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getHumidity);
			this.humidityGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), tertiaryGraphPaint);
			
			//  Pressure graph
			firstCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getPressure);
			this.pressureGraph = generateBitmap1CurvesGraphPath(firstCurve, this.width, dpToPx(30), primaryGraphPaint);
			
			//  Wind speeds graph
			firstCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getWindSpeed);
			secondCurve = HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getWindGustSpeed);
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Precipitations graph
			this.precipitationsGraph = generateBitmapPrecipitationsGraphPath(
					  HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getRain),
					  HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getSnow),
					  HourlyForecastArrayToSelectedAttributeFloatArray(HourlyForecastList, HourlyForecast::getPop),
					  this.width, dpToPx(40), tertiaryGraphPaint, primaryGraphPaint, popBarGraphPaint);
			
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	/**
	 * drawStructureAndDate(@NonNull Canvas canvas, @NonNull List<HourlyForecast> HourlyForecastList, @NonNull TimeZone timeZone)
	 * <p>
	 * Used to draw principal elements of the view such as date, day moments and separators
	 * </p>
	 *
	 * @param canvas                    Elements will be drawn on it
	 * @param HourlyForecastList Where the name of the day will be drawn on y axis
	 * @param timeZone                  The timeZone of the place
	 */
	private void drawStructureAndDate(@NonNull Canvas canvas, @NonNull List<HourlyForecast> HourlyForecastList, @NonNull TimeZone timeZone) {
		byte previousItemDay = 0;
		byte currentItemDay;
		int xDiv = 0;
		int dateFirstLineY;
		int dateSecondLineY;
		int hourLineY;
		int halfColumnWidth;
		Calendar calendar;
		Date date;
		
		calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTimeInMillis(HourlyForecastList.get(0).getDt());
		
		dateFirstLineY = dpToPx(15);
		dateSecondLineY = dpToPx(35);
		hourLineY = dpToPx(60);
		halfColumnWidth = columnWidth / 2;
		
		for (int index = 0; index < HourlyForecastList.size(); index++) {
			
			date = new Date(HourlyForecastList.get(index).getDt());
			
			calendar.setTimeInMillis(HourlyForecastList.get(index).getDt());
			currentItemDay = BigDecimal.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).byteValue();
			
			//  New day detected, draw day div and date
			if (previousItemDay != currentItemDay) {
				previousItemDay = currentItemDay;
				canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.datePaint);
				canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone),
						  xDiv + 10F, dateFirstLineY, this.datePaint);
				canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone),
						  xDiv + 10F, dateSecondLineY, this.datePaint);
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
	 * drawTemperatures(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw temperatures and feel like temperatures
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where temperatures will be drawn on the y axis
	 * @param middleOfColumnX              Where temperatures will be drawn on the x axis
	 */
	private void drawTemperatures(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyForecast.getTemperature(), UNIT_BUT_NO_SPACE),
				  middleOfColumnX, y, this.primaryPaint);
		
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyForecast.getTemperatureFeelsLike(), UNIT_BUT_NO_SPACE),
				  middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
	}
	
	
	/**
	 * drawPressure(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw pressure
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where pressure will be drawn on the y axis
	 * @param middleOfColumnX              Where pressure will be drawn on the x axis
	 */
	private void drawPressure(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFormattedPressure(currentHourlyForecast.getPressure(), UNIT_BUT_NO_SPACE),
				  middleOfColumnX, y, this.primaryPaint);
	}
	
	
	/**
	 * drawDewPoint(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw dewPoint
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where dewPoint will be drawn on the y axis
	 * @param middleOfColumnX              Where dewPoint will be drawn on the x axis
	 */
	private void drawDewPoint(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyForecast.getDewPoint(), UNIT_BUT_NO_SPACE),
				  middleOfColumnX, y, this.primaryPaint);
	}
	
	
	/**
	 * drawVisibility(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw visibility distance
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where visibility distance will be drawn on the y axis
	 * @param middleOfColumnX              Where visibility distance will be drawn on the x axis
	 */
	private void drawVisibility(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedDistance(currentHourlyForecast.getVisibility(), UNIT_AND_SPACE),
				  middleOfColumnX, y, this.primaryPaint);
	}
	
	
	/**
	 * drawWindSpeed(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw wind speed
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where wind speed will be drawn on the y axis
	 * @param middleOfColumnX              Where wind speed will be drawn on the x axis
	 */
	private void drawWindSpeed(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyForecast.getWindSpeed(), UNIT_AND_SPACE),
				  middleOfColumnX, y, this.primaryPaint);
		
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyForecast.getWindGustSpeed(), UNIT_AND_SPACE),
				  middleOfColumnX, y + dpToPx(25), this.secondaryPaint);
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
	private void drawWindDirection(@NonNull Canvas canvas, int windDirection, @Px int top, @Px int left, @Px int width) {
		int middle = width / 2;
		drawWindDirectionIcon(canvas, windDirection, left + dpToPx(5), top, width - dpToPx(15));
		
		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(windDirection),
				  left + middle, top + width + dpToPx(5), this.primaryPaint);
		
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(windDirection),
				  left + middle, top + width + dpToPx(25), this.primaryPaint);
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
		float connectionPointsX;
		float point1X;
		float point1Y;
		float point2Y;
		float point2X;
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
	 * drawPrecipitations((@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw precipitations (rain, snow, pop)
	 * </p>
	 *
	 * @param canvas                       Elements will be drawn on it
	 * @param currentHourlyForecast Where data will be taken
	 * @param y                            Where precipitations will be drawn on the y axis
	 * @param middleOfColumnX              Where precipitations will be drawn on the x axis
	 */
	private void drawPrecipitations(@NonNull Canvas canvas, @NonNull HourlyForecast currentHourlyForecast, @Px int y, @Px int middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyForecast.getRain(), UNIT_AND_SPACE),
				  middleOfColumnX, y, this.tertiaryPaint);
		
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyForecast.getSnow(), UNIT_AND_SPACE),
				  middleOfColumnX, y + dpToPx(25), this.primaryPaint);
		
		int convertedPopValue = BigDecimal.valueOf(currentHourlyForecast.getPop() * 100).intValue();
		canvas.drawText(String.format(Locale.US, "%d %%", convertedPopValue),
				  middleOfColumnX, y + dpToPx(50), this.secondaryPaint);
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
		
		HourlyForecast currentHourlyForecast;
		int halfWidthX = columnWidth / 2;
		int drawableX = halfWidthX - dpToPx(25);
		
		drawStructureAndDate(canvas, HourlyForecastList, timeZone);
		
		for (int index = 0; index < HourlyForecastList.size(); index++) {
			currentHourlyForecast = HourlyForecastList.get(index);
			
			drawWeatherConditionIcons(context,
					  canvas,
					  currentHourlyForecast.getWeatherCode(),
					  dpToPx(70), drawableX, dpToPx(50), dpToPx(50), isDayTime[index]);
			
			drawTemperatures(canvas,
					  currentHourlyForecast,
					  dpToPx(140), halfWidthX);
			
			canvas.drawText(String.format(Locale.US, "%d%%", currentHourlyForecast.getHumidity()),
					  halfWidthX, dpToPx(245), this.tertiaryPaint);
			
			drawPressure(canvas,
					  currentHourlyForecast,
					  dpToPx(310), halfWidthX);
			
			drawUvIndex(canvas,
					  currentHourlyForecast.getUvIndex(),
					  drawableX, dpToPx(360), dpToPx(50));
			
			drawDewPoint(canvas,
					  currentHourlyForecast,
					  dpToPx(435), halfWidthX);
			
			canvas.drawText(String.format(Locale.US, "%d%%", currentHourlyForecast.getCloudiness()),
					  halfWidthX, dpToPx(465), this.primaryPaint);
			
			drawVisibility(canvas,
					  currentHourlyForecast,
					  dpToPx(495), halfWidthX);
			
			drawWindSpeed(canvas,
					  currentHourlyForecast,
					  dpToPx(530), halfWidthX);
			
			drawWindDirection(canvas,
					  currentHourlyForecast.getWindDirection(),
					  dpToPx(620), drawableX, dpToPx(50));
			
			drawPrecipitations(canvas,
					  currentHourlyForecast,
					  dpToPx(720), halfWidthX);
			
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
