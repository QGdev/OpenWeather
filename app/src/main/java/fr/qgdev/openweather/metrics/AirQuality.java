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

package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * AirQuality
 * <p>
 *    A data holder class for Air Quality data
 *    It contains the Air Quality Index and the concentration of different pollutants
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Entity(tableName = "air_quality",
		  primaryKeys = {"placeId"})
public class AirQuality {
	@NonNull
	private String placeId;
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
		this.placeId = "";
		this.aqi = 1;
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
		setAqi(content.getJSONObject("main").getInt("aqi"));
		
		JSONObject componentsJSON = content.getJSONObject("components");
		setCo(BigDecimal.valueOf(componentsJSON.getDouble("co")).floatValue());
		setNo(BigDecimal.valueOf(componentsJSON.getDouble("no")).floatValue());
		setNo2(BigDecimal.valueOf(componentsJSON.getDouble("no2")).floatValue());
		setO3(BigDecimal.valueOf(componentsJSON.getDouble("o3")).floatValue());
		setSo2(BigDecimal.valueOf(componentsJSON.getDouble("so2")).floatValue());
		setPm2_5(BigDecimal.valueOf(componentsJSON.getDouble("pm2_5")).floatValue());
		setPm10(BigDecimal.valueOf(componentsJSON.getDouble("pm10")).floatValue());
		setNh3(BigDecimal.valueOf(componentsJSON.getDouble("nh3")).floatValue());
	}
	
	/*
	  Getters and Setters
	 */
	
	/**
	 * Gets place id.
	 *
	 * @return the place id
	 */
	public String getPlaceId() {
		return placeId;
	}
	
	/**
	 * Sets place id.
	 *
	 * @param placeId the place id
	 */
	public void setPlaceId(String placeId) {
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
	 * Must be between 1 and 5.
	 *
	 * @param aqi the Air Quality Index
	 * @throws IllegalArgumentException if the AQI is not between 1 and 5
	 */
	public void setAqi(int aqi) {
		if (aqi < 1 || aqi > 5) throw new IllegalArgumentException("AQI must be between 1 and 5");
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
	 * Must be null or positive.
	 *
	 * @param co the Carbon Monoxide concentration
	 * @throws IllegalArgumentException if the CO is negative
	 */
	public void setCo(float co) {
		if (co < 0.0f) throw new IllegalArgumentException("CO must be positive");
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
	 * Must be null or positive.
	 *
	 * @param no the Nitrogen Monoxide concentration
	 * @throws IllegalArgumentException if the NO is negative
	 */
	public void setNo(float no) {
		if (no < 0.0f) throw new IllegalArgumentException("NO must be positive");
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
	 * Must be null or positive.
	 *
	 * @param no2 the Nitrogen Dioxide concentration
	 * @throws IllegalArgumentException if the NO2 is negative
	 */
	public void setNo2(float no2) {
		if (no2 < 0.0f) throw new IllegalArgumentException("NO2 must be positive");
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
	 * Must be null or positive.
	 *
	 * @param o3 the Ozone concentration
	 * @throws IllegalArgumentException if the O3 is negative
	 */
	public void setO3(float o3) {
		if (o3 < 0.0f) throw new IllegalArgumentException("O3 must be positive");
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
	 * Must be null or positive.
	 *
	 * @param so2 the Sulfur Dioxide concentration
	 * @throws IllegalArgumentException if the SO2 is negative
	 */
	public void setSo2(float so2) {
		if (so2 < 0.0f) throw new IllegalArgumentException("SO2 must be positive");
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
	 * Must be null or positive.
	 *
	 * @param pm2_5 the Particulate Matter 2.5 concentration
	 * @throws IllegalArgumentException if the PM2.5 is negative
	 */
	public void setPm2_5(float pm2_5) {
		if (pm2_5 < 0.0f) throw new IllegalArgumentException("PM2.5 must be positive");
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
	 * Must be null or positive.
	 *
	 * @param pm10 the Particulate Matter 10 concentration
	 * @throws IllegalArgumentException if the PM10 is negative
	 */
	public void setPm10(float pm10) {
		if (pm10 < 0.0f) throw new IllegalArgumentException("PM10 must be positive");
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
	 * Must be null or positive.
	 *
	 * @param nh3 the Ammonia concentration
	 * @throws IllegalArgumentException if the NH3 is negative
	 */
	public void setNh3(float nh3) {
		if (nh3 < 0.0f) throw new IllegalArgumentException("NH3 must be positive");
		this.nh3 = nh3;
	}
	
	/**
	 * Clones the object.
	 *
	 * @return the cloned object
	 */
	@NonNull
	@Override
	public AirQuality clone() {
		AirQuality clonedObject = new AirQuality();
		clonedObject.setPlaceId(placeId);
		clonedObject.setAqi(aqi);
		clonedObject.setCo(co);
		clonedObject.setNo(no);
		clonedObject.setNo2(no2);
		clonedObject.setO3(o3);
		clonedObject.setSo2(so2);
		clonedObject.setPm2_5(pm2_5);
		clonedObject.setPm10(pm10);
		clonedObject.setNh3(nh3);
		
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