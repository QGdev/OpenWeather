package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class DailyWeatherForecast {

	public long dt;

	public String weather;
	public String weatherDescription;
	public int weatherCode;

	public float temperatureMorning;
	public float temperatureDay;
	public float temperatureEvening;
	public float temperatureNight;
	public float temperatureMinimum;
	public float temperatureMaximum;

	public float temperatureMorningFeelsLike;
	public float temperatureDayFeelsLike;
	public float temperatureEveningFeelsLike;
	public float temperatureNightFeelsLike;

	public int pressure;
	public int humidity;
	public float dewPoint;

	public int cloudiness;
	public long sunrise;
	public long sunset;
	public int uvIndex;

	public long moonrise;
	public long moonset;
	public float moonPhase;

	public float windSpeed;
	public float windGustSpeed;
	public short windDirection;

	public float pop;
	public float rain;
	public float snow;

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

	public DailyWeatherForecast(JSONObject dailyWeatherForecast) throws JSONException {
		//  Time
		this.dt = dailyWeatherForecast.getLong("dt");

		//  Weather
		this.weather = dailyWeatherForecast.getString("weather");
		this.weatherDescription = dailyWeatherForecast.getString("weather_description");
		this.weatherCode = dailyWeatherForecast.getInt("weather_code");

		//  Temperatures
		this.temperatureMorning = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_morning")).floatValue();
		this.temperatureDay = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_day")).floatValue();
		this.temperatureEvening = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_evening")).floatValue();
		this.temperatureNight = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_night")).floatValue();
		this.temperatureMinimum = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_minimum")).floatValue();
		this.temperatureMaximum = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_maximum")).floatValue();

		//  Feels Like Temperatures
		this.temperatureMorningFeelsLike = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_feelslike_morning")).floatValue();
		this.temperatureDayFeelsLike = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_feelslike_day")).floatValue();
		this.temperatureEveningFeelsLike = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_feelslike_evening")).floatValue();
		this.temperatureNightFeelsLike = BigDecimal.valueOf(dailyWeatherForecast.getDouble("temperature_feelslike_night")).floatValue();

		//  Pressure, Humidity, dewPoint
		this.pressure = dailyWeatherForecast.getInt("pressure");
		this.humidity = dailyWeatherForecast.getInt("humidity");
		this.dewPoint = BigDecimal.valueOf(dailyWeatherForecast.getDouble("dew_point")).floatValue();

		//  Sky
		this.cloudiness = dailyWeatherForecast.getInt("cloudiness");
		this.sunrise = dailyWeatherForecast.getLong("sunrise");
		this.sunset = dailyWeatherForecast.getLong("sunset");
		//  To assure retrocompatibility with older versions
		if (dailyWeatherForecast.has("uvi")) this.uvIndex = dailyWeatherForecast.getInt("uvi");
		else this.uvIndex = 0;

		//  Moon
		//  To assure retrocompatibility with older versions
		if (dailyWeatherForecast.has("moonrise") && dailyWeatherForecast.has("moonset") && dailyWeatherForecast.has("moon_phase")) {
			this.moonrise = dailyWeatherForecast.getInt("moonrise");
			this.moonset = dailyWeatherForecast.getInt("moonset");
			this.moonPhase = BigDecimal.valueOf(dailyWeatherForecast.getDouble("moon_phase")).floatValue();
		} else {
			this.moonrise = 0;
			this.moonset = 0;
			this.moonPhase = 0;
		}

		//  Wind
		this.windSpeed = BigDecimal.valueOf(dailyWeatherForecast.getDouble("wind_speed")).floatValue();
		this.windDirection = BigDecimal.valueOf(dailyWeatherForecast.getInt("wind_direction")).shortValue();
		this.windGustSpeed = BigDecimal.valueOf(dailyWeatherForecast.getDouble("wind_gust_speed")).floatValue();

		//  Precipitations
		////    PoP -   Probability of Precipitations
		this.pop = BigDecimal.valueOf(dailyWeatherForecast.getDouble("pop")).floatValue();
		////    Rain
		this.rain = BigDecimal.valueOf(dailyWeatherForecast.getDouble("rain")).floatValue();
		////    Snow
		this.snow = BigDecimal.valueOf(dailyWeatherForecast.getDouble("snow")).floatValue();
	}

	public void fillWithOWMData(JSONObject dailyWeather) throws JSONException {
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

	public JSONObject getJSONObject() throws JSONException {
		JSONObject dailyWeatherForecastJSON = new JSONObject();

		//  Daily Weather Forecast
		////    Time
		dailyWeatherForecastJSON.accumulate("dt", this.dt);

		////    Weather
		dailyWeatherForecastJSON.accumulate("weather", this.weather);
		dailyWeatherForecastJSON.accumulate("weather_description", this.weatherDescription);
		dailyWeatherForecastJSON.accumulate("weather_code", this.weatherCode);

		////    Temperatures
		dailyWeatherForecastJSON.accumulate("temperature_morning", this.temperatureMorning);
		dailyWeatherForecastJSON.accumulate("temperature_day", this.temperatureDay);
		dailyWeatherForecastJSON.accumulate("temperature_evening", this.temperatureEvening);
		dailyWeatherForecastJSON.accumulate("temperature_night", this.temperatureNight);
		dailyWeatherForecastJSON.accumulate("temperature_minimum", this.temperatureMinimum);
		dailyWeatherForecastJSON.accumulate("temperature_maximum", this.temperatureMaximum);

		////    Feels Like
		dailyWeatherForecastJSON.accumulate("temperature_feelslike_morning", this.temperatureMorningFeelsLike);
		dailyWeatherForecastJSON.accumulate("temperature_feelslike_day", this.temperatureDayFeelsLike);
		dailyWeatherForecastJSON.accumulate("temperature_feelslike_evening", this.temperatureEveningFeelsLike);
		dailyWeatherForecastJSON.accumulate("temperature_feelslike_night", this.temperatureNightFeelsLike);

		////    Environmental Variables
		dailyWeatherForecastJSON.accumulate("pressure", this.pressure);
		dailyWeatherForecastJSON.accumulate("humidity", this.humidity);
		dailyWeatherForecastJSON.accumulate("dew_point", this.dewPoint);

		////    Sky
		dailyWeatherForecastJSON.accumulate("cloudiness", this.cloudiness);
		dailyWeatherForecastJSON.accumulate("sunrise", this.sunrise);
		dailyWeatherForecastJSON.accumulate("sunset", this.sunset);
		dailyWeatherForecastJSON.accumulate("uvi", this.uvIndex);

		////    Moon
		dailyWeatherForecastJSON.accumulate("moonrise", this.moonrise);
		dailyWeatherForecastJSON.accumulate("moonset", this.moonset);
		dailyWeatherForecastJSON.accumulate("moon_phase", this.moonPhase);

		////    Wind
		dailyWeatherForecastJSON.accumulate("wind_speed", this.windSpeed);
		dailyWeatherForecastJSON.accumulate("wind_gust_speed", this.windGustSpeed);
		dailyWeatherForecastJSON.accumulate("wind_direction", this.windDirection);

		////    Precipitations
		dailyWeatherForecastJSON.accumulate("pop", this.pop);
		dailyWeatherForecastJSON.accumulate("rain", this.rain);
		dailyWeatherForecastJSON.accumulate("snow", this.snow);

		return dailyWeatherForecastJSON;
	}

	@NonNull
	public DailyWeatherForecast clone() {
		DailyWeatherForecast returnedDailyWeatherForecast = new DailyWeatherForecast();
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

	public String getWindDirectionCardinalPoints() {

		//  N
		if (windDirection > 348.75 || windDirection < 11.25) {
			return "N";
		}

		//  NNE
		if (windDirection >= 11.25 && windDirection < 33.75) {
			return "NNE";
		}
		//  NE
		if (windDirection >= 33.75 && windDirection <= 56.25) {
			return "NE";
		}
		//  ENE
		if (windDirection > 56.25 && windDirection <= 78.75) {
			return "ENE";
		}
		//  E
		if (windDirection > 78.75 && windDirection < 101.25) {
			return "E";
		}
		//  ESE
		if (windDirection >= 101.25 && windDirection < 123.75) {
			return "ESE";
		}
		//  SE
		if (windDirection >= 123.75 && windDirection <= 146.25) {
			return "SE";
		}
		// SSE
		if (windDirection > 146.25 && windDirection <= 168.75) {
			return "SSE";
		}
		//  S
		if (windDirection > 168.75 && windDirection < 191.25) {
			return "S";
		}
		//  SSW
		if (windDirection >= 191.25 && windDirection < 213.75) {
			return "SSW";
		}
		//  SW
		if (windDirection >= 213.75 && windDirection <= 236.25) {
			return "SW";
		}
		//  WSW
		if (windDirection > 236.25 && windDirection <= 258.75) {
			return "WSW";
		}
		//  W
		if (windDirection > 258.75 && windDirection < 281.25) {
			return "W";
		}
		//  WNW
		if (windDirection >= 281.25 && windDirection < 303.75) {
			return "WNW";
		}
		//  NW
		if (windDirection >= 303.75 && windDirection <= 326.25) {
			return "NW";
		}
		//  NNW
		if (windDirection > 326.25 && windDirection <= 348.75) {
			return "NNW";
		}
		return "";
	}
}

