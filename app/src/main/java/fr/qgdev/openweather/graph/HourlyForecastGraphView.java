package fr.qgdev.openweather.graph;

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
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;

public class HourlyForecastGraphView extends View {

	private static FormattingService formattingService;
	private final int COLUMN_WIDTH = dpToPx(90);
	private Context context;
	private int width, height;
	private Bitmap temperaturesGraph, humidityGraph, pressureGraph, windSpeedsGraph, precipitationsGraph;
	private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
	private boolean[] isDayTime;
	private TimeZone timeZone;
	private Paint datePaint,
			hourPaint,
			primaryPaint,
			secondaryPaint,
			primaryGraphPaint,
			secondaryGraphPaint,
			tertiaryPaint,
			tertiaryGraphPaint,
			iconsPaint,
			sunIconPaint;

	public HourlyForecastGraphView(Context context) {
		super(context);
		init(context);
	}

	public HourlyForecastGraphView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;

		this.datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.iconsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.sunIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		this.datePaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.datePaint.setAlpha(255);
		this.datePaint.setTextSize(spToPx(19));
		this.datePaint.setStrokeWidth(0.65F);
		this.datePaint.setTextAlign(Paint.Align.LEFT);

		this.hourPaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.hourPaint.setAlpha(255);
		this.hourPaint.setTextSize(spToPx(16));
		this.hourPaint.setStrokeWidth(0.55F);
		this.hourPaint.setTextAlign(Paint.Align.CENTER);

		this.primaryPaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.primaryPaint.setStrokeWidth(2);
		this.primaryPaint.setAlpha(255);
		this.primaryPaint.setTextSize(spToPx(16));
		this.primaryPaint.setTextAlign(Paint.Align.CENTER);

		this.secondaryPaint.setColor(getResources().getColor(R.color.colorAccent, null));
		this.secondaryPaint.setStrokeWidth(2);
		this.secondaryPaint.setAlpha(255);
		this.secondaryPaint.setTextSize(spToPx(16));
		this.secondaryPaint.setTextAlign(Paint.Align.CENTER);

		this.tertiaryPaint.setColor(getResources().getColor(R.color.colorLightBlue, null));
		this.tertiaryPaint.setStrokeWidth(2);
		this.tertiaryPaint.setAlpha(255);
		this.tertiaryPaint.setTextSize(spToPx(16));
		this.tertiaryPaint.setTextAlign(Paint.Align.CENTER);

		this.primaryGraphPaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.primaryGraphPaint.setStrokeWidth(5);
		this.primaryGraphPaint.setAlpha(155);
		this.primaryGraphPaint.setPathEffect(null);
		this.primaryGraphPaint.setStyle(Paint.Style.STROKE);

		this.secondaryGraphPaint.setColor(getResources().getColor(R.color.colorAccent, null));
		this.secondaryGraphPaint.setStrokeWidth(5);
		this.secondaryGraphPaint.setAlpha(155);
		this.secondaryGraphPaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
		this.secondaryGraphPaint.setStyle(Paint.Style.STROKE);

		this.tertiaryGraphPaint.setColor(getResources().getColor(R.color.colorLightBlue, null));
		this.tertiaryGraphPaint.setStrokeWidth(5);
		this.tertiaryGraphPaint.setAlpha(155);
		this.tertiaryGraphPaint.setPathEffect(null);
		this.tertiaryGraphPaint.setStyle(Paint.Style.STROKE);

		this.iconsPaint.setColor(getResources().getColor(R.color.colorIcons, null));
		this.iconsPaint.setStrokeWidth(3);
		this.iconsPaint.setAlpha(255);
		this.iconsPaint.setPathEffect(null);
		this.iconsPaint.setStyle(Paint.Style.STROKE);

