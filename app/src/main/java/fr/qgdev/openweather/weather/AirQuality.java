package fr.qgdev.openweather.weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class AirQuality {
    public int aqi;
    public float co;
    public float no;
    public float no2;
    public float o3;
    public float so2;
    public float pm2_5;
    public float pm10;
    public float nh3;

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

    public AirQuality(JSONObject airQuality) throws JSONException {
        this.aqi = airQuality.getInt("aqi");
        this.co = BigDecimal.valueOf(airQuality.getDouble("co")).floatValue();
        this.no = BigDecimal.valueOf(airQuality.getDouble("no")).floatValue();
        this.no2 = BigDecimal.valueOf(airQuality.getDouble("no2")).floatValue();
        this.o3 = BigDecimal.valueOf(airQuality.getDouble("o3")).floatValue();
        this.so2 = BigDecimal.valueOf(airQuality.getDouble("so2")).floatValue();
        this.pm2_5 = BigDecimal.valueOf(airQuality.getDouble("pm2_5")).floatValue();
        this.pm10 = BigDecimal.valueOf(airQuality.getDouble("pm10")).floatValue();
        this.nh3 = BigDecimal.valueOf(airQuality.getDouble("nh3")).floatValue();

    }

    public void fillWithOWMData(JSONObject airQuality) throws JSONException {
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

    public JSONObject getJSONObject() throws JSONException {
        JSONObject airQualityJSON = new JSONObject();

        airQualityJSON.accumulate("aqi", this.aqi);
        airQualityJSON.accumulate("co", this.co);
        airQualityJSON.accumulate("no", this.no);
        airQualityJSON.accumulate("no2", this.no2);
        airQualityJSON.accumulate("o3", this.o3);
        airQualityJSON.accumulate("so2", this.so2);
        airQualityJSON.accumulate("pm2_5", this.pm2_5);
        airQualityJSON.accumulate("pm10", this.pm10);
        airQualityJSON.accumulate("nh3", this.nh3);

        return airQualityJSON;
    }


    @Override
    public AirQuality clone() {
        AirQuality clonedObject = new AirQuality();
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
}
