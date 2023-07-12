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

@Entity(tableName = "daily_weather_forecast",
		  primaryKeys = {"placeId", "dt"})

public class DailyWeatherForecast {
	private int placeId;
	private long dt;
	
	private String weather;
	private String weatherDescription;
	private int weatherCode;
	
	private float temperatureMorning;
	private float temperatureDay;
	private float temperatureEvening;
	private float temperatureNight;
	private float temperatureMinimum;
	private float temperatureMaximum;
	
	private float temperatureMorningFeelsLike;
	private float temperatureDayFeelsLike;
	private float temperatureEveningFeelsLike;
	private float temperatureNightFeelsLike;
	
	private int pressure;
	private int humidity;
	private float dewPoint;
	
	private int cloudiness;
	private long sunrise;
	private long sunset;
	private int uvIndex;
	
	private long moonrise;
	private long moonset;
	private float moonPhase;
	
	private float windSpeed;
	private float windGustSpeed;
	private short windDirection;
	
	private float pop;
	private float rain;
	private float snow;
	
	public DailyWeatherForecast() {
		this.dt = 0;
		
		this.weather = "";
		this.weatherDescription = "";
		this.weatherCode = 0;
		
		this.temperatureMorning = 0;
		this.temperatureDay = 0;
		this.temperatureEvening = 0;
		this.temperatureNight = 0;
		this.temperatureMinimum = 0;
		this.temperatureMaximum = 0;
		
		this.temperatureMorningFeelsLike = 0;
		this.temperatureDayFeelsLike = 0;
		this.temperatureEveningFeelsLike = 0;
		this.temperatureNightFeelsLike = 0;
		
		this.pressure = 0;
		this.humidity = 0;
		this.dewPoint = 0;
		
		this.cloudiness = 0;
		this.sunrise = 0;
		this.sunset = 0;
		this.uvIndex = 0;
		
		this.moonrise = 0;
		this.moonset = 0;
		this.moonPhase = 0;
		
		this.windSpeed = 0;
		this.windGustSpeed = 0;
		this.windDirection = 0;
		
		this.pop = 0;
		this.rain = 0;
		this.snow = 0;
	}
	
