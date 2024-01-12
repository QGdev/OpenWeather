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

package fr.qgdev.openweather.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.content.res.AppCompatResources;

import java.math.BigDecimal;
import java.util.TimeZone;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.repositories.FormattingService;


/**
 * ForecastView
 * <p>
 * Used to generate graphics for forecasts
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see View
 */
public abstract class ForecastView extends View {
	
	protected FormattingService formattingService;
	protected Context context;
	protected int width;
	protected int height;
	protected Bitmap temperaturesGraph;
	protected Bitmap humidityGraph;
	protected Bitmap pressureGraph;
	protected Bitmap windSpeedsGraph;
	protected Bitmap precipitationsGraph;
	protected TimeZone timeZone;
	protected Paint datePaint;
	protected Paint structurePaint;
	protected Paint primaryPaint;
	protected Paint secondaryPaint;
	protected Paint primaryGraphPaint;
	protected Paint secondaryGraphPaint;
	protected Paint tertiaryPaint;
	protected Paint tertiaryGraphPaint;
	protected Paint popBarGraphPaint;
	protected Paint iconsPaint;
	protected Paint sunIconPaint;
	protected Paint moonLightIconPaint;
	protected Paint moonShadowIconPaint;
	
	
	/**
	 * ForecastView Constructor
	 * <p>
	 * Just build ForecastView object only with context
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 */
	protected ForecastView(@NonNull Context context) {
		super(context);
		initComponents(context);
	}
	
	
	/**
	 * ForecastView Constructor
	 * <p>
	 * Just build ForecastView object only with Context and AttributeSet
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 * @param attrs   AttributeSet for the GraphView
	 */
	protected ForecastView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initComponents(context);
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
	
	
	//  Graph generation functions
	//______________________________________________________________________________________________
	
