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
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.data.models.DailyForecast;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.utils.ParameterizedCallable;


/**
 * DailyForecastGraphView
 * <p>
 * Used to generate graphics for daily forecasts
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see ForecastView
 */
public class DailyForecastGraphView extends ForecastView {
	
	private static final String TAG = DailyForecastGraphView.class.getSimpleName();
	private final Logger logger = Logger.getLogger(TAG);
	
	private final int columnWidth = dpToPx(280);
	private final int halfColumnWidth = columnWidth / 2;
	private final int quarterColumnWidth = columnWidth / 4;
	private final int sixthColumnWidth = columnWidth / 6;
	private List<DailyForecast> DailyForecastList;
	
	/**
	 * DailyForecastGraphView Constructor
	 * <p>
	 * Just build DailyForecastGraphView object only with context
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 */
	public DailyForecastGraphView(@NonNull Context context) {
		super(context);
		initComponents(context);
	}
	
	
	/**
	 * DailyForecastGraphView Constructor
	 * <p>
	 * Just build DailyForecastGraphView object only with Context and AttributeSet
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 * @param attrs   AttributeSet for the GraphView
	 */
	public DailyForecastGraphView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initComponents(context);
		if (!isInEditMode()) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}
	
	
	/**
	 * initComponents(@NonNull Context context)
	 * <p>
	 * Used to initialize attributes used to draw a graph
	 * </p>
	 *
	 * @param context Current context, only used to construct super class and initialize attributes
	 */
	@Override
	protected void initComponents(@NonNull Context context) {
		super.initComponents(context);
		if (!isInEditMode()) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		
		this.context = context;
		
		this.moonLightIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.moonShadowIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		this.moonLightIconPaint.setColor(getResources().getColor(R.color.colorMoonLight, null));
		this.moonLightIconPaint.setStrokeWidth(5);
		this.moonLightIconPaint.setAlpha(255);
		this.moonLightIconPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		this.moonLightIconPaint.setTextAlign(Paint.Align.CENTER);
		
		this.moonShadowIconPaint.setColor(getResources().getColor(R.color.colorMoonShadow, null));
		this.moonShadowIconPaint.setStrokeWidth(5);
		this.moonShadowIconPaint.setAlpha(255);
		this.moonShadowIconPaint.setStyle(Paint.Style.STROKE);
		this.moonShadowIconPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	
	/**
	 * initialization(@NonNull ArrayList<DailyForecast> DailyForecastList, TimeZone timeZone, FormattingService unitsFormattingService)
	 * <p>
	 * Used to initialize attributes used to draw a view
	 * </p>
	 *
	 * @param DailyForecastList ArrayList of DailyForecasts
	 * @param timeZone                 TimeZone of the place
	 * @param unitsFormattingService   FormattingService of the application to format dates
	 */
	public void initialization(@NonNull List<DailyForecast> DailyForecastList, @NonNull TimeZone timeZone, @NonNull FormattingService unitsFormattingService) {
		this.width = DailyForecastList.size() * columnWidth;
		this.height = dpToPx(710);
		
		this.DailyForecastList = DailyForecastList;
		this.timeZone = timeZone;
		this.formattingService = unitsFormattingService;
		
		try {
			//  Temperatures graph
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(
					  extractTemperaturesFromDailyForecastList(DailyForecastList),
					  extractFeelsLikeTemperaturesFromDailyForecastList(DailyForecastList),
					  this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Wind speeds graph
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(
					  DailyForecastArrayToSelectedAttributeFloatArray(DailyForecastList, DailyForecast::getWindSpeed),
					  DailyForecastArrayToSelectedAttributeFloatArray(DailyForecastList, DailyForecast::getWindGustSpeed),
					  this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
			
			//  Precipitations graph
			this.precipitationsGraph = generateBitmapPrecipitationsGraphPath(
					  DailyForecastArrayToSelectedAttributeFloatArray(DailyForecastList, DailyForecast::getRain),
					  DailyForecastArrayToSelectedAttributeFloatArray(DailyForecastList, DailyForecast::getSnow),
					  DailyForecastArrayToSelectedAttributeFloatArray(DailyForecastList, DailyForecast::getPop),
					  this.width, dpToPx(50), tertiaryGraphPaint, primaryGraphPaint, popBarGraphPaint);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}
	
	
	/**
	 * DailyForecastArrayToSelectedAttributeFloatArray(ArrayList<DailyForecast> DailyForecastList, String selectedAttribute) throws NoSuchFieldException, IllegalAccessException
	 * <p>
	 * Used to get an array of selected attribute of an ArrayList of DailyForecast objects
	 * </p>
	 *
	 * @param DailyForecastList The DailyForecast arrayList
	 * @param attributeGetter          Used to provide the needed attribute
	 * @return The created array filled with all attributes
	 */
	private float[] DailyForecastArrayToSelectedAttributeFloatArray(@NonNull List<DailyForecast> DailyForecastList, @NonNull ParameterizedCallable<DailyForecast, Number> attributeGetter) {
		float[] returnedAttributeArray = new float[DailyForecastList.size()];
		
		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = attributeGetter.call(DailyForecastList.get(index)).floatValue();
		}
		
		return returnedAttributeArray;
	}
	
	
	/**
	 * extractTemperaturesFromDailyForecastArrayList(ArrayList<DailyForecast> DailyForecastList)
	 * <p>
	 * Used to get all temperatures attributes of a DailyWeather object<br>
	 * Sorted Day after Day, newest day first and oldest day last, day is composed of morning, day, evening and night temperatures
	 * </p>
	 *
	 * @param DailyForecastList The dailyForecast arrayList
	 * @return The created array filled with all temperature attributes
	 */
	private float[] extractTemperaturesFromDailyForecastList(@NonNull List<DailyForecast> DailyForecastList) {
		float[] returnedAttributeArray = new float[DailyForecastList.size() * 4];
		int arrayIndex = 0;
		
		for (DailyForecast DailyForecast : DailyForecastList) {
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureMorning();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureDay();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureEvening();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureNight();
		}
		return returnedAttributeArray;
	}
	
	
	/**
	 * extractFeelsLikeTemperaturesFromDailyForecastArrayList(ArrayList<DailyForecast> DailyForecastArrayList)
	 * <p>
	 * Used to get all FeelsLike temperatures attributes of a DailyWeather object<br>
	 * Sorted Day after Day, newest day first and oldest day last, day is composed of morning, day, evening and night FeelsLike temperatures
	 * </p>
	 *
	 * @param DailyForecastList The dailyForecast arrayList
	 * @return The created array filled with all temperature attributes
	 */
	private float[] extractFeelsLikeTemperaturesFromDailyForecastList(@NonNull List<DailyForecast> DailyForecastList) {
		float[] returnedAttributeArray = new float[DailyForecastList.size() * 4];
		int arrayIndex = 0;
		
		for (DailyForecast DailyForecast : DailyForecastList) {
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureMorningFeelsLike();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureDayFeelsLike();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureEveningFeelsLike();
			returnedAttributeArray[arrayIndex++] = DailyForecast.getTemperatureNightFeelsLike();
		}
		return returnedAttributeArray;
	}
	
	
	/**
	 * drawStructureAndDate(@NonNull Canvas canvas, float dateFirstLineY, float dateSecondLineY, float dayPeriodStartLineY, float dayPeriodStopLineY, ArrayList<DailyForecast> DailyForecastList, TimeZone timeZone)
	 * <p>
	 * Used to draw principal elements of the view such as date, day moments and separators
	 * </p>
	 *
	 * @param canvas                   Elements will be drawn on it
	 * @param dateFirstLineY           Where the name of the day will be drawn on y axis
	 * @param dateSecondLineY          Where the day number and the month number of the day will be drawn on y axis
	 * @param dayPeriodStartLineY      Where the separator between day moments start on y axis
	 * @param dayPeriodStopLineY       Where the separator between day moments ends on y axis
	 * @param DailyForecastList The dailyForecast arrayList
	 */
	private void drawStructureAndDate(@NonNull Canvas canvas, @Px int dateFirstLineY, @Px int dateSecondLineY, @Px int dayPeriodStartLineY, @Px int dayPeriodStopLineY, @NonNull List<DailyForecast> DailyForecastList, @NonNull TimeZone timeZone) {
		float xDiv = 0;
		float xDivDayPeriod = quarterColumnWidth / 2F;
		Date date;
		
		//  For each day
		for (int index = 0; index < DailyForecastList.size(); index++) {
			
			date = new Date(DailyForecastList.get(index).getDt());
			
			//  Draw date
			canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), xDiv + 10F, dateFirstLineY, this.datePaint);
			canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), xDiv + 10F, dateSecondLineY, this.datePaint);
			
			//  Draw day separator
			canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.structurePaint);
			
			//  Draw separator between each day moments
			for (int i = 1; i < 4; i++)
				canvas.drawLine(xDiv + quarterColumnWidth * i, dayPeriodStartLineY, xDiv + quarterColumnWidth * i, dayPeriodStopLineY, this.structurePaint);
			
			canvas.drawText(context.getString(R.string.title_daily_forecast_morning), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += quarterColumnWidth;
			canvas.drawText(context.getString(R.string.title_daily_forecast_noon), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += quarterColumnWidth;
			canvas.drawText(context.getString(R.string.title_daily_forecast_evening), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += quarterColumnWidth;
			canvas.drawText(context.getString(R.string.title_daily_forecast_night), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			
			xDiv += columnWidth;
			xDivDayPeriod = quarterColumnWidth / 2F;
		}
	}
	
	
	/**
	 * drawMaxMinTemperatures(@NonNull Context context, @NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw min and max temperatures of the day
	 * </p>
	 *
	 * @param context              Context in order to retrieve drawables
	 * @param canvas               Elements will be drawn on it
	 * @param DailyForecast Where data will be taken
	 * @param top                  Where temperatures will be drawn on the y axis
	 * @param left                 Where temperatures will be drawn on the x axis
	 */
	private void drawMaxMinTemperatures(@NonNull Context context, @NonNull Canvas canvas, DailyForecast DailyForecast, int top, int left) {
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.temperature_maximum_material),
				  formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureMaximum(), UNIT_AND_SPACE),
				  top,
				  left,
				  dpToPx(10),
				  this.secondaryPaint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.temperature_minimum_material),
				  formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureMinimum(), UNIT_AND_SPACE),
				  top + dpToPx(30),
				  left,
				  dpToPx(10),
				  this.tertiaryPaint);
	}
	
	
	/**
	 * drawTemperatures(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw temperatures and feel like temperatures of each day moment
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param DailyForecast Where data will be taken
	 * @param top                  Where temperatures will be drawn on the y axis
	 * @param left                 Where temperatures will be drawn on the x axis
	 */
	private void drawTemperatures(@NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left) {
		float textX = left + quarterColumnWidth / 2F;
		float textY1 = top;
		float textY2 = top + dpToPx(25);
		//  Temperatures
		////    Morning
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureMorning(), UNIT_BUT_NO_SPACE),
				  textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureMorningFeelsLike(), UNIT_BUT_NO_SPACE),
				  textX, textY2, this.secondaryPaint);
		textX += quarterColumnWidth;
		////    Noon
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureDay(), UNIT_BUT_NO_SPACE),
				  textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureDayFeelsLike(), UNIT_BUT_NO_SPACE),
				  textX, textY2, this.secondaryPaint);
		textX += quarterColumnWidth;
		////    Evening
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureEvening(), UNIT_BUT_NO_SPACE),
				  textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureEveningFeelsLike(), UNIT_BUT_NO_SPACE),
				  textX, textY2, this.secondaryPaint);
		textX += quarterColumnWidth;
		////    Night
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureNight(), UNIT_BUT_NO_SPACE),
				  textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(DailyForecast.getTemperatureNightFeelsLike(), UNIT_BUT_NO_SPACE),
				  textX, textY2, this.secondaryPaint);
	}
	
	
	/**
	 * drawMoonPhase(@NonNull Canvas canvas, float moonPhase, float x, float y, float sideLength)
	 * <p>
	 * Used to draw moon phase
	 * </p>
	 *
	 * @param canvas     Moon phase will be drawn on it
	 * @param moonPhase  The moon phase coefficient
	 * @param x          Center of the drawn moon phase on the x axis
	 * @param y          Center of the drawn moon phase on the y axis
	 * @param sideLength Length side of the drawn moon phase
	 */
	private void drawMoonPhase(@NonNull Canvas canvas, float moonPhase, @Px int x, @Px int y, @Px int sideLength) {
		float middle = sideLength / 2F;
		float circleRadius = (middle / 2F) - 3;
		
		Bitmap moonBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_8888);
		Canvas moonCanvas = new Canvas(moonBitmap);
		
		//  Draw a full visible moon
		moonCanvas.drawCircle(middle, middle, circleRadius, this.moonLightIconPaint);
		moonCanvas.drawCircle(middle, middle, circleRadius + 1, this.moonShadowIconPaint);
		
		//  No need to draw shadows part
		if (moonPhase != 0.5F) {
			int startX;
			int stopX;
			int dX;
			float left = middle - circleRadius;
			float right = middle + circleRadius;
			float top2 = left;
			float bottom2 = right;
			
			//  Delimitation between light and shadow parts
			dX = BigDecimal.valueOf(circleRadius * 2).intValue();
			dX *= (moonPhase * 2) % 1;
			
			//  First half of the moon cycle
			if (moonPhase < 0.5F) {
				startX = dX;
				stopX = BigDecimal.valueOf(circleRadius * 2).intValue();
				
			}
			//  Last half of the moon cycle
			else {
				startX = 0;
				stopX = dX;
			}
			
			//  Draw shadow part
			for (int i = startX; i <= stopX; i++) {
				if (left + i < middle)  //  First half
					moonCanvas.drawArc(left + i, top2, right - i, bottom2, -90, 180, false, this.moonShadowIconPaint);
				else                    //  Last haft
					moonCanvas.drawArc(right - i, top2, left + i, bottom2, 90, 180, false, this.moonShadowIconPaint);
			}
		}
		canvas.drawBitmap(moonBitmap, x, y, null);
	}
	
	
	/**
	 * drawEnvironmentalVariables(@NonNull Context context, @NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left, Paint paint)
	 * <p>
	 * Used to draw environmental variables such as pressure, cloudiness, humidity, dewPoint, sunrise, sunset, UVIndex Icon, moonrise, moonset and moonphase Icon
	 * </p>
	 *
	 * @param context              Context is used to retrieve drawables
	 * @param canvas               Elements will be drawn on it
	 * @param DailyForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 * @param paint                Paint used to draw text elements
	 */
	private void drawEnvironmentalVariables(@NonNull Context context, @NonNull Canvas canvas, @NonNull DailyForecast DailyForecast, @Px int top, @Px int left, @NonNull Paint paint) {
		int firstColumn = left + dpToPx(20);
		int secondColumn = firstColumn + halfColumnWidth;
		int textY1 = top;
		int textY2 = textY1 + dpToPx(35);
		int textY3 = textY2 + dpToPx(35);
		int textY4 = textY3 + dpToPx(35);
		int textY5 = textY4 + dpToPx(35);
		int textY6 = textY5 + dpToPx(35);
		
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.barometer_material),
				  formattingService.getFormattedPressure(DailyForecast.getPressure(), UNIT_AND_SPACE),
				  textY1, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.cloudy_material),
				  String.format(Locale.US, "%d %%", DailyForecast.getCloudiness()),
				  textY1, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.humidity_material),
				  String.format(Locale.US, "%d %%", DailyForecast.getHumidity()),
				  textY2, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.dew_point_material),
				  formattingService.getFloatFormattedTemperature(DailyForecast.getDewPoint(), UNIT_AND_SPACE),
				  textY2, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.sunrise_material),
				  formattingService.getFormattedTime(new Date(DailyForecast.getSunriseDt()), timeZone),
				  textY3, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.sunset_material),
				  formattingService.getFormattedTime(new Date(DailyForecast.getSunsetDt()), timeZone),
				  textY4, firstColumn, dpToPx(5), paint);
		
		drawUvIndex(canvas, DailyForecast.getUvIndex(), secondColumn, textY3, dpToPx(70));
		
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.moonrise_material),
				  formattingService.getFormattedTime(new Date(DailyForecast.getMoonriseDt()), timeZone),
				  textY5, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.moonset_material),
				  formattingService.getFormattedTime(new Date(DailyForecast.getMoonsetDt()), timeZone),
				  textY6, firstColumn, dpToPx(5), paint);
		
		drawMoonPhase(canvas, DailyForecast.getMoonPhase(), secondColumn, textY5, dpToPx(70));
	}
	
	
	/**
	 * drawWindVariables(@NonNull Context context, @NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw wind variables (wind speed, wind gust speed, wind direction)
	 * </p>
	 *
	 * @param context              Context is used to retrieve drawables
	 * @param canvas               Elements will be drawn on it
	 * @param DailyForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 */
	private void drawWindVariables(@NonNull Context context, @NonNull Canvas canvas, @NonNull DailyForecast DailyForecast, @Px int top, @Px int left) {
		int firstColumn = left + dpToPx(20);
		int secondColumn = firstColumn + halfColumnWidth;
		int textY = top + dpToPx(30);
		int textY2 = textY + dpToPx(40);
		int textY3 = textY2 + dpToPx(20);
		
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.windsock_material),
				  formattingService.getFloatFormattedSpeed(DailyForecast.getWindSpeed(), UNIT_AND_SPACE),
				  textY, firstColumn, dpToPx(5), this.primaryPaint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.wind_material),
				  formattingService.getFloatFormattedSpeed(DailyForecast.getWindGustSpeed(), UNIT_AND_SPACE),
				  textY, secondColumn, dpToPx(5), this.secondaryPaint);
		
		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(DailyForecast.getWindDirection()),
				  (float) left + quarterColumnWidth, textY2, this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(DailyForecast.getWindDirection()),
				  (float) left + quarterColumnWidth, textY3, this.primaryPaint);
		
		drawWindDirectionIcon(canvas,
				  DailyForecast.getWindDirection(),
				  left + halfColumnWidth + quarterColumnWidth - dpToPx(20),
				  textY + dpToPx(30), dpToPx(35));
	}
	
	
	/**
	 * drawPrecipitationsVariables(@NonNull Context context, @NonNull Canvas canvas, DailyForecast DailyForecast, float top, float left)
	 * <p>
	 * Used to draw wind variables (wind speed, wind gust speed, wind direction)
	 * </p>
	 *
	 * @param context              Context is used to retrieve drawables
	 * @param canvas               Elements will be drawn on it
	 * @param DailyForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 */
	private void drawPrecipitationsVariables(@NonNull Context context, @NonNull Canvas canvas, @NonNull DailyForecast DailyForecast, @Px int top, @Px int left) {
		int firstColumn = left + dpToPx(20);
		int secondColumn = firstColumn + halfColumnWidth;
		int textY = top;
		int textY2 = textY + dpToPx(35);
		
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.rain_material),
				  formattingService.getFloatFormattedShortDistance(DailyForecast.getRain(), UNIT_AND_SPACE),
				  textY, firstColumn, dpToPx(5), this.tertiaryPaint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.snow_material),
				  formattingService.getFloatFormattedShortDistance(DailyForecast.getSnow(), UNIT_AND_SPACE),
				  textY, secondColumn, dpToPx(5), this.primaryPaint);
		drawTextWithDrawable(canvas,
				  getDrawable(context, R.drawable.umbrella_material),
				  String.format(Locale.US, "%d %%", BigDecimal.valueOf(DailyForecast.getPop() * 100).intValue()),
				  textY2, firstColumn, dpToPx(5), this.secondaryPaint);
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
		
		int leftOfColumn = 0;
		int halfOfColumnWidth = halfColumnWidth;
		int sixthOfColumnWidth = sixthColumnWidth;
		
		drawStructureAndDate(canvas, dpToPx(15), dpToPx(35), dpToPx(125), dpToPx(230), DailyForecastList, timeZone);
		
		for (DailyForecast DailyForecast : DailyForecastList) {
			drawWeatherConditionIcons(context,
					  canvas,
					  DailyForecast.getWeatherCode(),
					  dpToPx(30), sixthOfColumnWidth, dpToPx(70), dpToPx(70));
			drawMaxMinTemperatures(context, canvas, DailyForecast, dpToPx(30), halfOfColumnWidth);
			
			drawTemperatures(canvas, DailyForecast, dpToPx(210), leftOfColumn);
			
			drawEnvironmentalVariables(context, canvas, DailyForecast, dpToPx(250), leftOfColumn, this.primaryPaint);
			drawWindVariables(context, canvas, DailyForecast, dpToPx(485), leftOfColumn);
			
			drawPrecipitationsVariables(context, canvas, DailyForecast, dpToPx(650), leftOfColumn);
			
			leftOfColumn += columnWidth;
			sixthOfColumnWidth += columnWidth;
			halfOfColumnWidth += columnWidth;
		}
		
		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(140), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(460), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(590), null);
	}
}