	public DailyWeatherForecast(JSONObject dailyWeather) throws JSONException {
		//  Time
		this.dt = dailyWeather.getLong("dt") * 1000;
		
		//    Weather descriptions
		JSONObject dailyWeatherDescriptionsJSON = dailyWeather.getJSONArray("weather").getJSONObject(0);
		this.weather = dailyWeatherDescriptionsJSON.getString("main");
		this.weatherDescription = dailyWeatherDescriptionsJSON.getString("description");
		this.weatherCode = dailyWeatherDescriptionsJSON.getInt("id");
		
		//  Temperatures
		JSONObject dailyWeatherTemperaturesJSON = dailyWeather.getJSONObject("temp");
		this.temperatureMorning = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("morn")).floatValue();
		this.temperatureDay = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("day")).floatValue();
		this.temperatureEvening = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("eve")).floatValue();
		this.temperatureNight = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("night")).floatValue();
		this.temperatureMinimum = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("min")).floatValue();
		this.temperatureMaximum = BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("max")).floatValue();
		
		//  Feels Like Temperatures
		JSONObject dailyWeatherTemperaturesFeelsLikeJSON = dailyWeather.getJSONObject("feels_like");
		this.temperatureMorningFeelsLike = BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("morn")).floatValue();
		this.temperatureDayFeelsLike = BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("day")).floatValue();
		this.temperatureEveningFeelsLike = BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("eve")).floatValue();
		this.temperatureNightFeelsLike = BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("night")).floatValue();
		
		//  Pressure, Humidity, dewPoint
		this.pressure = dailyWeather.getInt("pressure");
		this.humidity = dailyWeather.getInt("humidity");
		this.dewPoint = BigDecimal.valueOf(dailyWeather.getDouble("dew_point")).floatValue();
		
		//  Sky
		this.cloudiness = dailyWeather.getInt("clouds");
		this.sunrise = dailyWeather.getLong("sunrise") * 1000;
		this.sunset = dailyWeather.getLong("sunset") * 1000;
		this.uvIndex = BigDecimal.valueOf(dailyWeather.getDouble("uvi")).intValue();
		
		//  Moon
		this.moonrise = dailyWeather.getLong("moonrise") * 1000;
		this.moonset = dailyWeather.getLong("moonset") * 1000;
		this.moonPhase = BigDecimal.valueOf(dailyWeather.getDouble("moon_phase")).floatValue();
		
		//  Wind
		this.windSpeed = BigDecimal.valueOf(dailyWeather.getDouble("wind_speed")).floatValue();
		this.windDirection = BigDecimal.valueOf(dailyWeather.getInt("wind_deg")).shortValue();
		////    Wind Gusts
		if (dailyWeather.has("wind_gust")) {
			this.windGustSpeed = BigDecimal.valueOf(dailyWeather.getDouble("wind_gust")).floatValue();
		} else {
			this.windGustSpeed = 0;
		}
		
		//  Precipitations
		////    PoP -   Probability of Precipitations
		this.pop = BigDecimal.valueOf(dailyWeather.getDouble("pop")).floatValue();
		////    Rain
		if (dailyWeather.has("rain")) {
			this.rain = BigDecimal.valueOf(dailyWeather.getDouble("rain")).floatValue();
		} else {
			this.rain = 0;
		}
		////    Snow
		if (dailyWeather.has("snow")) {
			this.snow = BigDecimal.valueOf(dailyWeather.getDouble("snow")).floatValue();
		} else {
			this.snow = 0;
		}
	}
	
	@NonNull
	public DailyWeatherForecast clone() {
		DailyWeatherForecast returnedDailyWeatherForecast = new DailyWeatherForecast();
		
		returnedDailyWeatherForecast.placeId = placeId;
		
		////    Time
		returnedDailyWeatherForecast.dt = this.dt;
		
		////    Weather
		returnedDailyWeatherForecast.weather = this.weather;
		returnedDailyWeatherForecast.weatherDescription = this.weatherDescription;
		returnedDailyWeatherForecast.weatherCode = this.weatherCode;
		
		////    Temperatures
		returnedDailyWeatherForecast.temperatureMorning = this.temperatureMorning;
		returnedDailyWeatherForecast.temperatureDay = this.temperatureDay;
		returnedDailyWeatherForecast.temperatureEvening = this.temperatureEvening;
		returnedDailyWeatherForecast.temperatureNight = this.temperatureNight;
		returnedDailyWeatherForecast.temperatureMinimum = this.temperatureMinimum;
		returnedDailyWeatherForecast.temperatureMaximum = this.temperatureMaximum;
		
		////    Feels like
		returnedDailyWeatherForecast.temperatureMorningFeelsLike = this.temperatureMorningFeelsLike;
		returnedDailyWeatherForecast.temperatureDayFeelsLike = this.temperatureDayFeelsLike;
		returnedDailyWeatherForecast.temperatureEveningFeelsLike = this.temperatureEveningFeelsLike;
		returnedDailyWeatherForecast.temperatureNightFeelsLike = this.temperatureNightFeelsLike;
		
		////    Environmental Variables
		returnedDailyWeatherForecast.pressure = this.pressure;
		returnedDailyWeatherForecast.humidity = this.humidity;
		returnedDailyWeatherForecast.dewPoint = this.dewPoint;
		
		////    Sky
		returnedDailyWeatherForecast.cloudiness = this.cloudiness;
		returnedDailyWeatherForecast.sunrise = this.sunrise;
		returnedDailyWeatherForecast.sunset = this.sunset;
		returnedDailyWeatherForecast.uvIndex = this.uvIndex;
		
		////    Moon
		returnedDailyWeatherForecast.moonrise = this.moonrise;
		returnedDailyWeatherForecast.moonset = this.moonset;
		returnedDailyWeatherForecast.moonPhase = this.moonPhase;
		
		////    Wind
		returnedDailyWeatherForecast.windSpeed = this.windSpeed;
		returnedDailyWeatherForecast.windGustSpeed = this.windGustSpeed;
		returnedDailyWeatherForecast.windDirection = this.windDirection;
		
		////    Precipitations
		returnedDailyWeatherForecast.pop = this.pop;
		returnedDailyWeatherForecast.rain = this.rain;
		returnedDailyWeatherForecast.snow = this.snow;
		
		return returnedDailyWeatherForecast;
	}
	
	//	Getter
	
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
	
	public float getTemperatureMorning() {
		return temperatureMorning;
	}
	
	public void setTemperatureMorning(float temperatureMorning) {
		this.temperatureMorning = temperatureMorning;
	}
	
	public float getTemperatureDay() {
		return temperatureDay;
	}
	
	public void setTemperatureDay(float temperatureDay) {
		this.temperatureDay = temperatureDay;
	}
	
	public float getTemperatureEvening() {
		return temperatureEvening;
	}
	
	public void setTemperatureEvening(float temperatureEvening) {
		this.temperatureEvening = temperatureEvening;
	}
	
	public float getTemperatureNight() {
		return temperatureNight;
	}
	
	public void setTemperatureNight(float temperatureNight) {
		this.temperatureNight = temperatureNight;
	}
	
	public float getTemperatureMinimum() {
		return temperatureMinimum;
	}
	
	public void setTemperatureMinimum(float temperatureMinimum) {
		this.temperatureMinimum = temperatureMinimum;
	}
	
	public float getTemperatureMaximum() {
		return temperatureMaximum;
	}
	
	public void setTemperatureMaximum(float temperatureMaximum) {
		this.temperatureMaximum = temperatureMaximum;
	}
	
	public float getTemperatureMorningFeelsLike() {
		return temperatureMorningFeelsLike;
	}
	
	public void setTemperatureMorningFeelsLike(float temperatureMorningFeelsLike) {
		this.temperatureMorningFeelsLike = temperatureMorningFeelsLike;
	}
	
	public float getTemperatureDayFeelsLike() {
		return temperatureDayFeelsLike;
	}
	
	public void setTemperatureDayFeelsLike(float temperatureDayFeelsLike) {
		this.temperatureDayFeelsLike = temperatureDayFeelsLike;
	}
	
	public float getTemperatureEveningFeelsLike() {
		return temperatureEveningFeelsLike;
	}
	
	public void setTemperatureEveningFeelsLike(float temperatureEveningFeelsLike) {
		this.temperatureEveningFeelsLike = temperatureEveningFeelsLike;
	}
	
	public float getTemperatureNightFeelsLike() {
		return temperatureNightFeelsLike;
	}
	
	public void setTemperatureNightFeelsLike(float temperatureNightFeelsLike) {
		this.temperatureNightFeelsLike = temperatureNightFeelsLike;
	}
	
	public int getPressure() {
		return pressure;
	}
	
	
	//	Setter
	
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
	
	public int getUvIndex() {
		return uvIndex;
	}
	
	public void setUvIndex(int uvIndex) {
		this.uvIndex = uvIndex;
	}
	
	public long getMoonrise() {
		return moonrise;
	}
	
	public void setMoonrise(long moonrise) {
		this.moonrise = moonrise;
	}
	
	public long getMoonset() {
		return moonset;
	}
	
	public void setMoonset(long moonset) {
		this.moonset = moonset;
	}
	
	public float getMoonPhase() {
		return moonPhase;
	}
	
	public void setMoonPhase(float moonPhase) {
		this.moonPhase = moonPhase;
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
	@Override
	public String toString() {
		return new StringJoiner(", ", DailyWeatherForecast.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("dt=" + dt)
				  .add("weather='" + weather + "'")
				  .add("weatherDescription='" + weatherDescription + "'")
				  .add("weatherCode=" + weatherCode)
				  .add("temperatureMorning=" + temperatureMorning)
				  .add("temperatureDay=" + temperatureDay)
				  .add("temperatureEvening=" + temperatureEvening)
				  .add("temperatureNight=" + temperatureNight)
				  .add("temperatureMinimum=" + temperatureMinimum)
				  .add("temperatureMaximum=" + temperatureMaximum)
				  .add("temperatureMorningFeelsLike=" + temperatureMorningFeelsLike)
				  .add("temperatureDayFeelsLike=" + temperatureDayFeelsLike)
				  .add("temperatureEveningFeelsLike=" + temperatureEveningFeelsLike)
				  .add("temperatureNightFeelsLike=" + temperatureNightFeelsLike)
				  .add("pressure=" + pressure)
				  .add("humidity=" + humidity)
				  .add("dewPoint=" + dewPoint)
				  .add("cloudiness=" + cloudiness)
				  .add("sunrise=" + sunrise)
				  .add("sunset=" + sunset)
				  .add("uvIndex=" + uvIndex)
				  .add("moonrise=" + moonrise)
				  .add("moonset=" + moonset)
				  .add("moonPhase=" + moonPhase)
				  .add("windSpeed=" + windSpeed)
				  .add("windGustSpeed=" + windGustSpeed)
				  .add("windDirection=" + windDirection)
				  .add("pop=" + pop)
				  .add("rain=" + rain)
				  .add("snow=" + snow)
				  .toString();
	}
}