	/**
	 * generateBitmap2CurvesGraphPath(float[] firstCurveData, float[] secondCurveData, int width, int height, @NonNull Paint firstCurvePaint, @NonNull Paint secondCurvePaint)
	 * <p>
	 * Used to generate bitmap containing graph of two set of data
	 * </p>
	 *
	 * @param firstCurveData   Array of numerical values that will be used to draw the first curve
	 * @param secondCurveData  Array of numerical values that will be used to draw the second curve
	 * @param width            Width of the wanted graph
	 * @param height           Height of the wanted graph
	 * @param firstCurvePaint  Paint that will be used to draw first curve
	 * @param secondCurvePaint Paint that will be used to draw second curve
	 * @return A Bitmap with two generated curves, with the wanted height and width and in the ARGB_8888 format
	 * @apiNote firstCurveData & secondCurveData must have the same number of elements
	 */
	protected Bitmap generateBitmap2CurvesGraphPath(float[] firstCurveData, float[] secondCurveData, @Px int width, @Px int height, @NonNull Paint firstCurvePaint, @NonNull Paint secondCurvePaint) {
		
		//  Initializing graph paths
		Path firstCurvePath = new Path();
		Path secondCurvePath = new Path();
		
		//  Searching for max and min values in arrays
		float minValue = firstCurveData[0];
		float maxValue = firstCurveData[0];
		
		for (int index = 0; index < firstCurveData.length; index++) {
			if (firstCurveData[index] > maxValue) maxValue = firstCurveData[index];
			if (firstCurveData[index] < minValue) minValue = firstCurveData[index];
			
			if (secondCurveData[index] > maxValue) maxValue = secondCurveData[index];
			if (secondCurveData[index] < minValue) minValue = secondCurveData[index];
		}
		
		//  Find the value to adjust all values so that the minimum is 0
		float addValueMinTo0 = minValue * (-1F);
		//  Find scale factor of Y axis
		float scaleFactorY = 1 / (maxValue + addValueMinTo0);
		
		//  Doing Bezier curve calculations for each curves
		float connectionPointsX;
		float point1X;
		float point1Y;
		float point2Y;
		float point1Y2;
		float point2X;
		float point2Y2;
		float columnWidth = width / (float) firstCurveData.length;
		float halfColumnWidth = columnWidth / 2F;
		//  To avoid curve trimming
		float top = 4F;
		float bottom = height - 4F;
		float drawHeight = bottom - top;
		
		//  Clear each paths
		firstCurvePath.reset();
		secondCurvePath.reset();
		
		//  If min and max values are the same, the resulting curves are flat curves
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1X = halfColumnWidth;
			point1Y = bottom - drawHeight * (firstCurveData[0] + addValueMinTo0) * scaleFactorY;
			point1Y2 = bottom - drawHeight * (secondCurveData[0] + addValueMinTo0) * scaleFactorY;
			
			firstCurvePath.moveTo(0, point1Y);
			secondCurvePath.moveTo(0, point1Y2);
			
			for (int index = 1; index < firstCurveData.length; index++) {
				
				//  Position calculations
				point2X = index * columnWidth + halfColumnWidth;
				point2Y = bottom - drawHeight * (firstCurveData[index] + addValueMinTo0) * scaleFactorY;
				point2Y2 = bottom - drawHeight * (secondCurveData[index] + addValueMinTo0) * scaleFactorY;
				
				//  Middle connection point
				connectionPointsX = (point1X + point2X) / 2F;
				
				//  Extending each curves
				firstCurvePath.cubicTo(connectionPointsX, point1Y, connectionPointsX, point2Y, point2X, point2Y);
				secondCurvePath.cubicTo(connectionPointsX, point1Y2, connectionPointsX, point2Y2, point2X, point2Y2);
				
				//  Moving to new points to old points
				point1X = point2X;
				point1Y = point2Y;
				point1Y2 = point2Y2;
			}
			
			firstCurvePath.lineTo(width, point1Y);
			secondCurvePath.lineTo(width, point1Y2);
			firstCurvePath.moveTo(width, point1Y);
			secondCurvePath.moveTo(width, point1Y2);
		} else {
			firstCurvePath.moveTo(0, drawHeight);
			secondCurvePath.moveTo(0, drawHeight);
			
			firstCurvePath.lineTo(0, drawHeight);
			secondCurvePath.lineTo(0, drawHeight);
			firstCurvePath.lineTo(width, drawHeight);
			secondCurvePath.lineTo(width, drawHeight);
			
			firstCurvePath.moveTo(width, drawHeight);
			secondCurvePath.moveTo(width, drawHeight);
		}
		//  Close paths
		firstCurvePath.close();
		secondCurvePath.close();
		
