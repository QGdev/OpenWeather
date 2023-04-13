package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * The type Air quality.
 */
@Entity(tableName = "air_quality")
public class AirQuality {
	
	@PrimaryKey
	private int placeId;
	private int aqi;
	private float co;
	private float no;
	private float no2;
	private float o3;
	private float so2;
	private float pm2_5;
	private float pm10;
	private float nh3;
	
	/**
	 * Instantiates a new Air quality.
	 */
	public AirQuality() {
		this.aqi = 0;
		this.co = 0;
		this.no = 0;
		this.no2 = 0;
		this.o3 = 0;
		this.so2 = 0;
		this.pm2_5 = 0;
		this.pm10 = 0;
		this.nh3 = 0;
	}
	
	/**
	 * Instantiates a new Air quality with JSON from OpenWeatherMap.
	 *
	 * @param airQuality the air quality JSON Object from OpenWeatherMap
	 * @throws JSONException
	 */
	@Ignore
	public AirQuality(JSONObject airQuality) throws JSONException {
		JSONObject content = airQuality.getJSONArray("list").getJSONObject(0);
		this.aqi = content.getJSONObject("main").getInt("aqi");
		
		JSONObject componentsJSON = content.getJSONObject("components");
		this.co = BigDecimal.valueOf(componentsJSON.getDouble("co")).floatValue();
		this.no = BigDecimal.valueOf(componentsJSON.getDouble("no")).floatValue();
		this.no2 = BigDecimal.valueOf(componentsJSON.getDouble("no2")).floatValue();
		this.o3 = BigDecimal.valueOf(componentsJSON.getDouble("o3")).floatValue();
		this.so2 = BigDecimal.valueOf(componentsJSON.getDouble("so2")).floatValue();
		this.pm2_5 = BigDecimal.valueOf(componentsJSON.getDouble("pm2_5")).floatValue();
		this.pm10 = BigDecimal.valueOf(componentsJSON.getDouble("pm10")).floatValue();
		this.nh3 = BigDecimal.valueOf(componentsJSON.getDouble("nh3")).floatValue();
	}
	
	
	/**
	 * Gets place id.
	 *
	 * @return the place id
	 */
	public int getPlaceId() {
		return placeId;
	}
	
	/**
	 * Sets place id.
	 *
	 * @param placeId the place id
	 */
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	/**
	 * Gets the Air Quality Index.
	 *
	 * @return the Air Quality Index
	 */
	public int getAqi() {
		return aqi;
	}
	
	/**
	 * Sets the Air Quality Index.
	 *
	 * @param aqi the Air Quality Index
	 */
	public void setAqi(int aqi) {
		this.aqi = aqi;
	}
	
	/**
	 * Gets the Carbon Monoxide concentration.
	 *
	 * @return the Carbon Monoxide concentration
	 */
	public float getCo() {
		return co;
	}
	
	/**
	 * Sets the Carbon Monoxide concentration.
	 *
	 * @param co the Carbon Monoxide concentration
	 */
	public void setCo(float co) {
		this.co = co;
	}
	
	/**
	 * Gets the Nitrogen Monoxide concentration.
	 *
	 * @return the Nitrogen Monoxide concentration
	 */
	public float getNo() {
		return no;
	}
	
	/**
	 * Sets the Nitrogen Monoxide concentration.
	 *
	 * @param no the Nitrogen Monoxide concentration
	 */
	public void setNo(float no) {
		this.no = no;
	}
	
	/**
	 * Gets the Nitrogen Dioxide concentration.
	 *
	 * @return the Nitrogen Dioxide concentration
	 */
	public float getNo2() {
		return no2;
	}
	
	/**
	 * Sets the Nitrogen Dioxide concentration.
	 *
	 * @param no2 the Nitrogen Dioxide concentration
	 */
	public void setNo2(float no2) {
		this.no2 = no2;
	}
	
	/**
	 * Gets the Ozone concentration.
	 *
	 * @return the Ozone concentration
	 */
	public float getO3() {
		return o3;
	}
	
	/**
	 * Sets the Ozone concentration.
	 *
	 * @param o3 the Ozone concentration
	 */
	public void setO3(float o3) {
		this.o3 = o3;
	}
	
	/**
	 * Gets the Sulfur Dioxide concentration.
	 *
	 * @return the Sulfur Dioxide concentration
	 */
	public float getSo2() {
		return so2;
	}
	
	/**
	 * Sets the Sulfur Dioxide concentration.
	 *
	 * @param so2 the Sulfur Dioxide concentration
	 */
	public void setSo2(float so2) {
		this.so2 = so2;
	}
	
	/**
	 * Gets the Particulate Matter 2.5 concentration.
	 *
	 * @return the Particulate Matter 2.5 concentration
	 */
	public float getPm2_5() {
		return pm2_5;
	}
	
	/**
	 * Sets the Particulate Matter 2.5 concentration.
	 *
	 * @param pm2_5 the Particulate Matter 2.5 concentration
	 */
	public void setPm2_5(float pm2_5) {
		this.pm2_5 = pm2_5;
	}
	
	/**
	 * Gets the Particulate Matter 10 concentration.
	 *
	 * @return the Particulate Matter 10 concentration
	 */
	public float getPm10() {
		return pm10;
	}
	
	/**
	 * Sets the Particulate Matter 10 concentration.
	 *
	 * @param pm10 the Particulate Matter 10 concentration
	 */
	public void setPm10(float pm10) {
		this.pm10 = pm10;
	}
	
	
	/**
	 * Gets the Ammonia concentration.
	 *
	 * @return the Ammonia concentration
	 */
	public float getNh3() {
		return nh3;
	}
	
	/**
	 * Sets the Ammonia concentration.
	 *
	 * @param nh3 the Ammonia concentration
	 */
	public void setNh3(float nh3) {
		this.nh3 = nh3;
	}
	
	/**
	 * Clones the object.
	 *
	 * @return the cloned object
	 */
	@Override
	public AirQuality clone() {
		AirQuality clonedObject = new AirQuality();
		clonedObject.placeId = placeId;
		clonedObject.aqi = this.aqi;
		clonedObject.co = this.co;
		clonedObject.no = this.no;
		clonedObject.no2 = this.no2;
		clonedObject.o3 = this.o3;
		clonedObject.so2 = this.so2;
		clonedObject.pm2_5 = this.pm2_5;
		clonedObject.pm10 = this.pm10;
		clonedObject.nh3 = this.nh3;
		
		return clonedObject;
	}
	
	/**
	 * Gets the string representation of the object.
	 *
	 * @return the string representation of the object
	 */
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", AirQuality.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("aqi=" + aqi)
				  .add("co=" + co)
				  .add("no=" + no)
				  .add("no2=" + no2)
				  .add("o3=" + o3)
				  .add("so2=" + so2)
				  .add("pm2_5=" + pm2_5)
				  .add("pm10=" + pm10)
				  .add("nh3=" + nh3)
				  .toString();
	}
}