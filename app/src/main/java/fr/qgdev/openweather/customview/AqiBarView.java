/*
 *  Copyright (c) 2019 - 2023
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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.qgdev.openweather.R;


/**
 * AqiBarView
 * <p>
 * AqiBarView is a custom view to display air quality index
 * with a the index value and a label
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see View
 */
public class AqiBarView extends View {
	
	private final float innerElementMargin = dpToPx(5);
	private final float barThickness = dpToPx(60);
	private final float textLabelSize = spToPx(20);
	
	private final float textValueSize = spToPx(23);
	private final float subtextValueSize = spToPx(10);
	private final float minWidth = dpToPx(200);
	protected Paint backgroundPaint;
	protected Paint mainElementPaint;
	protected Paint labelTextPaint;
	protected Paint valuePaint;
	protected Paint subValuePaint;
	private int value = 1;
	private String labelText = "N/A";
	
	/**
	 * AirQualityView Constructor
	 * <p>
	 * Just build AirQualityView object only with context
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 */
	public AqiBarView(@NonNull Context context) {
		super(context);
		initComponents(null);
	}
	
	
	/**
	 * AirQualityView Constructor
	 * <p>
	 * Just build AirQualityView object only with Context and AttributeSet
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 * @param attrs   AttributeSet for the gaugeView
	 */
	public AqiBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initComponents(attrs);
		if (!this.isInEditMode()) {
			initValuesFromAttributes(context, attrs);
		}
	}
	
	/**
	 * AirQualityView Constructor
	 * <p>
	 * Just build AirQualityView object only with Context, AttributeSet and defStyleAttr
	 * </p>
	 *
	 * @param context      Current context, only used to construct super class
	 * @param attrs        AttributeSet for the gaugeView
	 * @param defStyleAttr Default style attribute for the gaugeView
	 */
	public AqiBarView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initComponents(attrs);
		if (!this.isInEditMode()) {
			initValuesFromAttributes(context, attrs);
		}
	}
	
	/**
	 * initComponents()
	 * <p>
	 * Used to initialize attributes used to draw a gauge
	 * </p>
	 **/
	private void initComponents(@Nullable AttributeSet attrs) {
		backgroundPaint = new Paint();
		mainElementPaint = new Paint();
		labelTextPaint = new Paint();
		valuePaint = new Paint();
		subValuePaint = new Paint();
		
		int colorBackground = Color.WHITE;
		int colorMainElement = Color.GRAY;
		int colorText = Color.BLACK;
		int colorSubText = Color.GRAY;
		
		if (attrs != null) {
			TypedArray a = getContext().getTheme().obtainStyledAttributes(
					  attrs,
					  R.styleable.AqiBarView,
					  0, 0);
			
			try {
				if (a.hasValue(R.styleable.AqiBarView_mainColor)) {
					colorMainElement = a.getColor(R.styleable.AqiBarView_mainColor, colorBackground);
				}
				if (a.hasValue(R.styleable.AqiBarView_backgroundColor)) {
					colorBackground = a.getColor(R.styleable.AqiBarView_backgroundColor, colorMainElement);
				}
				if (a.hasValue(R.styleable.AqiBarView_textColor)) {
					colorText = a.getColor(R.styleable.AqiBarView_textColor, colorText);
				}
				if (a.hasValue(R.styleable.AqiBarView_subtextColor)) {
					colorSubText = a.getColor(R.styleable.AqiBarView_subtextColor, colorSubText);
				}
			} finally {
				a.recycle();
			}
		}
		
		backgroundPaint.setColor(colorBackground);
		backgroundPaint.setStrokeWidth(0.65F);
		backgroundPaint.setAlpha(255);
		backgroundPaint.setTextAlign(Paint.Align.CENTER);
		backgroundPaint.setStyle(Paint.Style.FILL);
		
		mainElementPaint.setColor(colorMainElement);
		mainElementPaint.setStrokeWidth(3);
		mainElementPaint.setAlpha(255);
		mainElementPaint.setPathEffect(null);
		mainElementPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		labelTextPaint.setColor(colorText);
		labelTextPaint.setStrokeWidth(0.65F);
		labelTextPaint.setAlpha(255);
		labelTextPaint.setTextSize(textLabelSize);
		labelTextPaint.setTextAlign(Paint.Align.CENTER);
		
		valuePaint.setColor(colorText);
		valuePaint.setStrokeWidth(0.65F);
		valuePaint.setAlpha(255);
		valuePaint.setTextSize(textValueSize);
		valuePaint.setTextAlign(Paint.Align.CENTER);
		
		subValuePaint.setColor(colorSubText);
		subValuePaint.setStrokeWidth(0.65F);
		subValuePaint.setAlpha(255);
		subValuePaint.setTextSize(subtextValueSize);
		subValuePaint.setTextAlign(Paint.Align.CENTER);
		
		if (isInEditMode()) {
			labelTextPaint.setTypeface(Typeface.DEFAULT);
			valuePaint.setTypeface(Typeface.DEFAULT);
		}
	}
	
	
	/**
	 * initValuesFromAttributes(Context context, AttributeSet attrs)
	 * <p>
	 * Used to retrieve attributes from the AttributeSet
	 * and initialize the gauge with these values
	 * if they are present
	 * else use default values
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 * @param attrs   AttributeSet for the gaugeView
	 */
	private void initValuesFromAttributes(@NonNull Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AqiBarView);
		
		//	Check if the labelText is present
		if (typedArray.hasValue(R.styleable.GaugeBarView_labelText)) {
			labelText = typedArray.getString(R.styleable.GaugeBarView_labelText);
		} else {
			labelText = "N/A";
		}
		
		//	Check if the value is present
		if (typedArray.hasValue(R.styleable.AqiBarView_indexValue)) {
			value = typedArray.getInt(R.styleable.AqiBarView_indexValue, 0);
		} else {
			value = 0;
		}
		
		typedArray.recycle();
	}
	
	
	/**
	 * setIndexValue(int value)
	 * <p>
	 * Set the value of the gauge bar
	 * and redraw it
	 * </p>
	 *
	 * @param value The value to be set
	 */
	public void setIndexValue(int value) {
		this.value = value;
		invalidate();
	}
	
	/**
	 * setIndexPaintColor(int color)
	 * <p>
	 * Set the color of the gauge bar
	 * and redraw it
	 * </p>
	 *
	 * @param color The color to be set
	 */
	public void setIndexPaintColor(int color) {
		mainElementPaint.setColor(getResources().getColor(color, null));
		invalidate();
	}
	
	/**
	 * setLabelText(String labelText)
	 * <p>
	 * Set the text of the label
	 * and redraw it
	 * </p>
	 *
	 * @param labelText The text label to be set
	 */
	public void setLabelText(String labelText) {
		this.labelText = labelText;
		invalidate();
	}
	
	/**
	 * onDraw(@NonNull Canvas canvas)
	 * <p>
	 * Called to generate view
	 * </p>
	 *
	 * @param canvas The canvas that will be displayed on screen
	 * @see View
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);
		
		float halfBarThickness = barThickness / 2;
		
		float barXLeft = getPaddingLeft() + halfBarThickness;
		float barXRight = getWidth() - getPaddingRight() - halfBarThickness;
		
		float barYTop = getPaddingTop();
		float barYBottom = barYTop + barThickness;
		float barYCenter = barYTop + halfBarThickness;
		
		canvas.drawRect(barXLeft, barYTop, barXRight, barYBottom, mainElementPaint);
		canvas.drawCircle(barXLeft, barYCenter, halfBarThickness, mainElementPaint);
		canvas.drawCircle(barXRight, barYCenter, halfBarThickness, mainElementPaint);
		
		//	Draw the AQI
		canvas.drawCircle(barXLeft, barYCenter, halfBarThickness - innerElementMargin, backgroundPaint);
		canvas.drawText(String.valueOf(value), barXLeft, barYCenter + innerElementMargin, valuePaint);
		canvas.drawText("AQI", barXLeft, barYCenter + innerElementMargin + textValueSize / 1.5F, subValuePaint);
		
		//	Draw the label
		float barXLeftLabel = barXLeft + barThickness;
		
		float labelX = (barXLeftLabel + barXRight) / 2;
		canvas.drawRect(barXLeftLabel, barYTop + innerElementMargin, barXRight, barYBottom - innerElementMargin, backgroundPaint);
		canvas.drawCircle(barXLeftLabel, barYCenter, halfBarThickness - innerElementMargin, backgroundPaint);
		canvas.drawCircle(barXRight, barYCenter, halfBarThickness - innerElementMargin, backgroundPaint);
		canvas.drawText(labelText, labelX, barYCenter + innerElementMargin + textLabelSize / 10, labelTextPaint);
	}
	
	/**
	 * onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	 * <p>
	 * Called to measure the view
	 * </p>
	 *
	 * @param widthMeasureSpec  The width of the view
	 * @param heightMeasureSpec The height of the view
	 * @see View
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		if (widthSize < minWidth) widthSize = (int) minWidth;
		int heightSize = (int) (barThickness);
		
		setMeasuredDimension(widthSize, heightSize);
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
	protected float spToPx(float sp) {
		return TypedValue.applyDimension(
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
	 * @param dip DP value that you want to convert
	 * @return The DP converted value into PX
	 */
	protected float dpToPx(float dip) {
		return TypedValue.applyDimension(
				  TypedValue.COMPLEX_UNIT_DIP,
				  dip,
				  getResources().getDisplayMetrics());
	}
}

