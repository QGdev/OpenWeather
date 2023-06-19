package fr.qgdev.openweather.customview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.math.BigDecimal;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.qgdev.openweather.R;


/**
 * GaugeBarView
 * <p>
 * A custom view to draw a gauge bar
 * with a cursor, label and custom sections
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @see View
 */
public class GaugeBarView extends View {
	
	private final float leftGaugeMargin = dpToPx(60);
	private final float rightGaugeMargin = dpToPx(80);
	private final float verticalGaugeMargins = dpToPx(8);
	private final float textHorizontalMargins = dpToPx(15);
	private final float barThickness = dpToPx(8);
	private final float textSize = spToPx(15);
	
	private final float minHeight = dpToPx(20);
	private final float minWidth = dpToPx(200);
	protected Paint cursorPaint;
	protected Paint iconsPaint;
	protected Paint labelTextPaint;
	protected Paint valuePaint;
	private float value = 50;
	private String labelText = "O²";
	private Paint[] sectionsPaints = new Paint[]{
			  getNewSectionPaint(android.R.color.holo_green_light),
			  getNewSectionPaint(android.R.color.holo_orange_light),
			  getNewSectionPaint(android.R.color.holo_red_light)};
	private float[] sectionsBoundaries = new float[]{0, 25, 75};
	
	/**
	 * AirQualityView Constructor
	 * <p>
	 * Just build AirQualityView object only with context
	 * </p>
	 *
	 * @param context Current context, only used to construct super class
	 */
	public GaugeBarView(@NonNull Context context) {
		super(context);
		initComponents();
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
	public GaugeBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initComponents();
		if (!this.isInEditMode()) {
			initSectionsFromAttributes(context, attrs);
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
	public GaugeBarView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initComponents();
		if (!this.isInEditMode()) {
			initSectionsFromAttributes(context, attrs);
			initValuesFromAttributes(context, attrs);
		}
	}
	
	/**
	 * initComponents()
	 * <p>
	 * Used to initialize attributes used to draw a gauge
	 * </p>
	 **/
	private void initComponents() {
		cursorPaint = new Paint();
		iconsPaint = new Paint();
		labelTextPaint = new Paint();
		valuePaint = new Paint();
		
		cursorPaint.setColor(Color.WHITE);
		cursorPaint.setAlpha(255);
		cursorPaint.setStrokeWidth(0.65F);
		cursorPaint.setTextAlign(Paint.Align.CENTER);
		cursorPaint.setStyle(Paint.Style.FILL);
		cursorPaint.setShadowLayer(6, 0, 0, Color.BLACK);
		setLayerType(LAYER_TYPE_SOFTWARE, cursorPaint);
		
		iconsPaint.setColor(getResources().getColor(R.color.colorIcons, null));
		iconsPaint.setStrokeWidth(3);
		iconsPaint.setAlpha(255);
		iconsPaint.setPathEffect(null);
		iconsPaint.setStyle(Paint.Style.STROKE);
		
		labelTextPaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		labelTextPaint.setStrokeWidth(0.65F);
		labelTextPaint.setAlpha(255);
		labelTextPaint.setTextSize(textSize);
		labelTextPaint.setTextAlign(Paint.Align.RIGHT);
		
		valuePaint.setColor(getResources().getColor(R.color.colorFirstText, null));
		valuePaint.setStrokeWidth(0.65F);
		valuePaint.setAlpha(255);
		valuePaint.setTextSize(textSize);
		valuePaint.setTextAlign(Paint.Align.LEFT);
		
		if (isInEditMode()) {
			labelTextPaint.setTypeface(Typeface.DEFAULT);
			valuePaint.setTypeface(Typeface.DEFAULT);
		}
	}
	
	/**
	 * getNewSectionPaint(int color)
	 * <p>
	 * Used to get a new Paint object
	 * with the color passed as parameter
	 * and the style FILL
	 * and the stroke width 0.65F
	 * and the alpha 255
	 * </p>
	 *
	 * @param color The color of the Paint object
	 */
	private Paint getNewSectionPaint(int color) {
		Paint paint = new Paint();
		paint.setColor(getResources().getColor(color, null));
		paint.setStrokeWidth(0.65F);
		paint.setAlpha(255);
		paint.setStyle(Paint.Style.FILL);
		return paint;
	}
	
	private void initSectionsFromAttributes(@NonNull Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GaugeBarView);
		
		
		//	Sections settings
		//		Check if the sections settings are present
		//		The view requires both sectionsBoundaries and sectionsColors
		
		//	If both attributes are present, we can retrieve them
		//		Otherwise, we throw an exception
		if (!(typedArray.hasValue(R.styleable.GaugeBarView_sectionsBoundaries)
				  && typedArray.hasValue(R.styleable.GaugeBarView_sectionsColors))) {
			throw new IllegalArgumentException("You must specify sectionsBoundaries and sectionsColors attributes");
		}
		
		//	Retrieve sections boundaries
		TypedArray tmp = getResources().obtainTypedArray(typedArray
				  .getResourceId(R.styleable.GaugeBarView_sectionsBoundaries, 0));
		
		// Check if the array has at least two values
		if (tmp.length() < 2) {
			throw new IllegalArgumentException("sectionsBoundaries, array must have at least two values");
		}
		
		sectionsBoundaries = new float[tmp.length()];
		
		// Iterate through the TypedArray and retrieve the values
		// Check if the values are in ascending order
		
		// Retrieve the first value
		sectionsBoundaries[0] = tmp.getFloat(0, 0.0f);
		float lastValue = sectionsBoundaries[0];
		
		// Retrieve the other values
		for (int i = 1; i < tmp.length(); i++) {
			sectionsBoundaries[i] = tmp.getFloat(i, 0.0f);
			
			if (sectionsBoundaries[i] > lastValue) lastValue = sectionsBoundaries[i];
			else throw new IllegalArgumentException("sectionsBoundaries must be in ascending order");
		}
		
		tmp.recycle();
		
		//	Retrieve sections colors
		tmp = getResources().obtainTypedArray(typedArray
				  .getResourceId(R.styleable.GaugeBarView_sectionsColors, 0));
		
		// Check if the array has at least two values
		if (tmp.length() < 2) {
			throw new IllegalArgumentException("sectionsColors, array must have at least two values");
		}
		
		//	Check if sectionsBoundaries and sectionsColors have the same length
		if (sectionsBoundaries.length != tmp.length()) {
			throw new IllegalArgumentException("sectionsBoundaries and sectionsColors must have the same length");
		}
		
		sectionsPaints = new Paint[tmp.length()];
		
		// Iterate through the TypedArray and retrieve the values
		for (int i = 0; i < tmp.length(); i++) {
			sectionsPaints[i] = getNewSectionPaint(tmp.getResourceId(i, 0));
		}
		
		tmp.recycle();
		typedArray.recycle();
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
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GaugeBarView);
		
