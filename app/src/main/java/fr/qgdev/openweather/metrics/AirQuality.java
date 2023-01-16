package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

@Entity(tableName = "air_quality")

public class AirQuality {
	
	@PrimaryKey(autoGenerate = false)
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
	
	//  Setter
	
	public int getPlaceId() {
		return placeId;
	}
	
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	public int getAqi() {
		return aqi;
	}
	
	public void setAqi(int aqi) {
		this.aqi = aqi;
	}
	
	public float getCo() {
		return co;
	}
	
	public void setCo(float co) {
		this.co = co;
	}
	
	public float getNo() {
		return no;
	}
	
	public void setNo(float no) {
		this.no = no;
	}
	
	public float getNo2() {
		return no2;
	}
	
	public void setNo2(float no2) {
		this.no2 = no2;
	}
	
	//  Getters
	
	public float getO3() {
		return o3;
	}
	
	public void setO3(float o3) {
		this.o3 = o3;
	}
	
	public float getSo2() {
		return so2;
	}
	
	public void setSo2(float so2) {
		this.so2 = so2;
	}
	
	public float getPm2_5() {
		return pm2_5;
	}
	
	public void setPm2_5(float pm2_5) {
		this.pm2_5 = pm2_5;
	}
	
	public float getPm10() {
		return pm10;
	}
	
	public void setPm10(float pm10) {
		this.pm10 = pm10;
	}
	
	public float getNh3() {
		return nh3;
	}
	
	public void setNh3(float nh3) {
		this.nh3 = nh3;
	}
	
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