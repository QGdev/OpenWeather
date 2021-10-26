package fr.qgdev.openweather.forecastView;

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

import java.math.BigDecimal;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.R;

public abstract class ForecastView extends View {

	protected static FormattingService formattingService;
	protected int COLUMN_WIDTH;
	protected Context context;
	protected int width, height;
	protected Bitmap temperaturesGraph, humidityGraph, pressureGraph, windSpeedsGraph, precipitationsGraph;
	protected TimeZone timeZone;
	protected Paint datePaint,
			structurePaint,
			primaryPaint,
			secondaryPaint,
			primaryGraphPaint,
			secondaryGraphPaint,
			tertiaryPaint,
			tertiaryGraphPaint,
			popBarGraphPaint,
			iconsPaint,
			sunIconPaint,
			moonLightIconPaint,
			moonShadowIconPaint;

	public ForecastView(Context context) {
		super(context);
		init(context);
	}

	public ForecastView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
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


	//  Graph generation functions
	//______________________________________________________________________________________________

	//  Generate a bitmap of GraphPath for two arrays
	//
	////    firstCurveData and secondCurveData must have the same number of elements !
	protected Bitmap generateBitmap2CurvesGraphPath(float[] firstCurveData, float[] secondCurveData, int width, int height, @NonNull Paint firstCurvePaint, @NonNull Paint secondCurvePaint) {

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

	//  Generate precipitations graph
	//
	////    rainData, snowData and popData must have the same number of elements !
	protected Bitmap generateBitmapPrecipitationsGraphPath(float[] rainData, float[] snowData, float[] popData, int width, int height, @NonNull Paint rainCurvePaint, @NonNull Paint snowCurvePaint, @NonNull Paint popCurvePaint) {

		//  Initializing graph paths
		Path rainCurvePath = new Path(),
				snowCurvePath = new Path(),
				popCurvePath = new Path();

		//  Searching for max and min values in arrays
		float minValue = rainData[0],
				maxValue = rainData[0];

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
		float connectionPointsX, point1_X, point1_Y, point2_Y, point1_Y_2, point2_X, point2_Y_2;
		float columnWidth = width / rainData.length,
				halfColumnWidth = columnWidth / 2F,
				halfPopBarWidth = columnWidth / 6F,
				//  To avoid curve trimming
				top = 4,
				bottom = height - 4,
				drawHeight = bottom - top;

		//  Clear each paths
		rainCurvePath.reset();
		snowCurvePath.reset();

		//  If min and max values are the same, the resulting curves are flat curves
		if (minValue != maxValue) {
			//  Position calculation for the first point
			point1_X = halfColumnWidth;
			point1_Y = bottom - drawHeight * (rainData[0] + addValueMinTo0) * scaleFactorY;
			point1_Y_2 = bottom - drawHeight * (snowData[0] + addValueMinTo0) * scaleFactorY;

			rainCurvePath.moveTo(0, point1_Y);
			snowCurvePath.moveTo(0, point1_Y_2);
			popCurvePath.moveTo(0, bottom);

			popCurvePath.addRect(point1_X - halfPopBarWidth, bottom - drawHeight * popData[0], point1_X + halfPopBarWidth, bottom, Path.Direction.CW);


			for (int index = 1; index < rainData.length; index++) {

				//  Position calculations
				point2_X = index * columnWidth + halfColumnWidth;
				point2_Y = bottom - drawHeight * (rainData[index] + addValueMinTo0) * scaleFactorY;
				point2_Y_2 = bottom - drawHeight * (snowData[index] + addValueMinTo0) * scaleFactorY;

				//  Middle connection point
				connectionPointsX = (point1_X + point2_X) / 2F;

				//  Extending each curves
				rainCurvePath.cubicTo(connectionPointsX, point1_Y, connectionPointsX, point2_Y, point2_X, point2_Y);
				snowCurvePath.cubicTo(connectionPointsX, point1_Y_2, connectionPointsX, point2_Y_2, point2_X, point2_Y_2);

				//  Add pop bar
				popCurvePath.addRect(point2_X - halfPopBarWidth, bottom - drawHeight * popData[index], point2_X + halfPopBarWidth, bottom, Path.Direction.CW);

				//  Moving to new points to old points
				point1_X = point2_X;
				point1_Y = point2_Y;
				point1_Y_2 = point2_Y_2;
			}

			rainCurvePath.lineTo(width, point1_Y);
			snowCurvePath.lineTo(width, point1_Y_2);
			rainCurvePath.moveTo(width, point1_Y);
			snowCurvePath.moveTo(width, point1_Y_2);
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
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawPath(popCurvePath, popCurvePaint);
		canvas.drawPath(rainCurvePath, rainCurvePaint);
		canvas.drawPath(snowCurvePath, snowCurvePaint);

		return returnedBitmap;
	}


	//  Draw temperatures max and min temps and
	protected void drawTextWithDrawable(@NonNull Canvas canvas, @NonNull Drawable drawable, @NonNull String text, @Px float top, @Px float left, @Px float spaceBetween, @NonNull Paint paint) {
		int deltaDrawableText = 5;
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


	protected void drawWeatherConditionIcons(Canvas canvas, int weatherCode, float top, float left, float width, float height, boolean isDayTime) {
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
		drawable.setBounds(BigDecimal.valueOf(left).intValue(), BigDecimal.valueOf(top).intValue(), BigDecimal.valueOf(left + width).intValue(), BigDecimal.valueOf(top + height).intValue());
		drawable.draw(canvas);
	}

	protected void drawWeatherConditionIcons(Canvas canvas, int weatherCode, float top, float left, float width, float height) {
		this.drawWeatherConditionIcons(canvas, weatherCode, top, left, width, height, true);
	}

	//  Draw UV index icon
	protected void drawUvIndex(Canvas canvas, int uvIndex, float x, float y, float sideLength) {
		float middle = sideLength / 2F,
				circleRadius = middle / 2F - 3;

		Bitmap sunBitmap = Bitmap.createBitmap((int) sideLength, (int) sideLength, Bitmap.Config.ARGB_4444);
		Canvas sunCanvas = new Canvas(sunBitmap);

		//  if the uv index is null, there is no sunrays
		if (uvIndex != 0) {
			float maxAngle = 6.28218F, deltaAngle = 0.392636F,
					startRadius = circleRadius + 8, stopRadius = middle - 1;
			float cosAngle, sinAngle;

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

	protected void drawWindDirectionIcon(Canvas canvas, float windDirection, float x, float y, float sideLength) {
		Bitmap compassBitmap = Bitmap.createBitmap((int) sideLength, (int) sideLength, Bitmap.Config.ARGB_4444);
		Canvas compassCanvas = new Canvas(compassBitmap);

		//  Do calculation for each points in clockwise order
		float point1_x, point1_y, point2_x, point2_y, point3_x, point3_y, point4_x, point4_y, middle;

		point1_x = sideLength * 0.5F;
		point1_y = sideLength * 0.15F;
		point2_x = sideLength * 0.85F;
		point2_y = sideLength * 0.85F;
		point3_x = sideLength * 0.5F;
		point3_y = sideLength * 0.60F;
		point4_x = sideLength * 0.15F;
		point4_y = sideLength * 0.85F;

		//  Rotate canvas with windDirection
		middle = sideLength / 2F;
		compassCanvas.rotate(windDirection, middle, middle);

		//  Draw each lines of wind direction arrow
		compassCanvas.drawLine(point1_x, point1_y, point3_x, point3_y, this.iconsPaint);
		compassCanvas.drawLine(point3_x, point3_y, point4_x, point4_y, this.iconsPaint);
		compassCanvas.drawLine(point4_x, point4_y, point1_x, point1_y, this.iconsPaint);
		compassCanvas.drawLine(point3_x, point3_y, point2_x, point2_y, this.iconsPaint);
		compassCanvas.drawLine(point2_x, point2_y, point1_x, point1_y, this.iconsPaint);

		canvas.drawBitmap(compassBitmap, x, y, null);
	}

	protected void init(Context context) {
		this.context = context;

		this.datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.structurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.primaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.secondaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.tertiaryGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.popBarGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.iconsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.sunIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

		this.popBarGraphPaint.setColor(getResources().getColor(R.color.colorAccent, null));
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
		this.sunIconPaint.setStrokeWidth(3);
		this.sunIconPaint.setAlpha(255);
		this.sunIconPaint.setTextSize(spToPx(15));
		this.sunIconPaint.setPathEffect(null);
		this.sunIconPaint.setStyle(Paint.Style.STROKE);
		this.sunIconPaint.setTextAlign(Paint.Align.CENTER);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
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
