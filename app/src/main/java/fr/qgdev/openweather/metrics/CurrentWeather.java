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

package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;


@Entity(tableName = "current_weather",
		  primaryKeys = {"placeId"})

public class CurrentWeather {
	private int placeId;
	
	private long dt;
	
	private String weather;
	private String weatherDescription;
	private int weatherCode;
	
	private float temperature;
	private float temperatureFeelsLike;
	
	private int pressure;
	private int humidity;
	private float dewPoint;
	
	private int cloudiness;
	private int uvIndex;
	private int visibility;
	
	private long sunrise;
	private long sunset;
	
	private float windSpeed;
	private float windGustSpeed;
	private boolean isWindDirectionReadable;
	private short windDirection;
	
	private float rain;
	private float snow;
	
	public CurrentWeather() {
		this.dt = 0;
		
		this.weather = "";
		this.weatherDescription = "";
		this.weatherCode = 0;
		
		this.temperature = 0;
		this.temperatureFeelsLike = 0;
		
		this.pressure = 0;
		this.humidity = 0;
		this.dewPoint = 0;
		
		this.cloudiness = 0;
		this.uvIndex = 0;
		this.visibility = 0;
		
		this.sunrise = 0;
		this.sunset = 0;
		
		this.windSpeed = 0;
		this.windGustSpeed = 0;
		this.isWindDirectionReadable = false;
		this.windDirection = 0;
		
		this.rain = 0;
		this.snow = 0;
	}
	
	public CurrentWeather(JSONObject currentWeather) throws JSONException {
		//  The time of this update
		this.dt = currentWeather.getLong("dt") * 1000;
		
		//    Weather descriptions
		JSONObject currentWeatherDescriptionsJSON = currentWeather.getJSONArray("weather").getJSONObject(0);    //  Get only the first station
		this.weather = currentWeatherDescriptionsJSON.getString("main");
		this.weatherDescription = currentWeatherDescriptionsJSON.getString("description");
		this.weatherCode = currentWeatherDescriptionsJSON.getInt("id");
		
		//    Temperatures
		this.temperature = BigDecimal.valueOf(currentWeather.getDouble("temp")).floatValue();
		this.temperatureFeelsLike = BigDecimal.valueOf(currentWeather.getDouble("feels_like")).floatValue();
		
		//    Pressure, Humidity, dewPoint, uvIndex
		this.pressure = currentWeather.getInt("pressure");
		this.humidity = currentWeather.getInt("humidity");
		this.dewPoint = BigDecimal.valueOf(currentWeather.getDouble("dew_point")).floatValue();
		
		if (currentWeather.has("uvi")) {
			this.uvIndex = currentWeather.getInt("uvi");
		} else {
			this.uvIndex = 0;
		}
		
		//    Sky informations
		this.cloudiness = currentWeather.getInt("clouds");
		this.visibility = currentWeather.getInt("visibility");
		this.sunrise = currentWeather.getLong("sunrise") * 1000;
		this.sunset = currentWeather.getLong("sunset") * 1000;
		
		//    Wind informations
		this.windSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_speed")).floatValue();
		
		////  Enough wind for a viable wind direction information
		this.isWindDirectionReadable = currentWeather.has("wind_deg");
		if (this.isWindDirectionReadable) {
			this.windDirection = BigDecimal.valueOf(currentWeather.getInt("wind_deg")).shortValue();
		}
		////    Wind Gusts
		if (currentWeather.has("wind_gust")) {
			this.windGustSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_gust")).floatValue();
		} else {
			this.windGustSpeed = 0;
		}
		
		//  Precipitations
		////    Rain
		if (currentWeather.has("rain") && currentWeather.getJSONObject("rain").has("1h")) {
			this.rain = BigDecimal.valueOf(currentWeather.getJSONObject("rain").getDouble("1h")).floatValue();
		} else {
			this.rain = 0;
		}
		////    Snow
		if (currentWeather.has("snow") && currentWeather.getJSONObject("snow").has("1h")) {
			this.snow = BigDecimal.valueOf(currentWeather.getJSONObject("snow").getDouble("1h")).floatValue();
		} else {
			this.snow = 0;
		}
	}
	
	//  Setters
	
	//  Getters
	public int getPlaceId() {
		return placeId;
	}
	
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	public long getDt() {
		return dt;
	}
	
	public void setDt(long dt) {
		this.dt = dt;
	}
	
	public String getWeather() {
		return weather;
	}
	
	public void setWeather(String weather) {
		this.weather = weather;
	}
	
	public String getWeatherDescription() {
		return weatherDescription;
	}
	