		//	Check if the labelText is present
		if (typedArray.hasValue(R.styleable.GaugeBarView_labelText)) {
			labelText = typedArray.getString(R.styleable.GaugeBarView_labelText);
		} else {
			labelText = "";
		}
		
		//	Check if the value is present
		if (typedArray.hasValue(R.styleable.GaugeBarView_value)) {
			value = typedArray.getFloat(R.styleable.GaugeBarView_value, 0);
		} else {
			value = 0;
		}
		
		typedArray.recycle();
	}
	
	
	/**
	 * setValue(float value)
	 * <p>
	 * Set the value of the gauge bar
	 * and redraw it
	 * </p>
	 *
	 * @param value The value to be set
	 */
	public void setValue(float value) {
		this.value = value;
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
	 * setGaugeSections(int[] sectionsBoundaries, int[] sectionsColors)
	 * <p>
	 * Set the boundaries and each colors of the gauge gauge sections
	 * and redraw it
	 * </p>
	 *
	 * @param sectionsBoundaries An array of the boundaries of each sections
	 *                           Need to be in ascending order
	 *                           like this : [0, 50, 100, 150, 250]
	 *                           Last upper bound is infinite so no need to precise it
	 * @param sectionsColors     An array of the colors of each sections
	 *                           Need to be actual resources colors
	 *                           like this : [R.color.red, R.color.green, R.color.blue, R.color.yellow]
	 */
	public void setGaugeSections(@NonNull float[] sectionsBoundaries, @NonNull int[] sectionsColors) {
		if (sectionsBoundaries.length != sectionsColors.length)
			throw new IllegalArgumentException("sectionsBoundaries and sectionsColors must have the same length");
		
		//	Need to check if given sectionsBoundaries are in ascending order
		for (int i = 0; i < sectionsBoundaries.length - 1; i++) {
			if (sectionsBoundaries[i] > sectionsBoundaries[i + 1])
				throw new IllegalArgumentException(String.format("sectionsBoundaries[%d] must be inferior or equal to sectionsBoundaries[%d]", i, i + 1));
		}
		
		//	Need to check if given sectionsColors are actual resources colors
		for (int i = 0; i < sectionsColors.length; i++) {
			try {
				getResources().getColor(sectionsColors[i], null);
			} catch (Resources.NotFoundException e) {
				throw new IllegalArgumentException(String.format("sectionsColors[%d] is not a valid color resource", i));
			}
		}
		
		this.sectionsBoundaries = sectionsBoundaries.clone();
		
		this.sectionsPaints = new Paint[sectionsColors.length];
		for (int i = 0; i < sectionsColors.length; i++) {
			sectionsPaints[i] = getNewSectionPaint(sectionsColors[i]);
		}
		invalidate();
	}
	
	
	/**
	 * setGaugeSections(int[] sectionsBoundaries, int[] sectionsColors, int[] sectionsIcons)
	 * <p>
	 * Set the boundaries, each colors and each icons of the gauge gauge sections
	 * and redraw it
	 * </p>
	 *
	 * @param gaugeBarXLeft  The left X coordinate of the gauge bar
	 * @param gaugeBarXRight The right X coordinate of the gauge bar
	 * @param sectionWidth   The width of each section
	 * @return The X coordinate of the cursor
	 */
	private float computeGaugeCursorX(float gaugeBarXLeft, float gaugeBarXRight, float sectionWidth) {
		float cursorX;
		float halfGaugeBarThickness = barThickness / 2F;
		
		int finalLowerBound = 0;
		
		while (finalLowerBound < sectionsBoundaries.length - 1 && sectionsBoundaries[finalLowerBound + 1] <= value) {
			finalLowerBound++;
		}
		
		int upperBound = finalLowerBound + 1;
		
		float slope;
		
		if (upperBound < sectionsBoundaries.length) {
			slope = sectionsBoundaries[upperBound] - sectionsBoundaries[finalLowerBound];
		} else {
			slope = sectionsBoundaries[sectionsBoundaries.length - 1] / sectionsBoundaries.length;
		}
		cursorX = gaugeBarXLeft + halfGaugeBarThickness;
		cursorX += sectionWidth * finalLowerBound;
		cursorX += (int) ((value - sectionsBoundaries[finalLowerBound]) * sectionWidth / slope);
		
		if (cursorX < gaugeBarXLeft + halfGaugeBarThickness) {
			cursorX = gaugeBarXLeft + halfGaugeBarThickness;
		} else if (cursorX > gaugeBarXRight - halfGaugeBarThickness) {
			cursorX = gaugeBarXRight - halfGaugeBarThickness;
		}
		
		return cursorX;
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
		
		float halfGaugeBarThickness = barThickness / 2;
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		float gaugeBarWidth = width - leftGaugeMargin - rightGaugeMargin;
		
		float gaugeBarXLeft = leftGaugeMargin;
		float gaugeBarXRight = width - rightGaugeMargin;
		
		float gaugeBarYTop = verticalGaugeMargins;
		float gaugeBarYMiddle = gaugeBarYTop + halfGaugeBarThickness;
		float gaugeBarYBottom = gaugeBarYTop + barThickness;
		
		float sectionWidth = gaugeBarWidth / sectionsPaints.length;
		float edgeSectionWidth = sectionWidth - halfGaugeBarThickness;
		
		float textLabelX = gaugeBarXLeft - textHorizontalMargins;
		float textValueX = gaugeBarXRight + textHorizontalMargins;
		int textY = BigDecimal.valueOf(gaugeBarYMiddle + textSize / 3F).intValue();
		
		float cursorX = computeGaugeCursorX(gaugeBarXLeft, gaugeBarXRight, sectionWidth);
		
		// Draw the gauge bar
		float currentX = gaugeBarXLeft + halfGaugeBarThickness;
		int index = 0;
		
		canvas.drawRect(currentX, gaugeBarYTop, currentX + edgeSectionWidth, gaugeBarYBottom, sectionsPaints[index++]);
		currentX += edgeSectionWidth;
		
		while (index < sectionsBoundaries.length - 1) {
			canvas.drawRect(currentX, gaugeBarYTop, currentX + sectionWidth, gaugeBarYBottom, sectionsPaints[index]);
			currentX += sectionWidth;
			index++;
		}
		
		canvas.drawRect(currentX, gaugeBarYTop, currentX + edgeSectionWidth, gaugeBarYBottom, sectionsPaints[index]);
		
		canvas.drawCircle(gaugeBarXLeft + halfGaugeBarThickness, gaugeBarYMiddle, halfGaugeBarThickness, sectionsPaints[0]);
		canvas.drawCircle(gaugeBarXRight - halfGaugeBarThickness, gaugeBarYMiddle, halfGaugeBarThickness, sectionsPaints[sectionsPaints.length - 1]);
		
		canvas.drawText(labelText, textLabelX, textY, labelTextPaint);
		canvas.drawText(String.valueOf(value), textValueX, textY, valuePaint);
		canvas.drawCircle(cursorX, gaugeBarYMiddle, halfGaugeBarThickness + 4F, cursorPaint);
	}
	
	/**
	 * onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	 * <p>
	 * Called to measure the view
	 * </p>
	 *
	 * @param widthMeasureSpec  The width of the view
	 * @param heightMeasureSpec The height of the view
	 * @see android.view.View
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		if (widthSize < minWidth) widthSize = (int) minWidth;
		if (heightSize < minHeight) heightSize = (int) minHeight;
		
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

