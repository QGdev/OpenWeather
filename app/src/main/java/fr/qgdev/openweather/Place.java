package fr.qgdev.openweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;
import fr.qgdev.openweather.weather.MinutelyWeatherForecast;

public class Place {

    private String city;
    private String country;
    private String countryCode;

    private double latitude;
    private double longitude;

    private Date lastUpdateDate;
    private long lastUpdate;

    private String timeZone;

    private boolean errorDuringDataAcquisition;
    private int errorCode;

    private CurrentWeather currentWeather;
    private ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList;
    private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
    private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;


    public Place() {

    }

    public Place(String city, String country, String countryCode) {

        this.currentWeather = new CurrentWeather();
        this.minutelyWeatherForecastArrayList = new ArrayList<MinutelyWeatherForecast>();
        this.hourlyWeatherForecastArrayList = new ArrayList<HourlyWeatherForecast>();
        this.dailyWeatherForecastArrayList = new ArrayList<DailyWeatherForecast>();

        this.city = city;
        this.country = country;
        this.countryCode = countryCode;
    }

    public Place(JSONObject placeObjectJSON) throws JSONException {

        this.currentWeather = new CurrentWeather();
        this.minutelyWeatherForecastArrayList = new ArrayList<MinutelyWeatherForecast>();
        this.hourlyWeatherForecastArrayList = new ArrayList<HourlyWeatherForecast>();
        this.dailyWeatherForecastArrayList = new ArrayList<DailyWeatherForecast>();

        //  Place data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("place")) {
            JSONObject placeJSON = placeObjectJSON.optJSONObject("place");

            this.city = placeJSON.getString("city");
            this.country = placeJSON.getString("country");
            this.countryCode = placeJSON.getString("country_code");
            this.latitude = placeJSON.getDouble("latitude");
            this.longitude = placeJSON.getDouble("longitude");
            this.timeZone = placeJSON.getString("timezone");

        } else {
            throw new JSONException("Cannot find place data in PlaceObjectJSON");
        }


        //  Update data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("update")) {
            JSONObject updateJSON = placeObjectJSON.optJSONObject("update");

