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
import java.util.Date;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.DailyWeatherForecast;

public class DailyForecastGraphView extends View {

	private final int COLUMN_WIDTH = dpToPx(320);
	private final float HALF_COLUMN_WIDTH = COLUMN_WIDTH / 2F;
	private final float QUARTER_COLUMN_WIDTH = COLUMN_WIDTH / 4F;
	private final float SIXTH_COLUMN_WIDTH = COLUMN_WIDTH / 6F;
	private Context context;
	private int width, height;
	private Bitmap temperaturesGraph, windSpeedsGraph, precipitationsGraph;
	private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;

	private TimeZone timeZone;
	private Paint datePaint,
			dayPeriodPaint,
			primaryPaint,
			secondaryPaint,
			primaryGraphPaint,
			secondaryGraphPaint,
			tertiaryPaint,
			tertiaryGraphPaint,
			iconsPaint,
			sunIconPaint;

	private FormattingService formattingService;

	public DailyForecastGraphView(Context context) {
		super(context);
		init(context);
	}

	public DailyForecastGraphView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;

		this.datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.dayPeriodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

		this.dayPeriodPaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.dayPeriodPaint.setAlpha(255);
		this.dayPeriodPaint.setTextSize(spToPx(16));
		this.dayPeriodPaint.setStrokeWidth(0.55F);
		this.dayPeriodPaint.setTextAlign(Paint.Align.CENTER);

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

	public void initialisation(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, TimeZone timeZone, FormattingService unitsFormattingService) {

		float[] firstCurve, secondCurve;

		this.width = dailyWeatherForecastArrayList.size() * COLUMN_WIDTH;
		this.height = dpToPx(850);

		this.dailyWeatherForecastArrayList = dailyWeatherForecastArrayList;
		this.timeZone = timeZone;

		this.formattingService = unitsFormattingService;


		try {
			//  Temperatures graph
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(
					extractTemperaturesFromDailyWeatherForecastArrayList(dailyWeatherForecastArrayList),
					extractFeelsLikeTemperaturesFromDailyWeatherForecastArrayList(dailyWeatherForecastArrayList), this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
/*
			//  Wind speeds graph
			firstCurve = dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "windSpeed");
			secondCurve = dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "windGustSpeed");
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);

			//  Precipitations graph
			firstCurve = dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "rain");
			secondCurve = dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "snow");
			this.precipitationsGraph = generateBitmap2CurvesGraphPath(firstCurve, secondCurve, this.width, dpToPx(40), tertiaryGraphPaint, primaryGraphPaint);

			////    Pop graph
			firstCurve = dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "pop");
			//calculate1BarGraphPath(firstCurve, popGraphPath, dpToPx(760), dpToPx(800), 0, this.width);
*/
		}
		catch (Exception e) {
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

	private float[] dailyWeatherForecastArrayToSelectedAttributeFloatArray(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, String selectedAttribute) throws NoSuchFieldException, IllegalAccessException {
		float[] returnedAttributeArray = new float[dailyWeatherForecastArrayList.size()];
		Field classField = DailyWeatherForecast.class.getDeclaredField(selectedAttribute);

		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = classField.getFloat(dailyWeatherForecastArrayList.get(index));
		}

		return returnedAttributeArray;
	}

	private float[] extractTemperaturesFromDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
		float[] returnedAttributeArray = new float[dailyWeatherForecastArrayList.size() * 4];
		int arrayIndex = 0;

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureMorning;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureDay;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureEvening;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureNight;
		}
		return returnedAttributeArray;
	}

