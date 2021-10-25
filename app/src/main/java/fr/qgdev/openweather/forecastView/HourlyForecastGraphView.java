package fr.qgdev.openweather.forecastView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;

public class HourlyForecastGraphView extends ForecastView {

	private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
	private boolean[] isDayTime;

	public HourlyForecastGraphView(Context context) {
		super(context);
	}

	public HourlyForecastGraphView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	//  Generate an array of booleans to know of if it is the day or not, each item in the array corresponds to one hour of the hourlyWeatherForecastArrayList
	private boolean[] generateIsDayTimeArray(ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {

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

	private float[] hourlyWeatherForecastArrayToSelectedAttributeFloatArray(ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, String selectedAttribute) throws NoSuchFieldException, IllegalAccessException {
		float[] returnedAttributeArray = new float[hourlyWeatherForecastArrayList.size()];
		Field classField = HourlyWeatherForecast.class.getDeclaredField(selectedAttribute);

		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = classField.getFloat(hourlyWeatherForecastArrayList.get(index));
		}

		return returnedAttributeArray;
	}

	public void initialisation(@NonNull ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull FormattingService unitsFormattingService, @NonNull TimeZone timeZone) {

		this.COLUMN_WIDTH = dpToPx(90);

		float[] firstCurve, secondCurve;

		this.width = hourlyWeatherForecastArrayList.size() * COLUMN_WIDTH;
		this.height = dpToPx(850);

		this.hourlyWeatherForecastArrayList = hourlyWeatherForecastArrayList;

		this.formattingService = unitsFormattingService;

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

			////    Pop graph
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "pop");
			//calculate1BarGraphPath(firstCurve, popGraphPath, dpToPx(760), dpToPx(800), 0, this.width);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setMinimumHeight(@Px int minHeight) {
		super.setMinimumHeight(minHeight);
	}


	@Override
	public void setMinimumWidth(@Px int minHeight) {
		super.setMinimumWidth(minHeight);
	}


	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(this.width, this.height);
	}

	// Draw the main structure, day and hours divs and date
	private void drawStructureAndDate(Canvas canvas, float columnWidthDp, ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList, TimeZone timeZone) {

		byte previousItemDay = 0, currentItemDay;
		int x_div = 0;
		float dateFirstLineY, dateSecondLineY, hourLineY, halfColumnWidthDp;
		Calendar calendar;
		Date date;

		calendar = Calendar.getInstance();
		calendar.setTimeZone(timeZone);
		calendar.setTimeInMillis(hourlyWeatherForecastArrayList.get(0).dt);

		dateFirstLineY = dpToPx(15);
		dateSecondLineY = dpToPx(35);
		hourLineY = dpToPx(60);
		halfColumnWidthDp = COLUMN_WIDTH / 2F;

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
			canvas.drawText(formattingService.getFormattedHour(date, timeZone), x_div + halfColumnWidthDp, hourLineY, this.structurePaint);

			x_div += columnWidthDp;
		}
	}


	//  Draw temperatures
	private void drawTemperatures(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		//Temperatures
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperature, false), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperatureFeelsLike, false), middleOfColumnX, y + 50, this.secondaryPaint);
	}


	//  Draw pressure
	private void drawPressure(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFormattedPressure(currentHourlyWeatherForecast.pressure, false), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw dewPoint
	private void drawDewPoint(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.dewPoint, false), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw temperatures
	private void drawVisibility(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedDistance(currentHourlyWeatherForecast.visibility, true), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw wind and gust speeds
	private void drawWindSpeed(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windSpeed, true), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windGustSpeed, true), middleOfColumnX, y + 50, this.secondaryPaint);
	}

	//  Draw wind direction
	private void drawWindDirection(Canvas canvas, short windDirection, float top, float left, float width) {
		float middle = width / 2F;
		drawWindDirectionIcon(canvas, windDirection, left + dpToPx(5), top, width - dpToPx(10));

		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(windDirection), left + middle, top + width + dpToPx(5), this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(windDirection), left + middle, top + width + dpToPx(25), this.primaryPaint);

	}


	//  Generate a bitmap of GraphPath for one array
	//
	private Bitmap generateBitmap1CurvesGraphPath(float[] curveData, int width, int height, @NonNull Paint curvePaint) {

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

	//  Draw dewPoint
	private void drawPrecipitations(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.rain, true), middleOfColumnX, y, this.tertiaryPaint);
		canvas.drawText(formattingService.getFloatFormattedShortDistance(currentHourlyWeatherForecast.snow, true), middleOfColumnX, y + 50, this.primaryPaint);

		canvas.drawText(String.format("%d %%", (int) (currentHourlyWeatherForecast.pop * 100)), middleOfColumnX, y + 100, this.secondaryPaint);

	}

	//  Pixel to Complex Units conversion
	//----------------------------------------------------------------------------------------------

	// dp --> px
	private int dpToPx(float dip) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dip,
				getResources().getDisplayMetrics());
	}

	// sp --> px
	private int spToPx(float sp) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP,
				sp,
				getResources().getDisplayMetrics());
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		HourlyWeatherForecast currentHourlyWeatherForecast;
		float halfWidthX = COLUMN_WIDTH / 2F, drawableX = halfWidthX - dpToPx(25);

		drawStructureAndDate(canvas, COLUMN_WIDTH, hourlyWeatherForecastArrayList, timeZone);

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
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(570), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(785), null);
	}
}