		//  Generating returned Bitmap
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawPath(firstCurvePath, firstCurvePaint);
		canvas.drawPath(secondCurvePath, secondCurvePaint);
		
		
		return returnedBitmap;
	}
	
	
	/**
	 * generateBitmapPrecipitationsGraphPath(float[] rainData, float[] snowData, float[] popData, int width, int height, @NonNull Paint rainCurvePaint, @NonNull Paint snowCurvePaint, @NonNull Paint popCurvePaint)
	 * <p>
	 * Used to generate bitmap containing graph of three set of data, rain, sno and pop
	 * </p>
	 *
	 * @param rainData       Array of numerical values that will be used to draw the first curve
	 * @param snowData       Array of numerical values that will be used to draw the second curve
	 * @param popData        Array of float values between 0 and 1 that will be used to draw the bar graph
	 * @param width          Width of the wanted graph
	 * @param height         Height of the wanted graph
	 * @param rainCurvePaint Paint that will be used to draw first curve
	 * @param snowCurvePaint Paint that will be used to draw second curve
	 * @param popCurvePaint  Paint that will be used to draw third curve which is a bar graph
	 * @return A Bitmap with three generated curves, with the wanted height and width and in the ARGB_8888 format
	 * @apiNote rainData, snowData & popData must have the same number of elements
	 */
	protected Bitmap generateBitmapPrecipitationsGraphPath(float[] rainData, float[] snowData, float[] popData, @Px int width, @Px int height, @NonNull Paint rainCurvePaint, @NonNull Paint snowCurvePaint, @NonNull Paint popCurvePaint) {
		
		//  Initializing graph paths
		Path rainCurvePath = new Path();
		Path snowCurvePath = new Path();
		Path popCurvePath = new Path();
		
		//  Searching for max and min values in arrays
		float minValue = rainData[0];
		float maxValue = rainData[0];
		
		for (int index = 0; index < rainData.length; index++) {
			if (rainData[index] > maxValue) maxValue = rainData[index];
			if (rainData[index] < minValue) minValue = rainData[index];
			
			if (snowData[index] > maxValue) maxValue = snowData[index];
			if (snowData[index] < minValue) minValue = snowData[index];
		}
		
		//  Find the value to adjust all values so that the minimum is 0
		float addValueMinTo0 = minValue * (-1);
		//  Find scale factor of Y axis
		float scaleFactorY = 1 / (maxValue + addValueMinTo0);
		
		//  Doing Bezier curve calculations for each curves
		float connectionPointsX;
		float point1X;
		float point1Y;
		float point2Y;
		float point1Y2;
		float point2X;
		float point2Y2;
		float columnWidth = width / (float) rainData.length;
		float halfColumnWidth = columnWidth / 2F;
		float halfPopBarWidth = columnWidth / 6F;
		//  To avoid curve trimming
		float top = 4;
		float bottom = height - 4F;
		float drawHeight = bottom - top;
		
		//  Clear each paths
		rainCurvePath.reset();
		snowCurvePath.reset();
		
		//  If min and max values are the same, the resulting curves are flat curves
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1X = halfColumnWidth;
			point1Y = bottom - drawHeight * (rainData[0] + addValueMinTo0) * scaleFactorY;
			point1Y2 = bottom - drawHeight * (snowData[0] + addValueMinTo0) * scaleFactorY;
			
			rainCurvePath.moveTo(0, point1Y);
			snowCurvePath.moveTo(0, point1Y2);
			popCurvePath.moveTo(0, bottom);
			
			popCurvePath.addRect(point1X - halfPopBarWidth, bottom - drawHeight * popData[0], point1X + halfPopBarWidth, bottom, Path.Direction.CW);
			
			
			for (int index = 1; index < rainData.length; index++) {
				
				//  Position calculations
				point2X = index * columnWidth + halfColumnWidth;
				point2Y = bottom - drawHeight * (rainData[index] + addValueMinTo0) * scaleFactorY;
				point2Y2 = bottom - drawHeight * (snowData[index] + addValueMinTo0) * scaleFactorY;
				
				//  Middle connection point
				connectionPointsX = (point1X + point2X) / 2F;
				
				//  Extending each curves
				rainCurvePath.cubicTo(connectionPointsX, point1Y, connectionPointsX, point2Y, point2X, point2Y);
				snowCurvePath.cubicTo(connectionPointsX, point1Y2, connectionPointsX, point2Y2, point2X, point2Y2);
				
				//  Add pop bar
				popCurvePath.addRect(point2X - halfPopBarWidth, bottom - drawHeight * popData[index], point2X + halfPopBarWidth, bottom, Path.Direction.CW);
				
				//  Moving to new points to old points
				point1X = point2X;
				point1Y = point2Y;
				point1Y2 = point2Y2;
			}
			
			rainCurvePath.lineTo(width, point1Y);
			snowCurvePath.lineTo(width, point1Y2);
			rainCurvePath.moveTo(width, point1Y);
			snowCurvePath.moveTo(width, point1Y2);
		} else {
			rainCurvePath.moveTo(0, drawHeight);
			snowCurvePath.moveTo(0, drawHeight);
			popCurvePath.moveTo(0, drawHeight);
			
			rainCurvePath.lineTo(width, drawHeight);
			snowCurvePath.lineTo(width, drawHeight);
			
			rainCurvePath.moveTo(width, drawHeight);
			snowCurvePath.moveTo(width, drawHeight);
			popCurvePath.moveTo(width, drawHeight);
			
		}
		
		//  Close paths
		rainCurvePath.close();
		snowCurvePath.close();
		popCurvePath.close();
		
		//  Generating returned Bitmap
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawPath(popCurvePath, popCurvePaint);
		canvas.drawPath(rainCurvePath, rainCurvePaint);
		canvas.drawPath(snowCurvePath, snowCurvePaint);
		
		return returnedBitmap;
	}
	
	
	/**
	 * drawTextWithDrawable(@NonNull Canvas canvas, @NonNull Drawable drawable, @NonNull String text, @Px float top, @Px float left, @Px float spaceBetween, @NonNull Paint paint)
	 * <p>
	 * Used to draw text with drawable labels
	 * </p>
	 *
	 * @param canvas       Elements will be drawn on it
	 * @param drawable     Drawable that will be drawn next to the text
	 * @param text         That text will be drawn
	 * @param top          Where element will be drawn on the y axis
	 * @param left         Where element will be drawn on the x axis
	 * @param spaceBetween Space between text and drawable in pixels
	 * @param paint        The paint that will be used on drawable and text
	 * @apiNote canvas, drawable, text & paint shouldn't be null
	 */
	protected void drawTextWithDrawable(@NonNull Canvas canvas, @NonNull Drawable drawable, @NonNull String text, @Px int top, @Px int left, @Px int spaceBetween, @NonNull Paint paint) {
		int deltaDrawableText = 5;
		float sideLength = paint.getTextSize() + deltaDrawableText * 2;
		float textWidth = paint.measureText(text);
		float bottom = top + sideLength;
		float textX = left + sideLength + spaceBetween + textWidth / 2F;
		float textY = bottom - deltaDrawableText;
		
		//  Set Color and Dimensions of the drawable and print it on canvas
		drawable.setTint(paint.getColor());
		drawable.setBounds(BigDecimal.valueOf(left).intValue(),
				  BigDecimal.valueOf(top).intValue(),
				  BigDecimal.valueOf(left + sideLength).intValue(),
				  BigDecimal.valueOf(bottom).intValue());
		drawable.draw(canvas);
		canvas.drawText(text, textX, textY, paint);
	}
	
	
	/**
	 * drawWeatherConditionIcons(@NonNull Context context, @NonNull Canvas canvas, int weatherCode, @Px int top, @Px int left, @Px int width, @Px int height, boolean isDayTime)
	 * <p>
	 * Used to draw drawable corresponding to weatherCode
	 * </p>
	 *
	 * @param context     Context is used to retrieve drawables
	 * @param canvas      Elements will be drawn on it
	 * @param weatherCode Weather code of the weather
	 * @param top         Where element will be drawn on the y axis
	 * @param left        Where element will be drawn on the x axis
	 * @param width       The width of the element
	 * @param height      The height of the element
	 * @param isDayTime   Describes if it is day time or not
	 * @apiNote canvas shouldn't be null
	 */
	protected void drawWeatherConditionIcons(@NonNull Context context, @NonNull Canvas canvas, int weatherCode, @Px int top, @Px int left, @Px int width, @Px int height, boolean isDayTime) {
		//  WEATHER CONDITION DRAWING
		int weatherIconId;
		
		switch (weatherCode) {
			
			//  Thunderstorm Group
			case 210:
			case 211:
			case 212:
			case 221:
				weatherIconId = R.drawable.thunderstorm_flat;
				break;
			
			case 200:
			case 201:
			case 202:
			case 230:
			case 231:
			case 232:
				weatherIconId = R.drawable.storm_flat;
				break;
			
			//  Drizzle and Rain (Light)
			case 300:
			case 310:
			case 500:
			case 501:
			case 520:
				if (isDayTime) {
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
				if (isDayTime) {
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
				if (isDayTime) {
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
				if (isDayTime) {
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
				if (isDayTime) {
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
			default:
				weatherIconId = R.drawable.storm_flat;
				break;
			
		}
		
		Drawable drawable = getDrawable(context, weatherIconId);
		drawable.setBounds(left, top, left + width, top + height);
		drawable.draw(canvas);
	}
	
	
	/**
	 * drawWeatherConditionIcons(@NonNull Context context, @NonNull Canvas canvas, int weatherCode, @Px int top, @Px int left, @Px int width, @Px int height)
	 * <p>
	 * Used to draw drawable corresponding to weatherCode<br>
	 * Exactly the same as rawWeatherConditionIcons(@NonNullCanvas, int, @Px int, @Px int, @Px int, @Px int, boolean)<br>
	 * But the boolean parameter is set to true so it will only draw dayTime icons
	 * </p>
	 *
	 * @param context     Context is used to retrieve drawables
	 * @param canvas      Elements will be drawn on it
	 * @param weatherCode Weather code of the weather
	 * @param top         Where element will be drawn on the y axis
	 * @param left        Where element will be drawn on the x axis
	 * @param width       The width of the element
	 * @param height      The height of the element
	 */
	protected void drawWeatherConditionIcons(@NonNull Context context, @NonNull Canvas canvas, @Px int weatherCode, @Px int top, @Px int left, @Px int width, @Px int height) {
		this.drawWeatherConditionIcons(context, canvas, weatherCode, top, left, width, height, true);
	}
	
	
	/**
	 * drawUvIndex(@NonNull Canvas canvas, @Px int uvIndex, @Px int x, @Px int y, @Px int sideLength)
	 * <p>
	 * Used to draw UV Index icon
	 * </p>
	 *
	 * @param canvas     Moon phase will be drawn on it
	 * @param uvIndex    Uv Index
	 * @param x          Center of the drawn UV index icon on the x axis
	 * @param y          Center of the drawn UV index icon on the y axis
	 * @param sideLength Length side of the drawn UV index icon
	 */
	protected void drawUvIndex(@NonNull Canvas canvas, @Px int uvIndex, @Px int x, @Px int y, @Px int sideLength) {
		//uvIndex = 13;		//	ONLY FOR LAYOUT TEST PURPOSES
		int middle = sideLength / 2;
		int circleRadius = middle / 2 - 3;
		
		Bitmap sunBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_8888);
		Canvas sunCanvas = new Canvas(sunBitmap);
		
		//  if the uv index is null, there is no sunrays
		if (uvIndex != 0) {
			float maxAngle = 6.28218F;
			float deltaAngle = 0.392636F;
			float startRadius = circleRadius + 8F;
			float stopRadius = middle - 1F;
			float cosAngle;
			float sinAngle;
			
			if (uvIndex < 11) {
				stopRadius = startRadius + uvIndex * (stopRadius - startRadius) / 11F;
				
				//  Change color depending on the UV index
				if (uvIndex < 3)
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvLow, null));
				else if (uvIndex < 6)
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvModerate, null));
				else if (uvIndex < 8)
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvHigh, null));
				else
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvVeryHigh, null));
			} else {
				this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvExtreme, null));
			}
			
			//  Draw sun rays
			for (float angle = 0; angle < maxAngle; angle += deltaAngle) {
				cosAngle = BigDecimal.valueOf(Math.cos(angle)).floatValue();
				sinAngle = BigDecimal.valueOf(Math.sin(angle)).floatValue();
				
				sunCanvas.drawLine(middle + cosAngle * startRadius, middle + sinAngle * startRadius, middle + cosAngle * stopRadius, middle + sinAngle * stopRadius, this.sunIconPaint);
			}
		} else {
			this.sunIconPaint.setColor(getResources().getColor(R.color.colorIcons, null));
		}
		
		//  Draw the center of the sun an put uv index number in it
		sunCanvas.drawCircle(middle, middle, circleRadius, this.sunIconPaint);
		sunCanvas.drawText(String.valueOf(uvIndex), middle, middle + this.sunIconPaint.getTextSize() / 3F, this.sunIconPaint);
		
		canvas.drawBitmap(sunBitmap, x, y, null);
	}
	
	
	/**
	 * drawWindDirectionIcon(@NonNull Canvas canvas, float windDirection, @Px int x, @Px int y, @Px int sideLength)
	 * <p>
	 * Used to draw wind direction icon
	 * </p>
	 *
	 * @param canvas        Moon phase will be drawn on it
	 * @param windDirection Wind direction in degrees
	 * @param x             Center of the drawn wind direction icon on the x axis
	 * @param y             Center of the drawn wind direction icon on the y axis
	 * @param sideLength    Length side of the drawn wind direction icon
	 */
	protected void drawWindDirectionIcon(@NonNull Canvas canvas, float windDirection, @Px int x, @Px int y, @Px int sideLength) {
		Bitmap compassBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_8888);
		Canvas compassCanvas = new Canvas(compassBitmap);
		
		//  Do calculation for each points in clockwise order
		float point1x = sideLength * 0.5F;
		float point1y = sideLength * 0.15F;
		float point2X = sideLength * 0.85F;
		float point2Y = sideLength * 0.85F;
		float point3X = sideLength * 0.5F;
		float point3Y = sideLength * 0.60F;
		float point4X = sideLength * 0.15F;
		float point4Y = sideLength * 0.85F;
		
		//  Rotate canvas with windDirection
		float middle = sideLength / 2F;
		compassCanvas.rotate(windDirection, middle, middle);
		
		//  Draw each lines of wind direction arrow
		compassCanvas.drawLine(point1x, point1y, point3X, point3Y, this.iconsPaint);
		compassCanvas.drawLine(point3X, point3Y, point4X, point4Y, this.iconsPaint);
		compassCanvas.drawLine(point4X, point4Y, point1x, point1y, this.iconsPaint);
		compassCanvas.drawLine(point3X, point3Y, point2X, point2Y, this.iconsPaint);
		compassCanvas.drawLine(point2X, point2Y, point1x, point1y, this.iconsPaint);
		
		canvas.drawBitmap(compassBitmap, x, y, null);
	}
	
	/**
	 * initComponents(@NonNull Context context)
	 * <p>
	 * Used to initialize attributes used to draw a graph
	 * </p>
	 *
	 * @param context Current context, only used to construct super class and initialize attributes
	 */
	protected void initComponents(@NonNull Context context) {
		this.context = context;
		
		this.datePaint = new Paint();
		this.structurePaint = new Paint();
		this.primaryPaint = new Paint();
		this.secondaryPaint = new Paint();
		this.primaryGraphPaint = new Paint();
		this.secondaryGraphPaint = new Paint();
		this.tertiaryPaint = new Paint();
		this.tertiaryGraphPaint = new Paint();
		this.popBarGraphPaint = new Paint();
		this.iconsPaint = new Paint();
		this.sunIconPaint = new Paint();
		
		this.datePaint.setColor(getResources().getColor(R.color.colorPrimaryPaint, null));
		this.datePaint.setAlpha(255);
		this.datePaint.setTextSize(spToPx(19));
		this.datePaint.setStrokeWidth(0.65F);
		this.datePaint.setTextAlign(Paint.Align.LEFT);
		
		this.structurePaint.setColor(getResources().getColor(R.color.colorPrimaryPaint, null));
		this.structurePaint.setAlpha(255);
		this.structurePaint.setTextSize(spToPx(16));
		this.structurePaint.setStrokeWidth(0.55F);
		this.structurePaint.setTextAlign(Paint.Align.CENTER);
		
		this.primaryPaint.setColor(getResources().getColor(R.color.colorPrimaryPaint, null));
		this.primaryPaint.setStrokeWidth(2);
		this.primaryPaint.setAlpha(255);
		this.primaryPaint.setTextSize(spToPx(16));
		this.primaryPaint.setTextAlign(Paint.Align.CENTER);
		
		this.secondaryPaint.setColor(getResources().getColor(R.color.colorSecondaryPaint, null));
		this.secondaryPaint.setStrokeWidth(2);
		this.secondaryPaint.setAlpha(255);
		this.secondaryPaint.setTextSize(spToPx(16));
		this.secondaryPaint.setTextAlign(Paint.Align.CENTER);
		
		this.tertiaryPaint.setColor(getResources().getColor(R.color.colorTertiaryPaint, null));
		this.tertiaryPaint.setStrokeWidth(2);
		this.tertiaryPaint.setAlpha(255);
		this.tertiaryPaint.setTextSize(spToPx(16));
		this.tertiaryPaint.setTextAlign(Paint.Align.CENTER);
		
		this.primaryGraphPaint.setColor(getResources().getColor(R.color.colorPrimaryGraphPaint, null));
		this.primaryGraphPaint.setStrokeWidth(5);
		this.primaryGraphPaint.setAlpha(155);
		this.primaryGraphPaint.setPathEffect(null);
		this.primaryGraphPaint.setStyle(Paint.Style.STROKE);
		
		this.secondaryGraphPaint.setColor(getResources().getColor(R.color.colorSecondaryGraphPaint, null));
		this.secondaryGraphPaint.setStrokeWidth(5);
		this.secondaryGraphPaint.setAlpha(155);
		this.secondaryGraphPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
		this.secondaryGraphPaint.setStyle(Paint.Style.STROKE);
		
		this.tertiaryGraphPaint.setColor(getResources().getColor(R.color.colorTertiaryGraphPaint, null));
		this.tertiaryGraphPaint.setStrokeWidth(5);
		this.tertiaryGraphPaint.setAlpha(155);
		this.tertiaryGraphPaint.setPathEffect(null);
		this.tertiaryGraphPaint.setStyle(Paint.Style.STROKE);
		
		this.popBarGraphPaint.setColor(getResources().getColor(R.color.colorSecondaryGraphPaint, null));
		this.popBarGraphPaint.setStrokeWidth(5);
		this.popBarGraphPaint.setAlpha(155);
		this.popBarGraphPaint.setPathEffect(null);
		this.popBarGraphPaint.setStyle(Paint.Style.FILL);
		
		this.iconsPaint.setColor(getResources().getColor(R.color.colorIcons, null));
		this.iconsPaint.setStrokeWidth(3);
		this.iconsPaint.setAlpha(255);
		this.iconsPaint.setPathEffect(null);
		this.iconsPaint.setStyle(Paint.Style.STROKE);
		
		this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvExtreme, null));
		this.sunIconPaint.setStrokeWidth(2.5F);
		this.sunIconPaint.setAlpha(255);
		this.sunIconPaint.setTextSize(spToPx(15));
		this.sunIconPaint.setPathEffect(null);
		this.sunIconPaint.setStyle(Paint.Style.STROKE);
		this.sunIconPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	
	//  Pixel to Complex Units conversion
	//----------------------------------------------------------------------------------------------
	
	/**
	 * spToPx(float sp)
	 * <p>
	 * Just a SP to PX converter method
	 * </p>
	 *
	 * @param sp SP value that you want to convert
	 * @return The SP converted value into PX
	 */
	protected int spToPx(float sp) {
		return (int) TypedValue.applyDimension(
				  TypedValue.COMPLEX_UNIT_SP,
				  sp,
				  getResources().getDisplayMetrics());
	}
	
	
	/**
	 * dpToPx(float dip)
	 * <p>
	 * Just a DP to PX converter method
	 * </p>
	 *
	 * @param dip   DP value that you want to convert
	 * @return The DP converted value into PX
	 */
	protected int dpToPx(float dip) {
		return (int) TypedValue.applyDimension(
				  TypedValue.COMPLEX_UNIT_DIP,
				  dip,
				  getResources().getDisplayMetrics());
	}
	
	/**
	 * getDrawable(@NonNull Context context, @DrawableRes int drawableId)
	 * <p>
	 * Just a function do get drawables with AppCompatResources
	 * </p>
	 *
	 * @param context    The context needed to retrieve drawable
	 * @param drawableId The resource ID of the asked drawable
	 * @return The asked drawable
	 * @throws NullPointerException If the drawable hasn't been found, an exception is thrown
	 */
	protected Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableId) {
		Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
		if (drawable == null) throw new NullPointerException("Cannot find asked drawable");
		
		return drawable;
	}
}
