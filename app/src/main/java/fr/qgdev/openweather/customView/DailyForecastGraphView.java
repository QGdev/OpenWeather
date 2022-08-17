package fr.qgdev.openweather.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.DailyWeatherForecast;


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

	private final int COLUMN_WIDTH = dpToPx(280);
	private final int HALF_COLUMN_WIDTH = COLUMN_WIDTH / 2;
	private final int QUARTER_COLUMN_WIDTH = COLUMN_WIDTH / 4;
	private final int SIXTH_COLUMN_WIDTH = COLUMN_WIDTH / 6;
	private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;
	private FormattingService formattingService;


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
	 * initialization(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, TimeZone timeZone, FormattingService unitsFormattingService)
	 * <p>
	 * Used to initialize attributes used to draw a view
	 * </p>
	 *
	 * @param dailyWeatherForecastArrayList ArrayList of DailyWeatherForecasts
	 * @param timeZone                      TimeZone of the place
	 * @param unitsFormattingService        FormattingService of the application to format dates
	 */
	public void initialization(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull TimeZone timeZone, @NonNull FormattingService unitsFormattingService) {

		this.width = dailyWeatherForecastArrayList.size() * COLUMN_WIDTH;
		this.height = dpToPx(710);

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

			//  Precipitations graph
			this.precipitationsGraph = generateBitmapPrecipitationsGraphPath(
					dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "rain"),
					dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "snow"),
					dailyWeatherForecastArrayToSelectedAttributeFloatArray(dailyWeatherForecastArrayList, "pop"),
					this.width, dpToPx(50), tertiaryGraphPaint, primaryGraphPaint, popBarGraphPaint);
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
	 * dailyWeatherForecastArrayToSelectedAttributeFloatArray(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, String selectedAttribute) throws NoSuchFieldException, IllegalAccessException
	 * <p>
	 * Used to get an array of selected attribute of an ArrayList of DailyWeatherForecast objects
	 * </p>
	 *
	 * @param dailyWeatherForecastArrayList The DailyWeatherForecast arrayList
	 * @param selectedAttribute             The name of the wanted attribute
	 * @return The created array filled with all attributes
	 * @throws NoSuchFieldException   When the wanted attribute doesn't exist
	 * @throws IllegalAccessException When the wanted attributed is not a float
	 */
	private float[] dailyWeatherForecastArrayToSelectedAttributeFloatArray(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull String selectedAttribute) throws NoSuchFieldException, IllegalAccessException {
		float[] returnedAttributeArray = new float[dailyWeatherForecastArrayList.size()];
		Field classField = DailyWeatherForecast.class.getDeclaredField(selectedAttribute);

		for (int index = 0; index < returnedAttributeArray.length; index++) {
			returnedAttributeArray[index] = classField.getFloat(dailyWeatherForecastArrayList.get(index));
		}

		return returnedAttributeArray;
	}


	/**
	 * extractTemperaturesFromDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList)
	 * <p>
	 * Used to get all temperatures attributes of a DailyWeather object<br>
	 * Sorted Day after Day, newest day first and oldest day last, day is composed of morning, day, evening and night temperatures
	 * </p>
	 *
	 * @param dailyWeatherForecastArrayList The dailyForecast arrayList
	 * @return The created array filled with all temperature attributes
	 */
	private float[] extractTemperaturesFromDailyWeatherForecastArrayList(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
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


	/**
	 * extractFeelsLikeTemperaturesFromDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList)
	 * <p>
	 * Used to get all FeelsLike temperatures attributes of a DailyWeather object<br>
	 * Sorted Day after Day, newest day first and oldest day last, day is composed of morning, day, evening and night FeelsLike temperatures
	 * </p>
	 *
	 * @param dailyWeatherForecastArrayList The dailyForecast arrayList
	 * @return The created array filled with all temperature attributes
	 */
	private float[] extractFeelsLikeTemperaturesFromDailyWeatherForecastArrayList(@NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
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


	/**
	 * drawStructureAndDate(@NonNull Canvas canvas, float dateFirstLineY, float dateSecondLineY, float dayPeriodStartLineY, float dayPeriodStopLineY, ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, TimeZone timeZone)
	 * <p>
	 * Used to draw principal elements of the view such as date, day moments and separators
	 * </p>
	 *
	 * @param canvas                        Elements will be drawn on it
	 * @param dateFirstLineY                Where the name of the day will be drawn on y axis
	 * @param dateSecondLineY               Where the day number and the month number of the day will be drawn on y axis
	 * @param dayPeriodStartLineY           Where the separator between day moments start on y axis
	 * @param dayPeriodStopLineY            Where the separator between day moments ends on y axis
	 * @param dailyWeatherForecastArrayList The dailyForecast arrayList
	 */
	private void drawStructureAndDate(@NonNull Canvas canvas, @Px int dateFirstLineY, @Px int dateSecondLineY, @Px int dayPeriodStartLineY, @Px int dayPeriodStopLineY, @NonNull ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList, @NonNull TimeZone timeZone) {

		float xDiv = 0,
				xDivDayPeriod = QUARTER_COLUMN_WIDTH / 2F;
		Date date;

		//  For each day
		for (int index = 0; index < dailyWeatherForecastArrayList.size(); index++) {

			date = new Date(dailyWeatherForecastArrayList.get(index).dt);

			//  Draw date
			canvas.drawText(formattingService.getFormattedShortDayName(date, timeZone), xDiv + 10, dateFirstLineY, this.datePaint);
			canvas.drawText(formattingService.getFormattedDayMonth(date, timeZone), xDiv + 10, dateSecondLineY, this.datePaint);

			//  Draw day separator
			canvas.drawLine(xDiv, 0, xDiv, canvas.getHeight(), this.structurePaint);

			//  Draw separator between each day moments
			for (int i = 1; i < 4; i++)
				canvas.drawLine(xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStartLineY, xDiv + QUARTER_COLUMN_WIDTH * i, dayPeriodStopLineY, this.structurePaint);

			canvas.drawText(context.getString(R.string.title_daily_forecast_morning), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(context.getString(R.string.title_daily_forecast_noon), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(context.getString(R.string.title_daily_forecast_evening), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);
			xDivDayPeriod += QUARTER_COLUMN_WIDTH;
			canvas.drawText(context.getString(R.string.title_daily_forecast_night), xDiv + xDivDayPeriod, dayPeriodStartLineY, this.structurePaint);

			xDiv += COLUMN_WIDTH;
			xDivDayPeriod = QUARTER_COLUMN_WIDTH / 2F;
		}
	}


	/**
	 * drawMaxMinTemperatures(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw min and max temperatures of the day
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param dailyWeatherForecast Where data will be taken
	 * @param top                  Where temperatures will be drawn on the y axis
	 * @param left                 Where temperatures will be drawn on the x axis
	 */
	private void drawMaxMinTemperatures(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, int top, int left) {
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
				top + dpToPx(30),
				left,
				dpToPx(10),
				this.tertiaryPaint);
	}


	/**
	 * drawTemperatures(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw temperatures and feel like temperatures of each day moment
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param dailyWeatherForecast Where data will be taken
	 * @param top                  Where temperatures will be drawn on the y axis
	 * @param left                 Where temperatures will be drawn on the x axis
	 */
	private void drawTemperatures(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left) {
		float textX = left + QUARTER_COLUMN_WIDTH / 2F,
				textY1 = top,
				textY2 = top + dpToPx(25);
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
		int middle = sideLength / 2,
				circleRadius = middle / 2 - 3;

		Bitmap moonBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_4444);
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
			dX = circleRadius * 2;
			dX *= (moonPhase * 2) % 1;

			//  First half of the moon cycle
			if (moonPhase < 0.5F) {
				startX = dX;
				stopX = (circleRadius * 2);

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
	 * drawEnvironmentalVariables(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left, Paint paint)
	 * <p>
	 * Used to draw environmental variables such as pressure, cloudiness, humidity, dewPoint, sunrise, sunset, UVIndex Icon, moonrise, moonset and moonphase Icon
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param dailyWeatherForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 * @param paint                Paint used to draw text elements
	 */
	private void drawEnvironmentalVariables(@NonNull Canvas canvas, @NonNull DailyWeatherForecast dailyWeatherForecast, @Px int top, @Px int left, @NonNull Paint paint) {
		int firstColumn = left + dpToPx(20),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY1 = top,
				textY2 = textY1 + dpToPx(35),
				textY3 = textY2 + dpToPx(35),
				textY4 = textY3 + dpToPx(35),
				textY5 = textY4 + dpToPx(35),
				textY6 = textY5 + dpToPx(35);

		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.barometer_material), formattingService.getFormattedPressure(dailyWeatherForecast.pressure, true), textY1, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.cloudy_material), String.format("%d %%", dailyWeatherForecast.cloudiness), textY1, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.humidity_material), String.format("%d %%", dailyWeatherForecast.humidity), textY2, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.dew_point_material), formattingService.getFloatFormattedTemperature(dailyWeatherForecast.dewPoint, true), textY2, secondColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.sunrise_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.sunrise), timeZone), textY3, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.sunset_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.sunset), timeZone), textY4, firstColumn, dpToPx(5), paint);
		drawUvIndex(canvas, dailyWeatherForecast.uvIndex, secondColumn, textY3, dpToPx(70));
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.moonrise_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.moonrise), timeZone), textY5, firstColumn, dpToPx(5), paint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.moonset_material), formattingService.getFormattedTime(new Date(dailyWeatherForecast.moonset), timeZone), textY6, firstColumn, dpToPx(5), paint);
		drawMoonPhase(canvas, dailyWeatherForecast.moonPhase, secondColumn, textY5, dpToPx(70));
	}


	/**
	 * drawWindVariables(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw wind variables (wind speed, wind gust speed, wind direction)
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param dailyWeatherForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 */
	private void drawWindVariables(@NonNull Canvas canvas, @NonNull DailyWeatherForecast dailyWeatherForecast, @Px int top, @Px int left) {
		int firstColumn = left + dpToPx(20),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY = top + dpToPx(30),
				textY2 = textY + dpToPx(40),
				textY3 = textY2 + dpToPx(20);

		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.windsock_material), formattingService.getFloatFormattedSpeed(dailyWeatherForecast.windSpeed, true), textY, firstColumn, dpToPx(5), this.primaryPaint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.wind_material), formattingService.getFloatFormattedSpeed(dailyWeatherForecast.windGustSpeed, true), textY, secondColumn, dpToPx(5), this.secondaryPaint);

		canvas.drawText(formattingService.getFormattedDirectionInCardinalPoints(dailyWeatherForecast.windDirection), left + QUARTER_COLUMN_WIDTH, textY2, this.primaryPaint);
		canvas.drawText(formattingService.getFormattedDirectionInDegrees(dailyWeatherForecast.windDirection), left + QUARTER_COLUMN_WIDTH, textY3, this.primaryPaint);

		drawWindDirectionIcon(canvas, dailyWeatherForecast.windDirection, left + HALF_COLUMN_WIDTH + QUARTER_COLUMN_WIDTH - dpToPx(20), textY + dpToPx(30), dpToPx(35));
	}


	/**
	 * drawPrecipitationsVariables(@NonNull Canvas canvas, DailyWeatherForecast dailyWeatherForecast, float top, float left)
	 * <p>
	 * Used to draw wind variables (wind speed, wind gust speed, wind direction)
	 * </p>
	 *
	 * @param canvas               Elements will be drawn on it
	 * @param dailyWeatherForecast Where data will be taken
	 * @param top                  Where elements will be drawn on the y axis
	 * @param left                 Where elements will be drawn on the x axis
	 */
	private void drawPrecipitationsVariables(@NonNull Canvas canvas, @NonNull DailyWeatherForecast dailyWeatherForecast, @Px int top, @Px int left) {
		int firstColumn = left + dpToPx(20),
				secondColumn = firstColumn + HALF_COLUMN_WIDTH,
				textY = top,
				textY2 = textY + dpToPx(35);

		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.rain_material), formattingService.getFloatFormattedShortDistance(dailyWeatherForecast.rain, true), textY, firstColumn, dpToPx(5), this.tertiaryPaint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.snow_material), formattingService.getFloatFormattedShortDistance(dailyWeatherForecast.snow, true), textY, secondColumn, dpToPx(5), this.primaryPaint);
		drawTextWithDrawable(canvas, context.getDrawable(R.drawable.umbrella_material), String.format("%d %%", BigDecimal.valueOf(dailyWeatherForecast.pop * 100).intValue()), textY2, firstColumn, dpToPx(5), this.secondaryPaint);
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

		int leftOfColumn = 0, halfOfColumnWidth = HALF_COLUMN_WIDTH, quarterOfColumnWidth = QUARTER_COLUMN_WIDTH, sixthOfColumnWidth = SIXTH_COLUMN_WIDTH;

		drawStructureAndDate(canvas, dpToPx(15), dpToPx(35), dpToPx(125), dpToPx(230), dailyWeatherForecastArrayList, timeZone);

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {
			drawWeatherConditionIcons(canvas, dailyWeatherForecast.weatherCode, dpToPx(30), sixthOfColumnWidth, dpToPx(70), dpToPx(70));
			drawMaxMinTemperatures(canvas, dailyWeatherForecast, dpToPx(30), halfOfColumnWidth);

			drawTemperatures(canvas, dailyWeatherForecast, dpToPx(210), leftOfColumn);

			drawEnvironmentalVariables(canvas, dailyWeatherForecast, dpToPx(250), leftOfColumn, this.primaryPaint);
			drawWindVariables(canvas, dailyWeatherForecast, dpToPx(485), leftOfColumn);

			drawPrecipitationsVariables(canvas, dailyWeatherForecast, dpToPx(650), leftOfColumn);

			leftOfColumn += COLUMN_WIDTH;
			sixthOfColumnWidth += COLUMN_WIDTH;
			quarterOfColumnWidth += COLUMN_WIDTH;
			halfOfColumnWidth += COLUMN_WIDTH;
		}

		canvas.drawBitmap(this.temperaturesGraph, 0, dpToPx(140), null);
		canvas.drawBitmap(this.windSpeedsGraph, 0, dpToPx(460), null);
		canvas.drawBitmap(this.precipitationsGraph, 0, dpToPx(590), null);
	}
}