		this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvExtreme, null));
		this.sunIconPaint.setStrokeWidth(3);
		this.sunIconPaint.setAlpha(255);
		this.sunIconPaint.setTextSize(spToPx(15));
		this.sunIconPaint.setPathEffect(null);
		this.sunIconPaint.setStyle(Paint.Style.STROKE);
		this.sunIconPaint.setTextAlign(Paint.Align.CENTER);
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
			firstCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "rain");
			secondCurve = hourlyWeatherForecastArrayToSelectedAttributeFloatArray(hourlyWeatherForecastArrayList, "snow");
			this.precipitationsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(40), tertiaryGraphPaint, primaryGraphPaint);

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
				canvas.drawLine(x_div, 120, x_div, canvas.getHeight(), this.hourPaint);
			}
			//  Draw hour
			canvas.drawText(formattingService.getFormattedHour(date, timeZone), x_div + halfColumnWidthDp, hourLineY, this.hourPaint);

			x_div += columnWidthDp;
		}
	}


	private void drawHourWeatherConditionIcons(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, float top, float bottom, float middleOfColumnX, boolean isDayTime) {
		//  WEATHER CONDITION DRAWING
		int weatherIconId;
		float sideLength = bottom - top,
				middle = sideLength / 2F;

		switch (currentHourlyWeatherForecast.weatherCode) {

			//  Thunderstorm Group
			case 210:
			case 211:
			case 212:
			case 221:
				weatherIconId = this.context.getResources().getIdentifier("thunderstorm_flat", "drawable", this.context.getPackageName());
				break;

			case 200:
			case 201:
			case 202:
			case 230:
			case 231:
			case 232:
				weatherIconId = this.context.getResources().getIdentifier("storm_flat", "drawable", this.context.getPackageName());
				break;

			//  Drizzle and Rain (Light)
			case 300:
			case 310:
			case 500:
			case 501:
			case 520:
				if (isDayTime) {
					weatherIconId = this.context.getResources().getIdentifier("rain_and_sun_flat", "drawable", this.context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = this.context.getResources().getIdentifier("rainy_night_flat", "drawable", this.context.getPackageName());
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
				weatherIconId = this.context.getResources().getIdentifier("rain_flat", "drawable", this.context.getPackageName());
				break;

			//Drizzle and Rain (Heavy)
			case 312:
			case 314:
			case 502:
			case 503:
			case 504:
			case 522:
				weatherIconId = this.context.getResources().getIdentifier("heavy_rain_flat", "drawable", this.context.getPackageName());
				break;

			//  Snow
			case 600:
			case 601:
			case 620:
			case 621:
				if (isDayTime) {
					weatherIconId = this.context.getResources().getIdentifier("snow_flat", "drawable", this.context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = this.context.getResources().getIdentifier("snow_and_night_flat", "drawable", this.context.getPackageName());
				}
				break;

			case 602:
			case 622:
				weatherIconId = this.context.getResources().getIdentifier("snow_flat", "drawable", this.context.getPackageName());
				break;

			case 611:
			case 612:
			case 613:
			case 615:
			case 616:
				weatherIconId = this.context.getResources().getIdentifier("sleet_flat", "drawable", this.context.getPackageName());
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
					weatherIconId = this.context.getResources().getIdentifier("fog_flat", "drawable", this.context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = this.context.getResources().getIdentifier("fog_and_night_flat", "drawable", this.context.getPackageName());
				}
				break;

			//  Sky
			case 800:
				//  Day
				if (isDayTime) {
					weatherIconId = this.context.getResources().getIdentifier("sun_flat", "drawable", this.context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = this.context.getResources().getIdentifier("moon_phase_flat", "drawable", this.context.getPackageName());
				}
				break;

			case 801:
			case 802:
			case 803:
				if (isDayTime) {
					weatherIconId = this.context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", this.context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = this.context.getResources().getIdentifier("cloudy_night_flat", "drawable", this.context.getPackageName());
				}
				break;

			case 804:
				weatherIconId = this.context.getResources().getIdentifier("cloudy_flat", "drawable", this.context.getPackageName());
				break;

			//  Default
			default:
				weatherIconId = this.context.getResources().getIdentifier("storm_flat", "drawable", this.context.getPackageName());
				break;

		}

		Drawable drawable = getResources().getDrawable(weatherIconId, null);
		drawable.setBounds(BigDecimal.valueOf(middleOfColumnX - middle).intValue(), BigDecimal.valueOf(top).intValue(), BigDecimal.valueOf(middleOfColumnX + middle).intValue(), BigDecimal.valueOf(top + sideLength).intValue());
		drawable.draw(canvas);
	}

	//  Draw temperatures for an hour
	private void drawHourTemperatures(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		//Temperatures
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperature, false), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.temperatureFeelsLike, false), middleOfColumnX, y + 50, this.secondaryPaint);
	}


	//  Draw pressure for an hour
	private void drawHourPressure(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFormattedPressure(currentHourlyWeatherForecast.pressure, false), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw UV index for an hour
	private void drawHourUvIndex(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, float top, float bottom, float middleOfColumnX) {
		float sideLength = bottom - top,
				middle = sideLength / 2F,
				circleRadius = middle / 2F - 3;

		Bitmap sunBitmap = Bitmap.createBitmap((int) sideLength, (int) sideLength, Bitmap.Config.ARGB_4444);
		Canvas sunCanvas = new Canvas(sunBitmap);

		//  if the uv index is null, there is no sunrays
		if (currentHourlyWeatherForecast.uvIndex != 0) {
			float maxAngle = 6.28218F, deltaAngle = 0.392636F,
					startRadius = circleRadius + 8, stopRadius = middle - 1;
			float cosAngle, sinAngle;

			if (currentHourlyWeatherForecast.uvIndex < 11) {
				stopRadius = startRadius + currentHourlyWeatherForecast.uvIndex * (stopRadius - startRadius) / 11F;

				//  Change color depending on the UV index
				if (currentHourlyWeatherForecast.uvIndex < 3)
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvLow, null));
				else if (currentHourlyWeatherForecast.uvIndex < 6)
					this.sunIconPaint.setColor(getResources().getColor(R.color.colorUvModerate, null));
				else if (currentHourlyWeatherForecast.uvIndex < 8)
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
		sunCanvas.drawText(String.valueOf(currentHourlyWeatherForecast.uvIndex), middle, middle * 1.2F, this.sunIconPaint);

		canvas.drawBitmap(sunBitmap, middleOfColumnX - middle, top, null);
	}

	//  Draw dewPoint for an hour
	private void drawHourDewPoint(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedTemperature(currentHourlyWeatherForecast.dewPoint, false), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw temperatures for an hour
	private void drawHourVisibility(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedDistance(currentHourlyWeatherForecast.visibility, true), middleOfColumnX, y, this.primaryPaint);
	}

	//  Draw wind and gust speeds for an hour
	private void drawHourWindSpeed(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windSpeed, true), middleOfColumnX, y, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedSpeed(currentHourlyWeatherForecast.windGustSpeed, true), middleOfColumnX, y + 50, this.secondaryPaint);
	}

	//  Draw wind direction for an hour
	private void drawHourWindDirection(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, float top, float bottom, float middleOfColumnX) {
		float sideLength = bottom - top - dpToPx(60),
				middle = sideLength / 2F;

		Bitmap compassBitmap = Bitmap.createBitmap((int) sideLength, (int) sideLength, Bitmap.Config.ARGB_4444);
		Canvas compassCanvas = new Canvas(compassBitmap);

		//  Do calculation for each points in clockwise order
		float point1_x, point1_y, point2_x, point2_y, point3_x, point3_y, point4_x, point4_y;

		point1_x = sideLength * 0.5F;
		point1_y = sideLength * 0.15F;

		point2_x = sideLength * 0.85F;
		point2_y = sideLength * 0.85F;

		point3_x = sideLength * 0.5F;
		point3_y = sideLength * 0.60F;

		point4_x = sideLength * 0.15F;
		point4_y = sideLength * 0.85F;

		compassCanvas.rotate(currentHourlyWeatherForecast.windDirection, middle, middle);

		compassCanvas.drawLine(point1_x, point1_y, point3_x, point3_y, this.iconsPaint);

		compassCanvas.drawLine(point3_x, point3_y, point4_x, point4_y, this.iconsPaint);
		compassCanvas.drawLine(point4_x, point4_y, point1_x, point1_y, this.iconsPaint);

		compassCanvas.drawLine(point3_x, point3_y, point2_x, point2_y, this.iconsPaint);
		compassCanvas.drawLine(point2_x, point2_y, point1_x, point1_y, this.iconsPaint);

		canvas.drawBitmap(compassBitmap, middleOfColumnX - middle, top, null);

		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(currentHourlyWeatherForecast.windDirection), middleOfColumnX, bottom - dpToPx(40), this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(currentHourlyWeatherForecast.windDirection), middleOfColumnX, bottom - dpToPx(20), this.primaryPaint);

	}


	//  Generate a bitmap of GraphPath for two arrays
	//
	////    firstCurveData and secondCurveData must have the same number of elements !
	private Bitmap generateBitmap2CurvesGraphPath(float[] firstCurveData, float[] secondCurveData, int width, int height, @NonNull Paint firstCurvePaint, @NonNull Paint secondCurvePaint) {

		//  Initializing graph paths
		Path firstCurvePath = new Path();
		Path secondCurvePath = new Path();

		//  Searching for max and min values in arrays
		float minValue = firstCurveData[0],
				maxValue = firstCurveData[0];

		for (int index = 0; index < firstCurveData.length; index++) {
			if (firstCurveData[index] > maxValue) maxValue = firstCurveData[index];
			if (firstCurveData[index] < minValue) minValue = firstCurveData[index];

			if (secondCurveData[index] > maxValue) maxValue = secondCurveData[index];
			if (secondCurveData[index] < minValue) minValue = secondCurveData[index];
		}

		//  Find the value to adjust all values so that the minimum is 0
		float addValueMinTo0 = minValue * (-1);
		//  Find scale factor of Y axis
		float scaleFactorY = 1 / (maxValue + addValueMinTo0);

		//  Doing Bezier curve calculations for each curves
		float connectionPointsX, point1_X, point1_Y, point2_Y, point1_Y_2, point2_X, point2_Y_2;
		float columnWidth = width / firstCurveData.length,
				halfColumnWidth = columnWidth / 2F,
				//  To avoid curve trimming
				top = 4,
				bottom = height - 4,
				drawHeight = bottom - top;

		//  Clear each paths
		firstCurvePath.reset();
		secondCurvePath.reset();

		//  If min and max values are the same, the resulting curves are flat curves
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1_X = halfColumnWidth;
			point1_Y = bottom - drawHeight * (firstCurveData[0] + addValueMinTo0) * scaleFactorY;
			point1_Y_2 = bottom - drawHeight * (secondCurveData[0] + addValueMinTo0) * scaleFactorY;

			firstCurvePath.moveTo(0, point1_Y);
			secondCurvePath.moveTo(0, point1_Y_2);

			for (int index = 1; index < firstCurveData.length; index++) {

				//  Position calculations
				point2_X = index * columnWidth + halfColumnWidth;
				point2_Y = bottom - drawHeight * (firstCurveData[index] + addValueMinTo0) * scaleFactorY;
				point2_Y_2 = bottom - drawHeight * (secondCurveData[index] + addValueMinTo0) * scaleFactorY;

				//  Middle connection point
				connectionPointsX = (point1_X + point2_X) / 2F;

				//  Extending each curves
				firstCurvePath.cubicTo(connectionPointsX, point1_Y, connectionPointsX, point2_Y, point2_X, point2_Y);
				secondCurvePath.cubicTo(connectionPointsX, point1_Y_2, connectionPointsX, point2_Y_2, point2_X, point2_Y_2);

				//  Moving to new points to old points
				point1_X = point2_X;
				point1_Y = point2_Y;
				point1_Y_2 = point2_Y_2;
			}

			firstCurvePath.lineTo(width, point1_Y);
			secondCurvePath.lineTo(width, point1_Y_2);
			firstCurvePath.moveTo(width, point1_Y);
			secondCurvePath.moveTo(width, point1_Y_2);
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
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawPath(firstCurvePath, firstCurvePaint);
		canvas.drawPath(secondCurvePath, secondCurvePaint);


		return returnedBitmap;
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


	//  Generate BarGraphPath for one array
	//
	////    curvePath must have been initialized correctly !
	private void calculate1BarGraphPath(float[] curveData, @NonNull Path curvePath, float top, float bottom, float left, float right) {
		float halfGraphColumnWidth, middleOfColumnX, height = bottom - top;

		//  Clear each paths
		curvePath.reset();

		//  Position calculation for the first point
		halfGraphColumnWidth = COLUMN_WIDTH / 6F;

		curvePath.moveTo(left, bottom);

		for (int index = 0; index < curveData.length; index++) {

			//  Position calculations
			middleOfColumnX = index * COLUMN_WIDTH + COLUMN_WIDTH / 2F;

			curvePath.addRect(middleOfColumnX - halfGraphColumnWidth, (float) (bottom - height * curveData[index]), middleOfColumnX + halfGraphColumnWidth, (float) bottom, Path.Direction.CW);
		}

		// Close path
		curvePath.moveTo(right, bottom);
		curvePath.close();
	}

	//  Draw dewPoint for an hour
	private void drawHourPrecipitations(Canvas canvas, HourlyWeatherForecast currentHourlyWeatherForecast, int y, float middleOfColumnX) {
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
		float halfColumnWidth = COLUMN_WIDTH / 2F;

		drawStructureAndDate(canvas, COLUMN_WIDTH, hourlyWeatherForecastArrayList, timeZone);

		for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {
			currentHourlyWeatherForecast = hourlyWeatherForecastArrayList.get(index);

			drawHourWeatherConditionIcons(canvas, currentHourlyWeatherForecast, dpToPx(70), dpToPx(115), halfColumnWidth, isDayTime[index]);
			drawHourTemperatures(canvas, currentHourlyWeatherForecast, dpToPx(140), halfColumnWidth);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.humidity), halfColumnWidth, dpToPx(245), this.tertiaryPaint);
			drawHourPressure(canvas, currentHourlyWeatherForecast, dpToPx(315), halfColumnWidth);

			drawHourUvIndex(canvas, currentHourlyWeatherForecast, dpToPx(370), dpToPx(420), halfColumnWidth);

			drawHourDewPoint(canvas, currentHourlyWeatherForecast, dpToPx(445), halfColumnWidth);
			canvas.drawText(String.format("%d%%", currentHourlyWeatherForecast.cloudiness), halfColumnWidth, dpToPx(475), this.primaryPaint);
			drawHourVisibility(canvas, currentHourlyWeatherForecast, dpToPx(505), halfColumnWidth);

			drawHourWindSpeed(canvas, currentHourlyWeatherForecast, dpToPx(540), halfColumnWidth);
			drawHourWindDirection(canvas, currentHourlyWeatherForecast, dpToPx(625), dpToPx(715), halfColumnWidth);

			drawHourPrecipitations(canvas, currentHourlyWeatherForecast, dpToPx(725), halfColumnWidth);

			halfColumnWidth += COLUMN_WIDTH;
		}

		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(175), null);
		canvas.drawBitmap(this.humidityGraph, 0, dpToPx(260), null);
		canvas.drawBitmap(this.pressureGraph, 0, dpToPx(330), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(570), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(785), null);
	}
}