            this.lastUpdate = updateJSON.getLong("last_update");
            this.lastUpdateDate = new Date(lastUpdate);

        } else {
            throw new JSONException("Cannot find update data in PlaceObjectJSON");
        }


        //  Error data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("errors")) {
            JSONObject errorJSON = placeObjectJSON.optJSONObject("errors");

            this.errorDuringDataAcquisition = errorJSON.getBoolean("error_during_data_acquisition");
            this.errorCode = errorJSON.getInt("error_code");

        } else {
            throw new JSONException("Cannot find errors data in PlaceObjectJSON");
        }


        //  Current Weather data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("current_weather")) {
            JSONObject currentWeatherJSON = placeObjectJSON.optJSONObject("current_weather");

            //  The time of this update
            this.currentWeather.dt = currentWeatherJSON.getLong("dt");

            //  Weather
            this.currentWeather.weather = currentWeatherJSON.getString("weather");
            this.currentWeather.weatherDescription = currentWeatherJSON.getString("weather_description");
            this.currentWeather.weatherCode = currentWeatherJSON.getInt("weather_code");

            //  Temperatures
            this.currentWeather.temperature = currentWeatherJSON.getDouble("temperature");
            this.currentWeather.temperatureFeelsLike = currentWeatherJSON.getDouble("temperature_feels_like");

            //  Pressure, Humidity, dewPoint
            this.currentWeather.pressure = currentWeatherJSON.getInt("pressure");
            this.currentWeather.humidity = currentWeatherJSON.getInt("humidity");
            this.currentWeather.dewPoint = currentWeatherJSON.getDouble("dew_point");

            //  Sky informations
            this.currentWeather.cloudiness = currentWeatherJSON.getInt("cloudiness");
            this.currentWeather.uvIndex = currentWeatherJSON.getInt("uvi");
            this.currentWeather.visibility = currentWeatherJSON.getInt("visibility");
            this.currentWeather.sunrise = currentWeatherJSON.getLong("sunrise");
            this.currentWeather.sunset = currentWeatherJSON.getLong("sunset");

            //    Wind informations
            this.currentWeather.windSpeed = currentWeatherJSON.getDouble("wind_speed");
            this.currentWeather.windGustSpeed = currentWeatherJSON.getDouble("wind_gust_speed");
            this.currentWeather.isWindDirectionReadable = currentWeatherJSON.getBoolean("wind_readable_direction");
            this.currentWeather.windDirection = currentWeatherJSON.getInt("wind_direction");

            //  Precipitations
            this.currentWeather.rain = currentWeatherJSON.getInt("rain");
            this.currentWeather.snow = currentWeatherJSON.getInt("snow");

        } else {
            throw new JSONException("Cannot find current weather data in PlaceObjectJSON");
        }


        //  Minutely Weather Forecast data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("minutely_weather_forecast")) {
            JSONArray minutelyForecastWeatherJSON = placeObjectJSON.optJSONArray("minutely_weather_forecast");
            JSONObject minutelyForecastWeatherJSON_tmp;

            for (int i = 0; i < minutelyForecastWeatherJSON.length(); i++) {

                minutelyForecastWeatherJSON_tmp = minutelyForecastWeatherJSON.getJSONObject(i);

                this.minutelyWeatherForecastArrayList.add(i, new MinutelyWeatherForecast(minutelyForecastWeatherJSON_tmp.getLong("dt"), minutelyForecastWeatherJSON_tmp.getDouble("precipitation")));
            }
        } else {
            throw new JSONException("Cannot find minutely weather forecast in PlaceObjectJSON");
        }


        //  Hourly Weather Forecast
        //________________________________________________________________
        //

        if (placeObjectJSON.has("hourly_weather_forecast")) {
            JSONArray hourlyForecastWeatherJSON = placeObjectJSON.getJSONArray("hourly_weather_forecast");
            JSONObject hourlyForecastWeatherJSONtmp;

            HourlyWeatherForecast hourlyWeatherForecasttmp;

            for (int i = 0; i < hourlyForecastWeatherJSON.length(); i++) {

                hourlyForecastWeatherJSONtmp = hourlyForecastWeatherJSON.getJSONObject(i);
                hourlyWeatherForecasttmp = new HourlyWeatherForecast();

                //  Time
                hourlyWeatherForecasttmp.dt = hourlyForecastWeatherJSONtmp.getLong("dt");

                //  Weather
                hourlyWeatherForecasttmp.weather = hourlyForecastWeatherJSONtmp.getString("weather");
                hourlyWeatherForecasttmp.weatherDescription = hourlyForecastWeatherJSONtmp.getString("weather_description");
                hourlyWeatherForecasttmp.weatherCode = hourlyForecastWeatherJSONtmp.getInt("weather_code");

                //  Temperatures
                hourlyWeatherForecasttmp.temperature = hourlyForecastWeatherJSONtmp.getDouble("temperature");
                hourlyWeatherForecasttmp.temperatureFeelsLike = hourlyForecastWeatherJSONtmp.getDouble("temperature_feels_like");

                //  Pressure, Humidity, dew point, Cloudiness, Visibility
                hourlyWeatherForecasttmp.pressure = hourlyForecastWeatherJSONtmp.getInt("pressure");
                hourlyWeatherForecasttmp.humidity = hourlyForecastWeatherJSONtmp.getInt("humidity");
                hourlyWeatherForecasttmp.dewPoint = hourlyForecastWeatherJSONtmp.getDouble("dew_point");
                hourlyWeatherForecasttmp.cloudiness = hourlyForecastWeatherJSONtmp.getInt("cloudiness");
                hourlyWeatherForecasttmp.visibility = hourlyForecastWeatherJSONtmp.getInt("visibility");

                //  Wind
                hourlyWeatherForecasttmp.windSpeed = hourlyForecastWeatherJSONtmp.getDouble("wind_speed");
                hourlyWeatherForecasttmp.windGustSpeed = hourlyForecastWeatherJSONtmp.getDouble("wind_gust_speed");
                hourlyWeatherForecasttmp.windDirection = hourlyForecastWeatherJSONtmp.getInt("wind_direction");

                //  Precipitations
                ////    PoP -   Probability of Precipitations
                hourlyWeatherForecasttmp.pop = hourlyForecastWeatherJSONtmp.getDouble("pop");
                ////    Rain
                hourlyWeatherForecasttmp.rain = hourlyForecastWeatherJSONtmp.getDouble("rain");
                ////    Snow
                hourlyWeatherForecasttmp.snow = hourlyForecastWeatherJSONtmp.getDouble("snow");

                this.hourlyWeatherForecastArrayList.add(i, hourlyWeatherForecasttmp);
            }
        } else {
            throw new JSONException("Cannot find hourly weather forecast data in PlaceObjectJSON");
        }


        //  Daily Weather Forecast
        //________________________________________________________________
        //

        if (placeObjectJSON.has("daily_weather_forecast")) {
            JSONArray dailyWeatherJSON = placeObjectJSON.getJSONArray("daily_weather_forecast");
            JSONObject dailyWeatherJSONtmp;

            DailyWeatherForecast dailyWeatherForecasttmp;

            for (int i = 0; i < dailyWeatherJSON.length(); i++) {

                dailyWeatherJSONtmp = dailyWeatherJSON.getJSONObject(i);
                dailyWeatherForecasttmp = new DailyWeatherForecast();

                //  Time
                dailyWeatherForecasttmp.dt = dailyWeatherJSONtmp.getLong("dt");

                //  Weather
                dailyWeatherForecasttmp.weather = dailyWeatherJSONtmp.getString("weather");
                dailyWeatherForecasttmp.weatherDescription = dailyWeatherJSONtmp.getString("weather_description");
                dailyWeatherForecasttmp.weatherCode = dailyWeatherJSONtmp.getInt("weather_code");

                //  Temperatures
                dailyWeatherForecasttmp.temperatureMorning = dailyWeatherJSONtmp.getDouble("temperature_morning");
                dailyWeatherForecasttmp.temperatureDay = dailyWeatherJSONtmp.getDouble("temperature_day");
                dailyWeatherForecasttmp.temperatureEvening = dailyWeatherJSONtmp.getDouble("temperature_evening");
                dailyWeatherForecasttmp.temperatureNight = dailyWeatherJSONtmp.getDouble("temperature_night");
                dailyWeatherForecasttmp.temperatureMinimum = dailyWeatherJSONtmp.getDouble("temperature_minimum");
                dailyWeatherForecasttmp.temperatureMaximum = dailyWeatherJSONtmp.getDouble("temperature_maximum");

                //  Feels Like Temperatures
                dailyWeatherForecasttmp.temperatureMorningFeelsLike = dailyWeatherJSONtmp.getDouble("temperature_feelslike_morning");
                dailyWeatherForecasttmp.temperatureDayFeelsLike = dailyWeatherJSONtmp.getDouble("temperature_feelslike_day");
                dailyWeatherForecasttmp.temperatureEveningFeelsLike = dailyWeatherJSONtmp.getDouble("temperature_feelslike_evening");
                dailyWeatherForecasttmp.temperatureNightFeelsLike = dailyWeatherJSONtmp.getDouble("temperature_feelslike_night");

                //  Pressure, Humidity, dewPoint
                dailyWeatherForecasttmp.pressure = dailyWeatherJSONtmp.getInt("pressure");
                dailyWeatherForecasttmp.humidity = dailyWeatherJSONtmp.getInt("humidity");
                dailyWeatherForecasttmp.dewPoint = dailyWeatherJSONtmp.getDouble("dew_point");

                //  Sky
                dailyWeatherForecasttmp.cloudiness = dailyWeatherJSONtmp.getInt("cloudiness");
                dailyWeatherForecasttmp.sunrise = dailyWeatherJSONtmp.getLong("sunrise");
                dailyWeatherForecasttmp.sunset = dailyWeatherJSONtmp.getLong("sunset");

                //  Wind
                dailyWeatherForecasttmp.windSpeed = dailyWeatherJSONtmp.getDouble("wind_speed");
                dailyWeatherForecasttmp.windDirection = dailyWeatherJSONtmp.getInt("wind_direction");
                dailyWeatherForecasttmp.windGustSpeed = dailyWeatherJSONtmp.getDouble("wind_gust_speed");

                //  Precipitations
                ////    PoP -   Probability of Precipitations
                dailyWeatherForecasttmp.pop = dailyWeatherJSONtmp.getDouble("pop");
                ////    Rain
                dailyWeatherForecasttmp.rain = dailyWeatherJSONtmp.getDouble("rain");
                ////    Snow
                dailyWeatherForecasttmp.snow = dailyWeatherJSONtmp.getDouble("snow");

                this.dailyWeatherForecastArrayList.add(i, dailyWeatherForecasttmp);
            }
        } else {
            throw new JSONException("Cannot find daily weather forecast data in PlaceObjectJSON");
        }

    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    //public long getLastUpdateWithTimeZoneOffset() {return lastUpdate + timeZoneOffset;}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isErrorDuringDataAcquisition() {
        return errorDuringDataAcquisition;
    }

    public void setErrorDuringDataAcquisition(boolean errorDuringDataAcquisition) {
        this.errorDuringDataAcquisition = errorDuringDataAcquisition;
    }

    public CurrentWeather getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(CurrentWeather currentWeather) {
        this.currentWeather = currentWeather;
    }

    public ArrayList<MinutelyWeatherForecast> getMinutelyWeatherForecastArrayList() {
        return minutelyWeatherForecastArrayList;
    }

    public void setMinutelyWeatherForecastArrayList(ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList) {
        this.minutelyWeatherForecastArrayList = minutelyWeatherForecastArrayList;
    }

    public ArrayList<HourlyWeatherForecast> getHourlyWeatherForecastArrayList() {
        return hourlyWeatherForecastArrayList;
    }

    public void setHourlyWeatherForecastArrayList(ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList) {
        this.hourlyWeatherForecastArrayList = hourlyWeatherForecastArrayList;
    }

    public ArrayList<DailyWeatherForecast> getDailyWeatherForecastArrayList() {
        return dailyWeatherForecastArrayList;
    }

    public void setDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
        this.dailyWeatherForecastArrayList = dailyWeatherForecastArrayList;
    }

    public MinutelyWeatherForecast getMinutelyWeatherForecast(int minute) {
        return minutelyWeatherForecastArrayList.get(minute);
    }

    public HourlyWeatherForecast getHourlyWeatherForecast(int hour) {
        return hourlyWeatherForecastArrayList.get(hour);
    }

    public DailyWeatherForecast getDailyWeatherForecast(int day) {
        return dailyWeatherForecastArrayList.get(day);
    }

    public void setMinutelyWeatherForecast(int minute, MinutelyWeatherForecast minutelyWeatherForecast) {
        this.minutelyWeatherForecastArrayList.set(minute, minutelyWeatherForecast);
    }

    public void setHourlyWeatherForecast(int hour, HourlyWeatherForecast hourlyWeatherForecast) {
        this.hourlyWeatherForecastArrayList.set(hour, hourlyWeatherForecast);
    }

    public void setDailyWeatherForecast(int day, DailyWeatherForecast dailyWeatherForecast) {
        this.dailyWeatherForecastArrayList.set(day, dailyWeatherForecast);
    }

    public void addMinutelyWeatherForecast(int minute, MinutelyWeatherForecast minutelyWeatherForecast) {
        this.minutelyWeatherForecastArrayList.add(minute, minutelyWeatherForecast);
    }

    public void addHourlyWeatherForecast(int hour, HourlyWeatherForecast hourlyWeatherForecast) {
        this.hourlyWeatherForecastArrayList.add(hour, hourlyWeatherForecast);
    }

    public void addDailyWeatherForecast(int day, DailyWeatherForecast dailyWeatherForecast) {
        this.dailyWeatherForecastArrayList.add(day, dailyWeatherForecast);
    }


    //  getPlaceJSON()
    //________________________________________________________________
    //
    public JSONObject getPlaceJSON() {
        JSONObject placeJSON = new JSONObject();

        try {
            ////    Place
            placeJSON.accumulate("city", city);
            placeJSON.accumulate("country", country);
            placeJSON.accumulate("country_code", countryCode);
            placeJSON.accumulate("latitude", latitude);
            placeJSON.accumulate("longitude", longitude);
            placeJSON.accumulate("timezone", timeZone);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return placeJSON;
    }


    //  getUpdateJSON()
    //________________________________________________________________
    //

    public JSONObject getUpdateJSON() {
        JSONObject updateJSON = new JSONObject();

        try {
            ////    Update
            updateJSON.accumulate("last_update_date", lastUpdateDate);
            updateJSON.accumulate("last_update", lastUpdate);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return updateJSON;
    }


    //  getErrorsJSON()
    //________________________________________________________________
    //

    public JSONObject getErrorsJSON() {
        JSONObject errorsJSON = new JSONObject();

        try {
            ////    Errors during updates
            errorsJSON.accumulate("error_during_data_acquisition", errorDuringDataAcquisition);
            errorsJSON.accumulate("error_code", errorCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return errorsJSON;
    }


    //  getCurrentWeatherJSON()
    //________________________________________________________________
    //

    public JSONObject getCurrentWeatherJSON() {
        JSONObject currentWeatherJSON = new JSONObject();

        try {

            //  Weather content
            ////    Time
            currentWeatherJSON.put("dt", currentWeather.dt);

            ////    Weather
            currentWeatherJSON.accumulate("weather", currentWeather.weather);
            currentWeatherJSON.accumulate("weather_description", currentWeather.weatherDescription);
            currentWeatherJSON.accumulate("weather_code", currentWeather.weatherCode);

            ////    Temperatures
            currentWeatherJSON.accumulate("temperature", currentWeather.temperature);
            currentWeatherJSON.accumulate("temperature_feels_like", currentWeather.temperatureFeelsLike);

            ////    Environmental Variables
            currentWeatherJSON.accumulate("pressure", currentWeather.pressure);
            currentWeatherJSON.accumulate("humidity", currentWeather.humidity);
            currentWeatherJSON.accumulate("dew_point", currentWeather.dewPoint);

            ////    Sky
            currentWeatherJSON.accumulate("cloudiness", currentWeather.cloudiness);
            currentWeatherJSON.accumulate("uvi", currentWeather.uvIndex);
            currentWeatherJSON.accumulate("visibility", currentWeather.visibility);
            currentWeatherJSON.accumulate("sunrise", currentWeather.sunrise);
            currentWeatherJSON.accumulate("sunset", currentWeather.sunset);

            ////    Wind
            currentWeatherJSON.accumulate("wind_speed", currentWeather.windSpeed);
            currentWeatherJSON.accumulate("wind_gust_speed", currentWeather.windGustSpeed);
            currentWeatherJSON.accumulate("wind_readable_direction", currentWeather.isWindDirectionReadable);
            currentWeatherJSON.accumulate("wind_direction", currentWeather.windDirection);

            ////    Precipitations
            currentWeatherJSON.accumulate("rain", currentWeather.rain);
            currentWeatherJSON.accumulate("snow", currentWeather.snow);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currentWeatherJSON;
    }


    //  getMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONObject getMinutelyWeatherForecastJSON(int minute) throws Exception {
        JSONObject minutelyWeatherForecastJSON = new JSONObject();
        MinutelyWeatherForecast minutelyWeatherForecast = this.getMinutelyWeatherForecast(minute);

        try {
            minutelyWeatherForecastJSON.accumulate("dt", minutelyWeatherForecast.dt);
            minutelyWeatherForecastJSON.accumulate("precipitation", minutelyWeatherForecast.precipitation);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return minutelyWeatherForecastJSON;
    }


    //  getAllMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllMinutelyWeatherForecastJSON() throws Exception {
        JSONArray minutelyWeatherForecastJSON = new JSONArray();

        for (int index = 0; index < minutelyWeatherForecastArrayList.size(); index++) {
            minutelyWeatherForecastJSON.put(this.getMinutelyWeatherForecastJSON(index));
        }

        return minutelyWeatherForecastJSON;
    }


    //  getHourlyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONObject getHourlyWeatherForecastJSON(int hour) {
        JSONObject hourlyWeatherForecastJSON = new JSONObject();
        HourlyWeatherForecast hourlyWeatherForecast = this.getHourlyWeatherForecast(hour);

        try {

            //  Hourly Weather Forecast
            ////    Time
            hourlyWeatherForecastJSON.accumulate("dt", hourlyWeatherForecast.dt);

            ////    Weather
            hourlyWeatherForecastJSON.accumulate("weather", hourlyWeatherForecast.weather);
            hourlyWeatherForecastJSON.accumulate("weather_description", hourlyWeatherForecast.weatherDescription);
            hourlyWeatherForecastJSON.accumulate("weather_code", hourlyWeatherForecast.weatherCode);

            ////    Temperatures
            hourlyWeatherForecastJSON.accumulate("temperature", hourlyWeatherForecast.temperature);
            hourlyWeatherForecastJSON.accumulate("temperature_feels_like", hourlyWeatherForecast.temperatureFeelsLike);

            ////    Environmental Variables
            hourlyWeatherForecastJSON.accumulate("pressure", hourlyWeatherForecast.pressure);
            hourlyWeatherForecastJSON.accumulate("humidity", hourlyWeatherForecast.humidity);
            hourlyWeatherForecastJSON.accumulate("dew_point", hourlyWeatherForecast.dewPoint);

            ////    Sky
            hourlyWeatherForecastJSON.accumulate("cloudiness", hourlyWeatherForecast.cloudiness);
            hourlyWeatherForecastJSON.accumulate("visibility", hourlyWeatherForecast.visibility);

            ////    Wind
            hourlyWeatherForecastJSON.accumulate("wind_speed", hourlyWeatherForecast.windSpeed);
            hourlyWeatherForecastJSON.accumulate("wind_gust_speed", hourlyWeatherForecast.windGustSpeed);
            hourlyWeatherForecastJSON.accumulate("wind_direction", hourlyWeatherForecast.windDirection);

            ////    Precipitations
            hourlyWeatherForecastJSON.accumulate("pop", hourlyWeatherForecast.pop);
            hourlyWeatherForecastJSON.accumulate("rain", hourlyWeatherForecast.rain);
            hourlyWeatherForecastJSON.accumulate("snow", hourlyWeatherForecast.snow);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hourlyWeatherForecastJSON;
    }


    //  getAllHourlyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllHourlyWeatherForecastJSON() {
        JSONArray hourlyForecastWeatherJSON = new JSONArray();

        for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {
            hourlyForecastWeatherJSON.put(this.getHourlyWeatherForecastJSON(index));
        }

        return hourlyForecastWeatherJSON;
    }

    //  getDailyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONObject getDailyWeatherForecastJSON(int day) {
        JSONObject dailyWeatherForecastJSON = new JSONObject();
        DailyWeatherForecast dailyWeatherForecast = this.getDailyWeatherForecast(day);

        try {

            //  Daily Weather Forecast
            ////    Time
            dailyWeatherForecastJSON.accumulate("dt", dailyWeatherForecast.dt);

            ////    Weather
            dailyWeatherForecastJSON.accumulate("weather", dailyWeatherForecast.weather);
            dailyWeatherForecastJSON.accumulate("weather_description", dailyWeatherForecast.weatherDescription);
            dailyWeatherForecastJSON.accumulate("weather_code", dailyWeatherForecast.weatherCode);

            ////    Temperatures
            dailyWeatherForecastJSON.accumulate("temperature_morning", dailyWeatherForecast.temperatureMorning);
            dailyWeatherForecastJSON.accumulate("temperature_day", dailyWeatherForecast.temperatureDay);
            dailyWeatherForecastJSON.accumulate("temperature_evening", dailyWeatherForecast.temperatureEvening);
            dailyWeatherForecastJSON.accumulate("temperature_night", dailyWeatherForecast.temperatureNight);
            dailyWeatherForecastJSON.accumulate("temperature_minimum", dailyWeatherForecast.temperatureMinimum);
            dailyWeatherForecastJSON.accumulate("temperature_maximum", dailyWeatherForecast.temperatureMaximum);

            ////    Feels Like
            dailyWeatherForecastJSON.accumulate("temperature_feelslike_morning", dailyWeatherForecast.temperatureMorningFeelsLike);
            dailyWeatherForecastJSON.accumulate("temperature_feelslike_day", dailyWeatherForecast.temperatureDayFeelsLike);
            dailyWeatherForecastJSON.accumulate("temperature_feelslike_evening", dailyWeatherForecast.temperatureEveningFeelsLike);
            dailyWeatherForecastJSON.accumulate("temperature_feelslike_night", dailyWeatherForecast.temperatureNightFeelsLike);

            ////    Environmental Variables
            dailyWeatherForecastJSON.accumulate("pressure", dailyWeatherForecast.pressure);
            dailyWeatherForecastJSON.accumulate("humidity", dailyWeatherForecast.humidity);
            dailyWeatherForecastJSON.accumulate("dew_point", dailyWeatherForecast.dewPoint);

            ////    Sky
            dailyWeatherForecastJSON.accumulate("cloudiness", dailyWeatherForecast.cloudiness);
            dailyWeatherForecastJSON.accumulate("sunrise", dailyWeatherForecast.sunrise);
            dailyWeatherForecastJSON.accumulate("sunset", dailyWeatherForecast.sunset);

            ////    Wind
            dailyWeatherForecastJSON.accumulate("wind_speed", dailyWeatherForecast.windSpeed);
            dailyWeatherForecastJSON.accumulate("wind_gust_speed", dailyWeatherForecast.windGustSpeed);
            dailyWeatherForecastJSON.accumulate("wind_direction", dailyWeatherForecast.windDirection);

            ////    Precipitations
            dailyWeatherForecastJSON.accumulate("pop", dailyWeatherForecast.pop);
            dailyWeatherForecastJSON.accumulate("rain", dailyWeatherForecast.rain);
            dailyWeatherForecastJSON.accumulate("snow", dailyWeatherForecast.snow);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dailyWeatherForecastJSON;
    }

    //  getAllDailyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllDailyWeatherForecastJSON() {
        JSONArray dailyWeatherForecastJSON = new JSONArray();

        for (int index = 0; index < dailyWeatherForecastArrayList.size(); index++) {

            dailyWeatherForecastJSON.put(this.getDailyWeatherForecastJSON(index));
        }

        return dailyWeatherForecastJSON;
    }

    //  getPlaceObjectJSON()
    //________________________________________________________________
    //

    public JSONObject getPlaceObjectJSON() throws Exception {
        JSONObject placeObjectJSON = new JSONObject();

        try {
            placeObjectJSON.accumulate("place", this.getPlaceJSON());
            placeObjectJSON.accumulate("update", this.getUpdateJSON());
            placeObjectJSON.accumulate("errors", this.getErrorsJSON());

            placeObjectJSON.accumulate("current_weather", this.getCurrentWeatherJSON());
            placeObjectJSON.accumulate("minutely_weather_forecast", this.getAllMinutelyWeatherForecastJSON());
            placeObjectJSON.accumulate("hourly_weather_forecast", this.getAllHourlyWeatherForecastJSON());
            placeObjectJSON.accumulate("daily_weather_forecast", this.getAllDailyWeatherForecastJSON());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return placeObjectJSON;
    }
}