	private float[] extractFeelsLikeTemperaturesFromDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
		float[] returnedAttributeArray = new float[dailyWeatherForecastArrayList.size() * 4];
		int arrayIndex = 0;

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureMorningFeelsLike;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureDayFeelsLike;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureEveningFeelsLike;
			returnedAttributeArray[arrayIndex++] = dailyWeatherForecast.temperatureNightFeelsLike;
		}
		return returnedAttributeArray;
	}

	// Draw the main structure, day, divs and date
	private void drawStructureAndDate(Canvas canvas, float dateFirstLineY, float dateSecondLineY, float dayPeriodStartLineY, float dayPeriodStopLineY, ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, TimeZone timeZone) {

		float xDiv = 0,
				xDivDayPeriod = QUARTER_COLUMN_WIDTH / 2F,
				textLineY = dayPeriodStartLineY + 20;
		Date date;

		for (int index = 0; index < dailyWeatherForecastArrayList.size(); index++) {

			canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.dayPeriodPaint);

			date = new Date(dailyWeatherForecastArrayList.get(index).dt);

			canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), xDiv + 10, dateFirstLineY, this.datePaint);
			canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), xDiv + 10, dateSecondLineY, this.datePaint);

			for (int i = 1; i < 4; i++)
				canvas.drawLine(xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStartLineY, xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStopLineY, this.dayPeriodPaint);

			canvas.drawText(getContext().getString(R.string.title_daily_forecast_morning), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.dayPeriodPaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_noon), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.dayPeriodPaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_evening), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.dayPeriodPaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_night), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.dayPeriodPaint);

			xDiv += COLUMN_WIDTH;
			xDivDayPeriod = QUARTER_COLUMN_WIDTH / 2F;
		}
	}

	private void drawHourWeatherConditionIcons(Canvas canvas, int weatherCode, float top, float left, float width, float height) {
		//  WEATHER CONDITION DRAWING
		int weatherIconId;

		switch (weatherCode) {

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
				weatherIconId = this.context.getResources().getIdentifier("rain_and_sun_flat", "drawable", this.context.getPackageName());
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
				weatherIconId = this.context.getResources().getIdentifier("snow_flat", "drawable", this.context.getPackageName());
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
				weatherIconId = this.context.getResources().getIdentifier("fog_flat", "drawable", this.context.getPackageName());
				break;

			//  Sky
			case 800:
				weatherIconId = this.context.getResources().getIdentifier("sun_flat", "drawable", this.context.getPackageName());
				break;

			case 801:
			case 802:
			case 803:
				weatherIconId = this.context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", this.context.getPackageName());
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
		drawable.setBounds(BigDecimal.valueOf(left).intValue(), BigDecimal.valueOf(top).intValue(),
				BigDecimal.valueOf(left + width).intValue(), BigDecimal.valueOf(top + height).intValue());
		drawable.draw(canvas);
	}

	//  Draw temperatures max and min temps and
	private void drawMaxMinTemperatures(Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left) {
		//  Temperatures
		drawTextWithDrawable(canvas,
				getResources().getDrawable(this.context.getResources().getIdentifier("temperature_maximum_material", "drawable", this.context.getPackageName()), null),
				formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureMaximum, true),
				top,
				left,
				dpToPx(10),
				this.secondaryPaint);
		drawTextWithDrawable(canvas,
				getResources().getDrawable(this.context.getResources().getIdentifier("temperature_minimum_material", "drawable", this.context.getPackageName()), null),
				formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureMinimum, true),
				top + dpToPx(40),
				left,
				dpToPx(10),
				this.tertiaryPaint);
	}

	//  Draw temperatures max and min temps and
	private void drawTemperatures(Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left) {
		float textX = left + QUARTER_COLUMN_WIDTH / 2F,
				textY1 = top,
				textY2 = top + dpToPx(20);
		//  Temperatures
		////    Morning
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureMorning, false), textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureMorningFeelsLike, false), textX, textY2, this.secondaryPaint);
		textX += QUARTER_COLUMN_WIDTH;
		////    Noon
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureDay, false), textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureDayFeelsLike, false), textX, textY2, this.secondaryPaint);
		textX += QUARTER_COLUMN_WIDTH;
		////    Evening
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureEvening, false), textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureEveningFeelsLike, false), textX, textY2, this.secondaryPaint);
		textX += QUARTER_COLUMN_WIDTH;
		////    Night
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureNight, false), textX, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFloatFormattedTemperature(dailyWeatherForecast.temperatureNightFeelsLike, false), textX, textY2, this.secondaryPaint);


	}

	//  Draw temperatures max and min temps and
	private void drawTextWithDrawable(@NonNull Canvas canvas, @NonNull Drawable drawable, @NonNull String text, @Px float top, @Px float left, @Px float spaceBetween, @NonNull Paint paint) {
		int deltaDrawableText = 10;
		float height = paint.getTextSize() + deltaDrawableText * 2,
				textWidth = paint.measureText(text),
				bottom = top + height,
				textX = left + height + spaceBetween + textWidth / 2F,
				textY = bottom - deltaDrawableText;

		//  Set Color and Dimensions of the drawable and print it on canvas
		drawable.setTint(paint.getColor());
		drawable.setBounds(BigDecimal.valueOf(left).intValue(),
				BigDecimal.valueOf(top).intValue(),
				BigDecimal.valueOf(left + height).intValue(),
				BigDecimal.valueOf(bottom).intValue());
		drawable.draw(canvas);

		canvas.drawText(text, textX, textY, paint);
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


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float leftOfColumn = 0, halfOfColumnWidth = HALF_COLUMN_WIDTH, quarterOfColumnWidth = QUARTER_COLUMN_WIDTH, sixthOfColumnWidth = SIXTH_COLUMN_WIDTH;

		drawStructureAndDate(canvas, dpToPx(15), dpToPx(35), dpToPx(140), dpToPx(250), dailyWeatherForecastArrayList, timeZone);

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {

			drawHourWeatherConditionIcons(canvas, dailyWeatherForecast.weatherCode, dpToPx(40), sixthOfColumnWidth, dpToPx(70), dpToPx(70));


			drawMaxMinTemperatures(canvas, dailyWeatherForecast, dpToPx(40), halfOfColumnWidth);
			drawTemperatures(canvas, dailyWeatherForecast, dpToPx(230), leftOfColumn);
			//canvas.drawText(String.format("%d%%", currentDailyWeatherForecast.humidity), halfColumnWidth, dpToPx(245), this.tertiaryPaint);
			//drawTextWithDrawable(canvas, currentDailyWeatherForecast, dpToPx(315), halfColumnWidth, pressureUnit.contains("hpa"));
			/*
			drawHourUvIndex(canvas, currentDailyWeatherForecast, dpToPx(370), dpToPx(420), halfColumnWidth);

			drawHourDewPoint(canvas, currentDailyWeatherForecast, dpToPx(445), halfColumnWidth, temperatureUnit.contains("celsius"));
			canvas.drawText(String.format("%d%%", currentDailyWeatherForecast.cloudiness), halfColumnWidth, dpToPx(475), this.primaryPaint);
			drawHourVisibility(canvas, currentDailyWeatherForecast, dpToPx(505), halfColumnWidth, measureUnit.contains("metric"));

			drawHourWindSpeed(canvas, currentDailyWeatherForecast, dpToPx(540), halfColumnWidth, measureUnit.contains("metric"));
			drawHourWindDirection(canvas, currentDailyWeatherForecast,dpToPx(625), dpToPx(715), halfColumnWidth);

			drawHourPrecipitations(canvas, currentDailyWeatherForecast,dpToPx(725), halfColumnWidth, measureUnit.contains("metric"));
			*/

			leftOfColumn += COLUMN_WIDTH;
			sixthOfColumnWidth += COLUMN_WIDTH;
			quarterOfColumnWidth += COLUMN_WIDTH;
			halfOfColumnWidth += COLUMN_WIDTH;

		}


		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(150), null);
		/*
		canvas.drawBitmap(this.windSpeedsGraph, 0,dpToPx(570),null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(785),null);
		*/
	}


	//  Pixel to Complex Units conversion
	//----------------------------------------------------------------------------------------------

	// sp --> px
	private int spToPx(float sp) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP,
				sp,
				getResources().getDisplayMetrics());
	}

	// dp --> px
	private int dpToPx(float dip) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dip,
				getResources().getDisplayMetrics());
	}
}
