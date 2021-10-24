package fr.qgdev.openweather.forecastView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.DailyWeatherForecast;

public class DailyForecastGraphView extends ForecastView {

	private final int COLUMN_WIDTH = dpToPx(280);
	private final float HALF_COLUMN_WIDTH = COLUMN_WIDTH / 2F;
	private final float QUARTER_COLUMN_WIDTH = COLUMN_WIDTH / 4F;
	private final float SIXTH_COLUMN_WIDTH = COLUMN_WIDTH / 6F;
	private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;


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
		this.structurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.iconsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.sunIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.moonLightIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.moonShadowIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		this.datePaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.datePaint.setAlpha(255);
		this.datePaint.setTextSize(spToPx(19));
		this.datePaint.setStrokeWidth(0.65F);
		this.datePaint.setTextAlign(Paint.Align.LEFT);

		this.structurePaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		this.structurePaint.setAlpha(255);
		this.structurePaint.setTextSize(spToPx(16));
		this.structurePaint.setStrokeWidth(0.55F);
		this.structurePaint.setTextAlign(Paint.Align.CENTER);

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

	public void initialisation(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, TimeZone timeZone, FormattingService unitsFormattingService) {

		this.width = dailyWeatherForecastArrayList.size() * COLUMN_WIDTH;
		this.height = dpToPx(850);

		this.dailyWeatherForecastArrayList = dailyWeatherForecastArrayList;
		this.timeZone = timeZone;

		this.formattingService = unitsFormattingService;


		try {
			//  Temperatures graph
			this.temperaturesGraph = generateBitmap2CurvesGraphPath(
					extractTemperaturesFromDailyWeatherForecastArrayList(dailyWeatherForecastArrayList),
					extractFeelsLikeTemperaturesFromDailyWeatherForecastArrayList(dailyWeatherForecastArrayList),
					this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);

			//  Wind speeds graph
			this.windSpeedsGraph = generateBitmap2CurvesGraphPath(
					dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "windSpeed"),
					dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "windGustSpeed"),
					this.width, dpToPx(50), primaryGraphPaint, secondaryGraphPaint);
/*
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

			canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.structurePaint);

			date = new Date(dailyWeatherForecastArrayList.get(index).dt);

			canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), xDiv + 10, dateFirstLineY, this.datePaint);
			canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), xDiv + 10, dateSecondLineY, this.datePaint);

			for (int i = 1; i < 4; i++)
				canvas.drawLine(xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStartLineY, xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStopLineY, this.structurePaint);

			canvas.drawText(getContext().getString(R.string.title_daily_forecast_morning), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_noon), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_evening), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(getContext().getString(R.string.title_daily_forecast_night), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);

			xDiv += COLUMN_WIDTH;
			xDivDayPeriod = QUARTER_COLUMN_WIDTH / 2F;
		}
	}


	//  Draw max and min temperatures
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

	//  Draw temperatures
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

	//  Draw Moon phase icon
	private void drawMoonPhase(Canvas canvas, float moonPhase, float x, float y, float sideLength) {
		float middle = sideLength / 2F,
				circleRadius = middle / 2F - 3;

		Bitmap moonBitmap = Bitmap.createBitmap((int) sideLength, (int) sideLength, Bitmap.Config.ARGB_4444);
		Canvas moonCanvas = new Canvas(moonBitmap);

		//  Draw a full visible moon
		moonCanvas.drawCircle(middle, middle, circleRadius, this.moonLightIconPaint);
		moonCanvas.drawCircle(middle, middle, circleRadius + 1, this.moonShadowIconPaint);

		//  No need to draw shadows part
		if (moonPhase != 0.5F) {
			int dX, startX, stopX;
			float left = middle - circleRadius,
					right = middle + circleRadius,
					top2 = left,
					bottom2 = right;

			//  Delimitation between light and shadow parts
			dX = (int) circleRadius * 2;
			dX *= (moonPhase * 2) % 1;

			//  First half of the moon cycle
			if (moonPhase < 0.5F) {
				startX = dX;
				stopX = (int) (circleRadius * 2);

			}
			//  Last half of the moon cycle
			else {
				startX = 0;
				stopX = dX;
			}

			//  Draw shadow part
			for (int i = startX; i <= stopX; i++) {
				if (left + i < middle)  //  First half
					moonCanvas.drawArc(left + i, top2, right - i, bottom2, 90, 180, false, this.moonShadowIconPaint);
				else                    //  Last haft
					moonCanvas.drawArc(right - i, top2, left + i, bottom2, -90, 180, false, this.moonShadowIconPaint);
			}
		}
		canvas.drawBitmap(moonBitmap, x, y, null);
	}


	//  Draw environmental variables
	private void drawEnvironmentalVariables(Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left, Paint paint) {
		float firstColumn = left + dpToPx(10),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY1 = top,
				textY2 = textY1 + dpToPx(35),
				textY3 = textY2 + dpToPx(35),
				textY4 = textY3 + dpToPx(35),
				textY5 = textY4 + dpToPx(35),
				textY6 = textY5 + dpToPx(35),
				textY7 = textY6 + dpToPx(35);

		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.barometer_material), formattingService.getFormattedPressure(dailyWeatherForecast.pressure, true), textY1, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.cloudy_material), String.format("%d %%", dailyWeatherForecast.cloudiness), textY1, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.humidity_material), String.format("%d %%", dailyWeatherForecast.humidity), textY2, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.dew_point_material), formattingService.getFloatFormattedTemperature(dailyWeatherForecast.dewPoint, true), textY2, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.sunrise_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.sunrise), timeZone), textY3, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.sunset_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.sunset), timeZone), textY4, firstColumn, dpToPx(5), paint);
		drawUvIndex(canvas, dailyWeatherForecast.uvIndex, secondColumn, textY3, dpToPx(70));
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.moonrise_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.moonrise), timeZone), textY5, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.moonset_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.moonset), timeZone), textY6, firstColumn, dpToPx(5), paint);
		drawMoonPhase(canvas, dailyWeatherForecast.moonPhase, secondColumn, textY5, dpToPx(70));

	}

	//  Draw wind Speeds
	private void drawWindsSpeed(Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float graphHeight, float top, float left) {
		float firstColumn = left + dpToPx(10),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY = top + graphHeight + dpToPx(10);

		canvas.drawLine(left + HALF_COLUMN_WIDTH, top, left + HALF_COLUMN_WIDTH, top + graphHeight, this.structurePaint);
		//  Wind Speed
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.windsock_material), formattingService.getFloatFormattedSpeed(dailyWeatherForecast.windSpeed, true), textY, firstColumn, dpToPx(5), this.primaryPaint);

		//  Wind GustSeed
		drawTextWithDrawable(canvas, getContext().getDrawable(R.drawable.wind_material), formattingService.getFloatFormattedSpeed(dailyWeatherForecast.windGustSpeed, true), textY, secondColumn, dpToPx(5), this.secondaryPaint);
	}

	///  Draw wind variables
	private void drawWindVariables(Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float bottom, float left) {
		float sideLength = bottom - top,
				middle = sideLength / 2F,
				firstColumn = left + dpToPx(10),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY1 = top + sideLength / 4F * 1.5F,
				textY2 = top + sideLength / 4F * 3.5F;


		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(dailyWeatherForecast.windDirection), firstColumn, textY1, this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(dailyWeatherForecast.windDirection), firstColumn, textY2, this.primaryPaint);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float leftOfColumn = 0, halfOfColumnWidth = HALF_COLUMN_WIDTH, quarterOfColumnWidth = QUARTER_COLUMN_WIDTH, sixthOfColumnWidth = SIXTH_COLUMN_WIDTH;

		drawStructureAndDate(canvas, dpToPx(15), dpToPx(35), dpToPx(125), dpToPx(230), dailyWeatherForecastArrayList, timeZone);

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {

			drawWeatherConditionIcons(canvas, dailyWeatherForecast.weatherCode, dpToPx(30), sixthOfColumnWidth, dpToPx(70), dpToPx(70));
			drawMaxMinTemperatures(canvas, dailyWeatherForecast, dpToPx(30), halfOfColumnWidth);

			drawTemperatures(canvas, dailyWeatherForecast, dpToPx(210), leftOfColumn);

			drawEnvironmentalVariables(canvas, dailyWeatherForecast, dpToPx(250), leftOfColumn, this.primaryPaint);
			drawWindVariables(canvas, dailyWeatherForecast, dpToPx(50), dpToPx(475), leftOfColumn);

			//canvas.drawText(String.format("%d%%", currentDailyWeatherForecast.humidity), halfColumnWidth, dpToPx(245), this.tertiaryPaint);
			//drawTextWithDrawable(canvas, currentDailyWeatherForecast, dpToPx(315), halfColumnWidth, pressureUnit.contains("hpa"));
			/*top
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


		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(140), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(475), null);
		/*
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