	public void setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
	}
	
	public int getWeatherCode() {
		return weatherCode;
	}
	
	public void setWeatherCode(int weatherCode) {
		this.weatherCode = weatherCode;
	}
	
	public float getTemperature() {
		return temperature;
	}
	
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	public float getTemperatureFeelsLike() {
		return temperatureFeelsLike;
	}
	
	public void setTemperatureFeelsLike(float temperatureFeelsLike) {
		this.temperatureFeelsLike = temperatureFeelsLike;
	}
	
	public int getPressure() {
		return pressure;
	}
	
	public void setPressure(int pressure) {
		this.pressure = pressure;
	}
	
	public int getHumidity() {
		return humidity;
	}
	
	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}
	
	public float getDewPoint() {
		return dewPoint;
	}
	
	public void setDewPoint(float dewPoint) {
		this.dewPoint = dewPoint;
	}
	
	public int getCloudiness() {
		return cloudiness;
	}
	
	public void setCloudiness(int cloudiness) {
		this.cloudiness = cloudiness;
	}
	
	public int getUvIndex() {
		return uvIndex;
	}
	
	public void setUvIndex(int uvIndex) {
		this.uvIndex = uvIndex;
	}
	
	public int getVisibility() {
		return visibility;
	}
	
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	public long getSunrise() {
		return sunrise;
	}
	
	public void setSunrise(long sunrise) {
		this.sunrise = sunrise;
	}
	
	public long getSunset() {
		return sunset;
	}
	
	public void setSunset(long sunset) {
		this.sunset = sunset;
	}
	
	public boolean isDaytime() {
		return getDt() > getSunrise() && getDt() < getSunset();
	}
	
	public float getWindSpeed() {
		return windSpeed;
	}
	
	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}
	
	public float getWindGustSpeed() {
		return windGustSpeed;
	}
	
	public void setWindGustSpeed(float windGustSpeed) {
		this.windGustSpeed = windGustSpeed;
	}
	
	public boolean isWindDirectionReadable() {
		return isWindDirectionReadable;
	}
	
	public void setWindDirectionReadable(boolean windDirectionReadable) {
		isWindDirectionReadable = windDirectionReadable;
	}
	
	public short getWindDirection() {
		return windDirection;
	}
	
	public void setWindDirection(short windDirection) {
		this.windDirection = windDirection;
	}
	
	public float getRain() {
		return rain;
	}
	
	public void setRain(float rain) {
		this.rain = rain;
	}
	
	public boolean thereIsRain() {
		return rain > 0;
	}
	
	public float getSnow() {
		return snow;
	}
	
	public void setSnow(float snow) {
		this.snow = snow;
	}
	
	public boolean thereIsSnow() {
		return snow > 0;
	}
	
	@NonNull
	public CurrentWeather clone() {
		CurrentWeather returnedCurrentWeather = new CurrentWeather();
		returnedCurrentWeather.placeId = placeId;
		returnedCurrentWeather.dt = this.dt;
		
		returnedCurrentWeather.weather = this.weather;
		returnedCurrentWeather.weatherDescription = this.weatherDescription;
		returnedCurrentWeather.weatherCode = this.weatherCode;
		
		returnedCurrentWeather.temperature = this.temperature;
		returnedCurrentWeather.temperatureFeelsLike = this.temperatureFeelsLike;
		
		returnedCurrentWeather.pressure = this.pressure;
		returnedCurrentWeather.humidity = this.humidity;
		returnedCurrentWeather.dewPoint = this.dewPoint;
		
		returnedCurrentWeather.cloudiness = this.cloudiness;
		returnedCurrentWeather.uvIndex = this.uvIndex;
		returnedCurrentWeather.visibility = this.visibility;
		
		returnedCurrentWeather.sunrise = this.sunrise;
		returnedCurrentWeather.sunset = this.sunset;
		
		returnedCurrentWeather.windSpeed = this.windSpeed;
		returnedCurrentWeather.windGustSpeed = this.windGustSpeed;
		returnedCurrentWeather.isWindDirectionReadable = this.isWindDirectionReadable;
		returnedCurrentWeather.windDirection = this.windDirection;
		
		returnedCurrentWeather.rain = this.rain;
		returnedCurrentWeather.snow = this.snow;
		
		return returnedCurrentWeather;
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", CurrentWeather.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("dt=" + dt)
				  .add("weather='" + weather + "'")
				  .add("weatherDescription='" + weatherDescription + "'")
				  .add("weatherCode=" + weatherCode)
				  .add("temperature=" + temperature)
				  .add("temperatureFeelsLike=" + temperatureFeelsLike)
				  .add("pressure=" + pressure)
				  .add("humidity=" + humidity)
				  .add("dewPoint=" + dewPoint)
				  .add("cloudiness=" + cloudiness)
				  .add("uvIndex=" + uvIndex)
				  .add("visibility=" + visibility)
				  .add("sunrise=" + sunrise)
				  .add("sunset=" + sunset)
				  .add("windSpeed=" + windSpeed)
				  .add("windGustSpeed=" + windGustSpeed)
				  .add("isWindDirectionReadable=" + isWindDirectionReadable)
				  .add("windDirection=" + windDirection)
				  .add("rain=" + rain)
				  .add("snow=" + snow)
				  .toString();
	}
}

