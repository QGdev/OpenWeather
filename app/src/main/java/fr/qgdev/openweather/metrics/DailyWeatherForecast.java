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
	private long sunriseDt;
	private long sunsetDt;
	private int uvIndex;
	
	private long moonriseDt;
	private long moonsetDt;
	private float moonPhase;
	
	private float windSpeed;
	private float windGustSpeed;
	private short windDirection;
	
	private float pop;
	private float rain;
	private float snow;
	
	/**
	 * Instantiates a new Daily weather forecast.
	 */
	public DailyWeatherForecast() {
		this.placeId = 0;
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
		this.sunriseDt = 0;
		this.sunsetDt = 0;
		this.uvIndex = 0;
		
		this.moonriseDt = 0;
		this.moonsetDt = 0;
		this.moonPhase = 0;
		
		this.windSpeed = 0;
		this.windGustSpeed = 0;
		this.windDirection = 0;
		
		this.pop = 0;
		this.rain = 0;
		this.snow = 0;
	}
	
	/**
	 * Instantiates a new Daily weather forecast.
	 *
	 * @param dailyWeather the daily weather json object from openweathermap
	 */
	public DailyWeatherForecast(JSONObject dailyWeather) throws JSONException {
		//  Time
		setDt(dailyWeather.getLong("dt") * 1000);
		
		//    Weather descriptions
		JSONObject dailyWeatherDescriptionsJSON = dailyWeather.getJSONArray("weather").getJSONObject(0);
		setWeather(dailyWeatherDescriptionsJSON.getString("main"));
		setWeatherDescription(dailyWeatherDescriptionsJSON.getString("description"));
		setWeatherCode(dailyWeatherDescriptionsJSON.getInt("id"));
		
		//  Temperatures
		JSONObject dailyWeatherTemperaturesJSON = dailyWeather.getJSONObject("temp");
		setTemperatureMorning(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("morn")).floatValue());
		setTemperatureDay(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("day")).floatValue());
		setTemperatureEvening(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("eve")).floatValue());
		setTemperatureNight(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("night")).floatValue());
		setTemperatureMinimum(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("min")).floatValue());
		setTemperatureMaximum(BigDecimal.valueOf(dailyWeatherTemperaturesJSON.getDouble("max")).floatValue());
		
		//  Feels Like Temperatures
		JSONObject dailyWeatherTemperaturesFeelsLikeJSON = dailyWeather.getJSONObject("feels_like");
		setTemperatureMorningFeelsLike(BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("morn")).floatValue());
		setTemperatureDayFeelsLike(BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("day")).floatValue());
		setTemperatureEveningFeelsLike(BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("eve")).floatValue());
		setTemperatureNightFeelsLike(BigDecimal.valueOf(dailyWeatherTemperaturesFeelsLikeJSON.getDouble("night")).floatValue());
		
		//  Pressure, Humidity, dewPoint
		setPressure(dailyWeather.getInt("pressure"));
		setHumidity(dailyWeather.getInt("humidity"));
		setDewPoint(BigDecimal.valueOf(dailyWeather.getDouble("dew_point")).floatValue());
		
		//  Sky
		setCloudiness(dailyWeather.getInt("clouds"));
		setSunriseDt(dailyWeather.getLong("sunrise") * 1000);
		setSunsetDt(dailyWeather.getLong("sunset") * 1000);
		setUvIndex(BigDecimal.valueOf(dailyWeather.getDouble("uvi")).intValue());
		
		//  Moon
		setMoonriseDt(dailyWeather.getLong("moonrise") * 1000);
		setMoonsetDt(dailyWeather.getLong("moonset") * 1000);
		setMoonPhase(BigDecimal.valueOf(dailyWeather.getDouble("moon_phase")).floatValue());
		
		//  Wind
		setWindSpeed(BigDecimal.valueOf(dailyWeather.getDouble("wind_speed")).floatValue());
		setWindDirection(BigDecimal.valueOf(dailyWeather.getInt("wind_deg")).shortValue());
		////    Wind Gusts
		if (dailyWeather.has("wind_gust")) {
			setWindGustSpeed(BigDecimal.valueOf(dailyWeather.getDouble("wind_gust")).floatValue());
		} else {
			setWindGustSpeed(0);
		}
		
		//  Precipitations
		////    PoP -   Probability of Precipitations
		setPop(BigDecimal.valueOf(dailyWeather.getDouble("pop")).floatValue());
		////    Rain
		if (dailyWeather.has("rain")) {
			setRain(BigDecimal.valueOf(dailyWeather.getDouble("rain")).floatValue());
		} else {
			setRain(0);
		}
		////    Snow
		if (dailyWeather.has("snow")) {
			setSnow(BigDecimal.valueOf(dailyWeather.getDouble("snow")).floatValue());
		} else {
			setSnow(0);
		}
	}
	
	/*
	  Getters and Setters
	 */
	
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
	 * Gets dt.
	 *
	 * @return the dt
	 */
	public long getDt() {
		return dt;
	}
	
	/**
	 * Sets dt.
	 *
	 * @param dt the dt (in milliseconds)
	 *           Must be positive or zero
	 */
	public void setDt(long dt) {
		if (dt <= 0)
			throw new IllegalArgumentException("dt must be positive or null");
		this.dt = dt;
	}
	
	/**
	 * Gets weather.
	 *
	 * @return the weather
	 */
	public String getWeather() {
		return weather;
	}
	
	/**
	 * Sets weather.
	 *
	 * @param weather the weather
	 */
	public void setWeather(String weather) {
		this.weather = weather;
	}
	
	/**
	 * Gets weather description.
	 *
	 * @return the weather description
	 */
	public String getWeatherDescription() {
		return weatherDescription;
	}
	
	/**
	 * Sets weather description.
	 *
	 * @param weatherDescription the weather description
	 */
	public void setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
	}
	
	/**
	 * Gets weather code.
	 *
	 * @return the weather code
	 */
	public int getWeatherCode() {
		return weatherCode;
	}
	
	/**
	 * Sets weather code.
	 *
	 * @param weatherCode the weather code
	 *                    Must be positive or null
	 */
	public void setWeatherCode(int weatherCode) {
		if (weatherCode < 0)
			throw new IllegalArgumentException("weatherCode must be positive or null");
		this.weatherCode = weatherCode;
	}
	
	/**
	 * Gets temperature for the morning.
	 *
	 * @return the temperature for the morning
	 */
	public float getTemperatureMorning() {
		return temperatureMorning;
	}
	
	/**
	 * Sets temperature for the morning.
	 *
	 * @param temperatureMorning the temperature for the morning
	 */
	public void setTemperatureMorning(float temperatureMorning) {
		this.temperatureMorning = temperatureMorning;
	}
	
	/**
	 * Gets temperature for the day.
	 *
	 * @return the temperature for the day
	 */
	public float getTemperatureDay() {
		return temperatureDay;
	}
	
	/**
	 * Sets temperature for the day.
	 *
	 * @param temperatureDay the temperature for the day
	 */
	public void setTemperatureDay(float temperatureDay) {
		this.temperatureDay = temperatureDay;
	}
	
	/**
	 * Gets temperature for the evening.
	 *
	 * @return the temperature for the evening
	 */
	public float getTemperatureEvening() {
		return temperatureEvening;
	}
	
	/**
	 * Sets temperature for the evening.
	 *
	 * @param temperatureEvening the temperature for the evening
	 */
	public void setTemperatureEvening(float temperatureEvening) {
		this.temperatureEvening = temperatureEvening;
	}
	
	/**
	 * Gets temperature for the night.
	 *
	 * @return the temperature for the night
	 */
	public float getTemperatureNight() {
		return temperatureNight;
	}
	
	/**
	 * Sets temperature for the night.
	 *
	 * @param temperatureNight the temperature for the night
	 */
	public void setTemperatureNight(float temperatureNight) {
		this.temperatureNight = temperatureNight;
	}
	
	/**
	 * Gets temperature minimum.
	 *
	 * @return the temperature minimum
	 */
	public float getTemperatureMinimum() {
		return temperatureMinimum;
	}
	
	/**
	 * Sets temperature minimum.
	 *
	 * @param temperatureMinimum the temperature minimum
	 */
	public void setTemperatureMinimum(float temperatureMinimum) {
		this.temperatureMinimum = temperatureMinimum;
	}
	
	/**
	 * Gets temperature maximum.
	 *
	 * @return the temperature maximum
	 */
	public float getTemperatureMaximum() {
		return temperatureMaximum;
	}
	
	/**
	 * Sets temperature maximum.
	 *
	 * @param temperatureMaximum the temperature maximum
	 */
	public void setTemperatureMaximum(float temperatureMaximum) {
		this.temperatureMaximum = temperatureMaximum;
	}
	
	/**
	 * Gets feels like temperature for the morning.
	 *
	 * @return the feels like temperature for the morning
	 */
	public float getTemperatureMorningFeelsLike() {
		return temperatureMorningFeelsLike;
	}
	
	/**
	 * Sets feels like temperature for the morning.
	 *
	 * @param temperatureMorningFeelsLike the feels like temperature for the morning
	 */
	public void setTemperatureMorningFeelsLike(float temperatureMorningFeelsLike) {
		this.temperatureMorningFeelsLike = temperatureMorningFeelsLike;
	}
	
	/**
	 * Gets feels like temperature for the day.
	 *
	 * @return the feels like temperature for the day
	 */
	public float getTemperatureDayFeelsLike() {
		return temperatureDayFeelsLike;
	}
	
	/**
	 * Sets feels like temperature for the day.
	 *
	 * @param temperatureDayFeelsLike the feels like temperature for the day
	 */
	public void setTemperatureDayFeelsLike(float temperatureDayFeelsLike) {
		this.temperatureDayFeelsLike = temperatureDayFeelsLike;
	}
	
	/**
	 * Gets feels like temperature for the evening.
	 *
	 * @return the feels like temperature for the evening
	 */
	public float getTemperatureEveningFeelsLike() {
		return temperatureEveningFeelsLike;
	}
	
	/**
	 * Sets feels like temperature for the evening.
	 *
	 * @param temperatureEveningFeelsLike the feels like temperature for the evening
	 */
	public void setTemperatureEveningFeelsLike(float temperatureEveningFeelsLike) {
		this.temperatureEveningFeelsLike = temperatureEveningFeelsLike;
	}
	
	/**
	 * Gets feels like temperature for the night.
	 *
	 * @return the feels like temperature for the night
	 */
	public float getTemperatureNightFeelsLike() {
		return temperatureNightFeelsLike;
	}
	
	/**
	 * Sets feels like temperature for the night.
	 *
	 * @param temperatureNightFeelsLike the feels like temperature for the night
	 */
	public void setTemperatureNightFeelsLike(float temperatureNightFeelsLike) {
		this.temperatureNightFeelsLike = temperatureNightFeelsLike;
	}
	
	/**
	 * Gets pressure value.
	 *
	 * @return the pressure value
	 */
	public int getPressure() {
		return pressure;
	}
	
	/**
	 * Sets pressure value.
	 *
	 * @param pressure the pressure value
	 *                 Must be positive or null
	 */
	public void setPressure(int pressure) {
		if (pressure < 0)
			throw new IllegalArgumentException("pressure must be positive or null");
		this.pressure = pressure;
	}
	
	/**
	 * Gets humidity value.
	 *
	 * @return the humidity value
	 */
	public int getHumidity() {
		return humidity;
	}
	
	/**
	 * Sets humidity value.
	 *
	 * @param humidity the humidity value
	 *                 Must be between 0 and 100 (both included)
	 */
	public void setHumidity(int humidity) {
		if (humidity < 0 || humidity > 100)
			throw new IllegalArgumentException("humidity must be between 0 and 100 (both included)");
		this.humidity = humidity;
	}
	
	/**
	 * Gets wind speed value.
	 *
	 * @return the wind speed value
	 */
	public float getDewPoint() {
		return dewPoint;
	}
	
	/**
	 * Sets dew point value.
	 *
	 * @param dewPoint the dew point value
	 */
	public void setDewPoint(float dewPoint) {
		this.dewPoint = dewPoint;
	}
	
	/**
	 * Gets cloud coverage value.
	 *
	 * @return the cloudiness value
	 */
	public int getCloudiness() {
		return cloudiness;
	}
	
	/**
	 * Sets cloud coverage value.
	 *
	 * @param cloudiness the cloud coverage value
	 *                   Must be between 0 and 100 (both included)
	 */
	public void setCloudiness(int cloudiness) {
		if (cloudiness < 0 || cloudiness > 100)
			throw new IllegalArgumentException("cloudiness must be between 0 and 100 (both included)");
		this.cloudiness = cloudiness;
	}
	
	/**
	 * Gets sunrise time.
	 *
	 * @return the sunrise time (in milliseconds)
	 */
	public long getSunriseDt() {
		return sunriseDt;
	}
	
	/**
	 * Sets sunrise time.
	 *
	 * @param sunriseDt the sunrise time (in milliseconds)
	 *                  Must be positive or null
	 */
	public void setSunriseDt(long sunriseDt) {
		if (sunriseDt < 0)
			throw new IllegalArgumentException("sunrise must be positive or null");
		this.sunriseDt = sunriseDt;
	}
	
	/**
	 * Gets sunset time.
	 *
	 * @return the sunset time (in milliseconds)
	 */
	public long getSunsetDt() {
		return sunsetDt;
	}
	
	/**
	 * Sets sunset time.
	 *
	 * @param sunsetDt the sunset time (in milliseconds)
	 *                 Must be positive or null
	 */
	public void setSunsetDt(long sunsetDt) {
		if (sunsetDt < 0)
			throw new IllegalArgumentException("sunset must be positive or null");
		this.sunsetDt = sunsetDt;
	}
	
	/**
	 * Gets UV index.
	 *
	 * @return the UV index
	 */
	public int getUvIndex() {
		return uvIndex;
	}
	
	/**
	 * Sets UV index.
	 *
	 * @param uvIndex the UV index
	 *                Must be positive or null
	 */
	public void setUvIndex(int uvIndex) {
		if (uvIndex < 0)
			throw new IllegalArgumentException("uvIndex must be positive or null");
		this.uvIndex = uvIndex;
	}
	
	/**
	 * Gets moonrise time.
	 *
	 * @return the moonrise time (in milliseconds)
	 */
	public long getMoonriseDt() {
		return moonriseDt;
	}
	
	/**
	 * Sets moonrise time.
	 *
	 * @param moonriseDt the moonrise time (in milliseconds)
	 *                   Must be positive or null
	 */
	public void setMoonriseDt(long moonriseDt) {
		if (moonriseDt < 0)
			throw new IllegalArgumentException("moonrise must be positive or null");
		this.moonriseDt = moonriseDt;
	}
	
	/**
	 * Gets moonset time.
	 *
	 * @return the moonset time (in milliseconds)
	 */
	public long getMoonsetDt() {
		return moonsetDt;
	}
	
	/**
	 * Sets moonset time.
	 *
	 * @param moonsetDt the moonset time (in milliseconds)
	 *                  Must be positive or null
	 */
	public void setMoonsetDt(long moonsetDt) {
		if (moonsetDt < 0)
			throw new IllegalArgumentException("moonset must be positive or null");
		this.moonsetDt = moonsetDt;
	}
	
	/**
	 * Gets moon phase.
	 *
	 * @return the moon phase
	 */
	public float getMoonPhase() {
		return moonPhase;
	}
	
	/**
	 * Sets moon phase.
	 *
	 * @param moonPhase the moon phase
	 *                  Must be between 0 and 1 (both included)
	 */
	public void setMoonPhase(float moonPhase) {
		if (moonPhase < 0 || moonPhase > 1)
			throw new IllegalArgumentException("moonPhase must be between 0 and 1 (both included)");
		this.moonPhase = moonPhase;
	}
	
	/**
	 * Gets wind speed.
	 *
	 * @return the wind speed
	 */
	public float getWindSpeed() {
		return windSpeed;
	}
	
	/**
	 * Sets wind speed.
	 *
	 * @param windSpeed the wind speed
	 *                  Must be positive or null
	 */
	public void setWindSpeed(float windSpeed) {
		if (windSpeed < 0)
			throw new IllegalArgumentException("windSpeed must be positive or null");
		this.windSpeed = windSpeed;
	}
	
	/**
	 * Gets wind gust speed.
	 *
	 * @return the wind gust speed
	 */
	public float getWindGustSpeed() {
		return windGustSpeed;
	}
	
	/**
	 * Sets wind gust speed.
	 *
	 * @param windGustSpeed the wind gust speed
	 *                      Must be positive or null
	 */
	public void setWindGustSpeed(float windGustSpeed) {
		if (windGustSpeed < 0)
			throw new IllegalArgumentException("windGustSpeed must be positive or null");
		this.windGustSpeed = windGustSpeed;
	}
	
	/**
	 * Gets wind direction.
	 *
	 * @return the wind direction
	 */
	public short getWindDirection() {
		return windDirection;
	}
	
	/**
	 * Sets wind direction.
	 *
	 * @param windDirection the wind direction
	 *                      Must be between 0 and 360 (both included)
	 */
	public void setWindDirection(short windDirection) {
		if (windDirection < 0 || windDirection > 360)
			throw new IllegalArgumentException("windDirection must be between 0 and 360 (both included)");
		this.windDirection = windDirection;
	}
	
	/**
	 * Gets probability of precipitation.
	 *
	 * @return the probability of precipitation value
	 */
	public float getPop() {
		return pop;
	}
	
	/**
	 * Sets probability of precipitation.
	 *
	 * @param pop the probability of precipitation value
	 *            Must be between 0 and 1 (both included)
	 */
	public void setPop(float pop) {
		if (pop < 0 || pop > 1)
			throw new IllegalArgumentException("pop must be between 0 and 1 (both included)");
		this.pop = pop;
	}
	
	/**
	 * Gets rain volume for the day.
	 *
	 * @return the rain volume
	 */
	public float getRain() {
		return rain;
	}
	
	
	/**
	 * Sets rain volume for the day.
	 *
	 * @param rain the rain volume
	 *             Must be positive or null
	 */
	public void setRain(float rain) {
		if (rain < 0)
			throw new IllegalArgumentException("rain must be positive or null");
		this.rain = rain;
	}
	
	/**
	 * Gets snow volume for the day.
	 *
	 * @return the snow volume
	 */
	public float getSnow() {
		return snow;
	}
	
	/**
	 * Sets snow volume for the day.
	 *
	 * @param snow the snow volume
	 *             Must be positive or null
	 */
	public void setSnow(float snow) {
		if (snow < 0)
			throw new IllegalArgumentException("snow must be positive or null");
		this.snow = snow;
	}
	
	/**
	 * Used to clone the daily weather forecast object.
	 *
	 * @return a clone of the daily weather forecast object.
	 */
	@NonNull
	public DailyWeatherForecast clone() {
		DailyWeatherForecast returnedDailyWeatherForecast = new DailyWeatherForecast();
		
		returnedDailyWeatherForecast.setPlaceId(placeId);
		
		////    Time
		returnedDailyWeatherForecast.setDt(this.dt);
		
		////    Weather
		returnedDailyWeatherForecast.setWeather(this.weather);
		returnedDailyWeatherForecast.setWeatherDescription(this.weatherDescription);
		returnedDailyWeatherForecast.setWeatherCode(this.weatherCode);
		
		////    Temperatures
		returnedDailyWeatherForecast.setTemperatureMorning(this.temperatureMorning);
		returnedDailyWeatherForecast.setTemperatureDay(this.temperatureDay);
		returnedDailyWeatherForecast.setTemperatureEvening(this.temperatureEvening);
		returnedDailyWeatherForecast.setTemperatureNight(this.temperatureNight);
		returnedDailyWeatherForecast.setTemperatureMinimum(this.temperatureMinimum);
		returnedDailyWeatherForecast.setTemperatureMaximum(this.temperatureMaximum);
		
		////    Feels like
		returnedDailyWeatherForecast.setTemperatureMorningFeelsLike(this.temperatureMorningFeelsLike);
		returnedDailyWeatherForecast.setTemperatureDayFeelsLike(this.temperatureDayFeelsLike);
		returnedDailyWeatherForecast.setTemperatureEveningFeelsLike(this.temperatureEveningFeelsLike);
		returnedDailyWeatherForecast.setTemperatureNightFeelsLike(this.temperatureNightFeelsLike);
		
		////    Environmental Variables
		returnedDailyWeatherForecast.setPressure(this.pressure);
		returnedDailyWeatherForecast.setHumidity(this.humidity);
		returnedDailyWeatherForecast.setDewPoint(this.dewPoint);
		
		////    Sky
		returnedDailyWeatherForecast.setCloudiness(this.cloudiness);
		returnedDailyWeatherForecast.setSunriseDt(this.sunriseDt);
		returnedDailyWeatherForecast.setSunsetDt(this.sunsetDt);
		returnedDailyWeatherForecast.setUvIndex(this.uvIndex);
		
		////    Moon
		returnedDailyWeatherForecast.setMoonriseDt(this.moonriseDt);
		returnedDailyWeatherForecast.setMoonsetDt(this.moonsetDt);
		returnedDailyWeatherForecast.setMoonPhase(this.moonPhase);
		
		////    Wind
		returnedDailyWeatherForecast.setWindSpeed(this.windSpeed);
		returnedDailyWeatherForecast.setWindGustSpeed(this.windGustSpeed);
		returnedDailyWeatherForecast.setWindDirection(this.windDirection);
		
		////    Precipitations
		returnedDailyWeatherForecast.setPop(this.pop);
		returnedDailyWeatherForecast.setRain(this.rain);
		returnedDailyWeatherForecast.setSnow(this.snow);
		
		return returnedDailyWeatherForecast;
	}
	
	/**
	 * Used to get the daily weather forecast as a string.
	 *
	 * @return the daily weather forecast as a string.
	 */
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
				  .add("sunrise=" + sunriseDt)
				  .add("sunset=" + sunsetDt)
				  .add("uvIndex=" + uvIndex)
				  .add("moonrise=" + moonriseDt)
				  .add("moonset=" + moonsetDt)
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