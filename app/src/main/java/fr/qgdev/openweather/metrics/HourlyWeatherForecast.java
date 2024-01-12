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

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * HourlyWeatherForecast
 * <p>
 *    A data holder class for Hourly Weather Forecast data.
 *    It contains the weather description, the temperature, the pressure, the humidity,
 *    the wind speed, the wind direction, the probability of precipitation, the rain and the snow.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Entity(tableName = "hourly_weather_forecasts",
		  primaryKeys = {"placeId", "dt"})
public class HourlyWeatherForecast {
	@NonNull
	private String placeId;
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
	private int visibility;
	private int uvIndex;
	
	private float windSpeed;
	private float windGustSpeed;
	private short windDirection;
	
	private float pop;
	private float rain;
	private float snow;
	
	public HourlyWeatherForecast() {
		this.placeId = "";
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
		this.visibility = 0;
		this.uvIndex = 0;
		
		this.windSpeed = 0;
		this.windGustSpeed = 0;
		this.windDirection = 0;
		
		this.pop = 0;
		this.rain = 0;
		this.snow = 0;
	}
	
	public HourlyWeatherForecast(JSONObject hourlyWeather) throws JSONException {
		//  Time
		this.dt = hourlyWeather.getLong("dt") * 1000;
		
		//    Weather descriptions
		JSONObject hourlyForecastWeatherDescriptionsJSON = hourlyWeather.getJSONArray("weather").getJSONObject(0);
		this.weather = hourlyForecastWeatherDescriptionsJSON.getString("main");
		this.weatherDescription = hourlyForecastWeatherDescriptionsJSON.getString("description");
		this.weatherCode = hourlyForecastWeatherDescriptionsJSON.getInt("id");
		
		//  Temperatures
		this.temperature = BigDecimal.valueOf(hourlyWeather.getDouble("temp")).floatValue();
		this.temperatureFeelsLike = BigDecimal.valueOf(hourlyWeather.getDouble("feels_like")).floatValue();
		
		//  Pressure, Humidity, Visibility, cloudiness, dewPoint and uvIndex
		this.pressure = hourlyWeather.getInt("pressure");
		this.humidity = hourlyWeather.getInt("humidity");
		this.dewPoint = BigDecimal.valueOf(hourlyWeather.getDouble("dew_point")).floatValue();
		this.visibility = hourlyWeather.getInt("visibility");
		this.cloudiness = hourlyWeather.getInt("clouds");
		this.uvIndex = BigDecimal.valueOf(hourlyWeather.getDouble("uvi")).intValue();
		
		//  Wind
		this.windSpeed = BigDecimal.valueOf(hourlyWeather.getDouble("wind_speed")).floatValue();
		this.windDirection = BigDecimal.valueOf(hourlyWeather.getInt("wind_deg")).shortValue();
		////    Wind Gusts
		if (hourlyWeather.has("wind_gust")) {
			this.windGustSpeed = BigDecimal.valueOf(hourlyWeather.getDouble("wind_gust")).floatValue();
		} else {
			this.windGustSpeed = 0;
		}
		
		//  Precipitations
		////    PoP -   Probability of Precipitations
		this.pop = BigDecimal.valueOf(hourlyWeather.getDouble("pop")).floatValue();
		////    Rain
		if (hourlyWeather.has("rain") && hourlyWeather.getJSONObject("rain").has("1h")) {
			this.rain = BigDecimal.valueOf(hourlyWeather.getJSONObject("rain").getDouble("1h")).floatValue();
		} else {
			this.rain = 0;
		}
		////    Snow
		if (hourlyWeather.has("snow") && hourlyWeather.getJSONObject("snow").has("1h")) {
			this.snow = BigDecimal.valueOf(hourlyWeather.getJSONObject("snow").getDouble("1h")).floatValue();
		} else {
			this.snow = 0;
		}
	}
	
	//  Getter
	public String getPlaceId() {
		return placeId;
	}
	
	//  Setter
	public void setPlaceId(String placeId) {
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
	
	public int getVisibility() {
		return visibility;
	}
	
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	public int getUvIndex() {
		return uvIndex;
	}
	
	public void setUvIndex(int uvIndex) {
		this.uvIndex = uvIndex;
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
	
	public short getWindDirection() {
		return windDirection;
	}
	
	public void setWindDirection(short windDirection) {
		this.windDirection = windDirection;
	}
	
	public float getPop() {
		return pop;
	}
	
	public void setPop(float pop) {
		this.pop = pop;
	}
	
	public float getRain() {
		return rain;
	}
	
	public void setRain(float rain) {
		this.rain = rain;
	}
	
	public float getSnow() {
		return snow;
	}
	
	public void setSnow(float snow) {
		this.snow = snow;
	}
	
	
	@NonNull
	public HourlyWeatherForecast clone() {
		HourlyWeatherForecast returnedHourlyWeatherForecast = new HourlyWeatherForecast();
		
		returnedHourlyWeatherForecast.placeId = placeId;
		returnedHourlyWeatherForecast.dt = this.dt;
		
		returnedHourlyWeatherForecast.weather = this.weather;
		returnedHourlyWeatherForecast.weatherDescription = this.weatherDescription;
		returnedHourlyWeatherForecast.weatherCode = this.weatherCode;
		
		returnedHourlyWeatherForecast.temperature = this.temperature;
		returnedHourlyWeatherForecast.temperatureFeelsLike = this.temperatureFeelsLike;
		
		returnedHourlyWeatherForecast.pressure = this.pressure;
		returnedHourlyWeatherForecast.humidity = this.humidity;
		returnedHourlyWeatherForecast.dewPoint = this.dewPoint;
		
		returnedHourlyWeatherForecast.cloudiness = this.cloudiness;
		returnedHourlyWeatherForecast.visibility = this.visibility;
		returnedHourlyWeatherForecast.uvIndex = this.uvIndex;
		
		returnedHourlyWeatherForecast.windSpeed = this.windSpeed;
		returnedHourlyWeatherForecast.windGustSpeed = this.windGustSpeed;
		returnedHourlyWeatherForecast.windDirection = this.windDirection;
		
		returnedHourlyWeatherForecast.pop = this.pop;
		returnedHourlyWeatherForecast.rain = this.rain;
		returnedHourlyWeatherForecast.snow = this.snow;
		
		return returnedHourlyWeatherForecast;
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", HourlyWeatherForecast.class.getSimpleName() + "[", "]")
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
				  .add("visibility=" + visibility)
				  .add("uvIndex=" + uvIndex)
				  .add("windSpeed=" + windSpeed)
				  .add("windGustSpeed=" + windGustSpeed)
				  .add("windDirection=" + windDirection)
				  .add("pop=" + pop)
				  .add("rain=" + rain)
				  .add("snow=" + snow)
				  .toString();
	}
}