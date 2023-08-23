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
	
	private long sunriseDt;
	private long sunsetDt;
	
	private float windSpeed;
	private float windGustSpeed;
	private boolean isWindDirectionReadable;
	private short windDirection;
	
	private float rain;
	private float snow;
	
	/**
	 * Instantiates a new Current weather.
	 */
	public CurrentWeather() {
		this.placeId = 0;
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
		
		this.sunriseDt = 0;
		this.sunsetDt = 0;
		
		this.windSpeed = 0;
		this.windGustSpeed = 0;
		this.isWindDirectionReadable = false;
		this.windDirection = 0;
		
		this.rain = 0;
		this.snow = 0;
	}
	
	/**
	 * Instantiates a new Current weather.
	 *
	 * @param currentWeather the current weather json object from openweathermap
	 */
	public CurrentWeather(JSONObject currentWeather) throws JSONException {
		//	Set the time
		// Check for overflows
		long dt = currentWeather.getLong("dt");
		if (dt > dt * 1000)
			throw new IllegalArgumentException("dt is too big, overflow on long");
		setDt(currentWeather.getLong("dt") * 1000);
		
		// Weather descriptions
		JSONObject currentWeatherDescriptionsJSON = currentWeather.getJSONArray("weather").getJSONObject(0);    //  Get only the first station
		setWeather(currentWeatherDescriptionsJSON.getString("main"));
		setWeatherDescription(currentWeatherDescriptionsJSON.getString("description"));
		setWeatherCode(currentWeatherDescriptionsJSON.getInt("id"));
		
		// Temperatures
		setTemperature(BigDecimal.valueOf(currentWeather.getDouble("temp")).floatValue());
		setTemperatureFeelsLike(BigDecimal.valueOf(currentWeather.getDouble("feels_like")).floatValue());
		
		// Pressure, Humidity, dewPoint, uvIndex
		setPressure(currentWeather.getInt("pressure"));
		setHumidity(currentWeather.getInt("humidity"));
		setDewPoint(BigDecimal.valueOf(currentWeather.getDouble("dew_point")).floatValue());
		
		if (currentWeather.has("uvi")) {
			setUvIndex(currentWeather.getInt("uvi"));
		} else {
			setUvIndex(0);
		}
		
		// Sky informations
		setCloudiness(currentWeather.getInt("clouds"));
		setVisibility(currentWeather.getInt("visibility"));
		
		// Sunrise and Sunset
		//	Check for overflows
		long sunrise = currentWeather.getLong("sunrise");
		long sunset = currentWeather.getLong("sunset");
		
		if (sunrise > sunrise * 1000)
			throw new IllegalArgumentException("sunrise is too big, overflow on long");
		if (sunset > sunset * 1000)
			throw new IllegalArgumentException("sunset is too big, overflow on long");
		
		setSunriseDt(currentWeather.getLong("sunrise") * 1000);
		setSunsetDt(currentWeather.getLong("sunset") * 1000);
		
		// Wind informations
		setWindSpeed(BigDecimal.valueOf(currentWeather.getDouble("wind_speed")).floatValue());
		
		////	Enough wind for a viable wind direction information
		setWindDirectionReadable(currentWeather.has("wind_deg"));
		if (isWindDirectionReadable()) {
			setWindDirection(BigDecimal.valueOf(currentWeather.getInt("wind_deg")).shortValue());
		}
		////  Wind Gusts
		if (currentWeather.has("wind_gust")) {
			setWindGustSpeed(BigDecimal.valueOf(currentWeather.getDouble("wind_gust")).floatValue());
		} else {
			setWindGustSpeed(0);
		}
		
		//	Precipitations
		////	Rain
		if (currentWeather.has("rain") && currentWeather.getJSONObject("rain").has("1h")) {
			setRain(BigDecimal.valueOf(currentWeather.getJSONObject("rain").getDouble("1h")).floatValue());
		} else {
			setRain(0);
		}
		////	Snow
		if (currentWeather.has("snow") && currentWeather.getJSONObject("snow").has("1h")) {
			setSnow(BigDecimal.valueOf(currentWeather.getJSONObject("snow").getDouble("1h")).floatValue());
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
	 * @return the dt  (in milliseconds)
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
		if (dt < 0)
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
	 *                Must not be null
	 */
	public void setWeather(String weather) {
		if (weather == null)
			throw new IllegalArgumentException("weather must not be null");
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
	 *                           Must not be null
	 */
	public void setWeatherDescription(String weatherDescription) {
		if (weatherDescription == null)
			throw new IllegalArgumentException("weatherDescription must not be null");
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
	 * Gets temperature.
	 *
	 * @return the temperature
	 */
	public float getTemperature() {
		return temperature;
	}
	
	/**
	 * Sets temperature.
	 *
	 * @param temperature the temperature
	 */
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	/**
	 * Gets feels like temperature
	 *
	 * @return the feels like temperature
	 */
	public float getTemperatureFeelsLike() {
		return temperatureFeelsLike;
	}
	
	/**
	 * Sets feels like temperature
	 *
	 * @param temperatureFeelsLike the feels like temperature
	 */
	public void setTemperatureFeelsLike(float temperatureFeelsLike) {
		this.temperatureFeelsLike = temperatureFeelsLike;
	}
	
	/**
	 * Gets pressure.
	 *
	 * @return the pressure
	 */
	public int getPressure() {
		return pressure;
	}
	
	/**
	 * Sets pressure.
	 *
	 * @param pressure the pressure
	 *                 Must be positive or null
	 */
	public void setPressure(int pressure) {
		if (pressure < 0)
			throw new IllegalArgumentException("pressure must be positive or null");
		this.pressure = pressure;
	}
	
	/**
	 * Gets humidity.
	 *
	 * @return the humidity
	 */
	public int getHumidity() {
		return humidity;
	}
	
	/**
	 * Sets humidity.
	 *
	 * @param humidity the humidity
	 *                 Must be between 0 and 100 (both included)
	 */
	public void setHumidity(int humidity) {
		if (humidity < 0 || humidity > 100)
			throw new IllegalArgumentException("humidity must be between 0 and 100 (both included)");
		this.humidity = humidity;
	}
	
	/**
	 * Gets dew point.
	 *
	 * @return the dew point
	 */
	public float getDewPoint() {
		return dewPoint;
	}
	
	/**
	 * Sets dew point.
	 *
	 * @param dewPoint the dew point
	 */
	public void setDewPoint(float dewPoint) {
		this.dewPoint = dewPoint;
	}
	
	/**
	 * Gets cloud coverage.
	 *
	 * @return the cloudiness
	 */
	public int getCloudiness() {
		return cloudiness;
	}
	
	/**
	 * Sets cloud coverage.
	 *
	 * @param cloudiness the cloud coverage
	 *                   Must be between 0 and 100 (both included)
	 */
	public void setCloudiness(int cloudiness) {
		if (cloudiness < 0 || cloudiness > 100)
			throw new IllegalArgumentException("cloudiness must be between 0 and 100 (both included)");
		this.cloudiness = cloudiness;
	}
	
	/**
	 * Gets UV index.
	 *
	 * @return the uv index
	 */
	public int getUvIndex() {
		return uvIndex;
	}
	
	/**
	 * Sets UV index.
	 *
	 * @param uvIndex the uv index
	 *                Must be positive or null
	 */
	public void setUvIndex(int uvIndex) {
		if (uvIndex < 0)
			throw new IllegalArgumentException("uvIndex must be positive or null");
		this.uvIndex = uvIndex;
	}
	
	/**
	 * Gets visibility distance.
	 *
	 * @return the visibility distance
	 */
	public int getVisibility() {
		return visibility;
	}
	
	/**
	 * Sets visibility distance.
	 *
	 * @param visibility the visibility distance
	 *                   Must be positive or null
	 */
	public void setVisibility(int visibility) {
		if (visibility < 0)
			throw new IllegalArgumentException("visibility must be positive or null");
		this.visibility = visibility;
	}
	
	/**
	 * Gets sunrise time.
	 *
	 * @return the sunrise time  (in milliseconds)
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
	 * @return the sunset time  (in milliseconds)
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
	 * Used to check if the current weather is during the day or during the night.
	 *
	 * @return true if the current weather is during the day, false otherwise
	 */
	public boolean isDaytime() {
		return getDt() > getSunriseDt() && getDt() < getSunsetDt();
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
	 *                  Must be positive
	 */
	public void setWindSpeed(float windSpeed) {
		if (windSpeed < 0)
			throw new IllegalArgumentException("windSpeed must be positive");
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
	 * Used to check if the wind gust speed is actually readable.
	 *
	 * @return true if the wind gust speed is readable, false otherwise
	 */
	public boolean isWindDirectionReadable() {
		return isWindDirectionReadable;
	}
	
	/**
	 * Sets if the wind gust speed is readable.
	 *
	 * @param windDirectionReadable true if the wind gust speed is readable, false otherwise
	 */
	public void setWindDirectionReadable(boolean windDirectionReadable) {
		isWindDirectionReadable = windDirectionReadable;
	}
	
	/**
	 * Gets wind direction in degrees.
	 *
	 * @return the wind direction
	 */
	public short getWindDirection() {
		return windDirection;
	}
	
	/**
	 * Sets wind direction in degrees.
	 *
	 * @param windDirection the wind direction
	 *                      Must be between 0 and 360
	 */
	public void setWindDirection(short windDirection) {
		if (windDirection < 0 || windDirection > 360)
			throw new IllegalArgumentException("windDirection must be between 0 and 360");
		this.windDirection = windDirection;
	}
	
	/**
	 * Gets rain volume for the last hour.
	 *
	 * @return the rain volume for the last hour
	 */
	public float getRain() {
		return rain;
	}
	
	/**
	 * Sets rain volume for the last hour.
	 *
	 * @param rain the rain volume for the last hour
	 *             Must be positive
	 */
	public void setRain(float rain) {
		if (rain < 0)
			throw new IllegalArgumentException("rain must be positive");
		this.rain = rain;
	}
	
	/**
	 * Used to check if there is rain.
	 *
	 * @return true if there is rain, false otherwise
	 */
	public boolean thereIsRain() {
		return rain > 0;
	}
	
	/**
	 * Gets snow volume for the last hour.
	 *
	 * @return the snow volume for the last hour
	 */
	public float getSnow() {
		return snow;
	}
	
	/**
	 * Sets snow volume for the last hour.
	 *
	 * @param snow the snow volume for the last hour
	 *             Must be positive
	 */
	public void setSnow(float snow) {
		if (snow < 0)
			throw new IllegalArgumentException("snow must be positive");
		this.snow = snow;
	}
	
	/**
	 * Used to check if there is snow.
	 *
	 * @return true if there is snow, false otherwise
	 */
	public boolean thereIsSnow() {
		return snow > 0;
	}
	
	/**
	 * Used to clone the current weather.
	 *
	 * @return a clone of the current weather
	 */
	@NonNull
	public CurrentWeather clone() {
		CurrentWeather returnedCurrentWeather = new CurrentWeather();
		
		returnedCurrentWeather.setPlaceId(getPlaceId());
		returnedCurrentWeather.setDt(getDt());
		returnedCurrentWeather.setWeather(getWeather());
		returnedCurrentWeather.setWeatherDescription(getWeatherDescription());
		returnedCurrentWeather.setWeatherCode(getWeatherCode());
		returnedCurrentWeather.setTemperature(getTemperature());
		returnedCurrentWeather.setTemperatureFeelsLike(getTemperatureFeelsLike());
		returnedCurrentWeather.setPressure(getPressure());
		returnedCurrentWeather.setHumidity(getHumidity());
		returnedCurrentWeather.setDewPoint(getDewPoint());
		returnedCurrentWeather.setCloudiness(getCloudiness());
		returnedCurrentWeather.setUvIndex(getUvIndex());
		returnedCurrentWeather.setVisibility(getVisibility());
		returnedCurrentWeather.setSunriseDt(getSunriseDt());
		returnedCurrentWeather.setSunsetDt(getSunsetDt());
		returnedCurrentWeather.setWindSpeed(getWindSpeed());
		returnedCurrentWeather.setWindGustSpeed(getWindGustSpeed());
		returnedCurrentWeather.setWindDirectionReadable(isWindDirectionReadable());
		returnedCurrentWeather.setWindDirection(getWindDirection());
		returnedCurrentWeather.setRain(getRain());
		returnedCurrentWeather.setSnow(getSnow());
		
		return returnedCurrentWeather;
	}
	
	/**
	 * Used to get the current weather as a string.
	 *
	 * @return the current weather as a string
	 */
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
				  .add("sunrise=" + sunriseDt)
				  .add("sunset=" + sunsetDt)
				  .add("windSpeed=" + windSpeed)
				  .add("windGustSpeed=" + windGustSpeed)
				  .add("isWindDirectionReadable=" + isWindDirectionReadable)
				  .add("windDirection=" + windDirection)
				  .add("rain=" + rain)
				  .add("snow=" + snow)
				  .toString();
	}
}