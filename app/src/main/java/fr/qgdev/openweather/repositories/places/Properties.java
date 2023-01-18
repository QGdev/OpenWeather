package fr.qgdev.openweather.repositories.places;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

@Entity(tableName = "properties")
public class Properties {
	
	private final long creationTime;
	private final int timeOffset;
	@PrimaryKey(autoGenerate = false)
	private final int placeId;
	private long lastSuccessfulWeatherUpdateTime;
	private long lastSuccessfulAirQualityUpdateTime;
	private long lastWeatherUpdateAttemptTime;
	private long lastAirQualityUpdateAttemptTime;
	private long lastAvailableWeatherDataTime;
	private long lastAvailableAirQualityDataTime;
	private int order;
	
	public Properties(long lastSuccessfulWeatherUpdateTime, long lastSuccessfulAirQualityUpdateTime, long lastWeatherUpdateAttemptTime, long lastAirQualityUpdateAttemptTime, long creationTime, long lastAvailableWeatherDataTime, long lastAvailableAirQualityDataTime, int timeOffset, int order, int placeId) {
		
		if (lastSuccessfulWeatherUpdateTime < 0)
			throw new IllegalArgumentException("lastSuccessfulWeatherUpdateTime must be positive or null !");
		if (lastSuccessfulAirQualityUpdateTime < 0)
			throw new IllegalArgumentException("lastSuccessfulAirQualityUpdateTime must be positive or null !");
		if (lastWeatherUpdateAttemptTime < 0)
			throw new IllegalArgumentException("lastWeatherUpdateAttemptTime must be positive or null !");
		if (lastAirQualityUpdateAttemptTime < 0)
			throw new IllegalArgumentException("lastAirQualityUpdateAttemptTime must be positive or null !");
		if (creationTime < 0)
			throw new IllegalArgumentException("creationTime must be positive or null !");
		if (lastAvailableWeatherDataTime < 0)
			throw new IllegalArgumentException("lastAvailableWeatherDataTime must be positive or null !");
		if (lastAvailableAirQualityDataTime < 0)
			throw new IllegalArgumentException("lastAvailableAirQualityDataTime must be positive or null !");
		
		this.lastSuccessfulWeatherUpdateTime = lastSuccessfulWeatherUpdateTime;
		this.lastSuccessfulAirQualityUpdateTime = lastSuccessfulAirQualityUpdateTime;
		
		this.lastWeatherUpdateAttemptTime = lastWeatherUpdateAttemptTime;
		this.lastAirQualityUpdateAttemptTime = lastAirQualityUpdateAttemptTime;
		
		this.creationTime = creationTime;
		
		this.lastAvailableWeatherDataTime = lastAvailableWeatherDataTime;
		this.lastAvailableAirQualityDataTime = lastAvailableAirQualityDataTime;
		
		this.timeOffset = timeOffset;
		this.order = order;
		this.placeId = placeId;
	}
	
	@Ignore
	public Properties(long creationTime, int timeOffset, int order, int placeId) {
		if (creationTime < 0)
			throw new IllegalArgumentException("creationTime must be positive or null !");
		
		lastSuccessfulWeatherUpdateTime = 0;
		lastSuccessfulAirQualityUpdateTime = 0;
		lastWeatherUpdateAttemptTime = 0;
		lastAirQualityUpdateAttemptTime = 0;
		lastAvailableWeatherDataTime = 0;
		lastAvailableAirQualityDataTime = 0;
		
		this.creationTime = creationTime;
		this.timeOffset = timeOffset;
		this.order = order;
		this.placeId = placeId;
	}
	
	public long getLastSuccessfulWeatherUpdateTime() {
		return lastSuccessfulWeatherUpdateTime;
	}
	
	public void setLastSuccessfulWeatherUpdateTime(long lastSuccessfulWeatherUpdateTime) {
		if (lastSuccessfulWeatherUpdateTime < 0)
			throw new IllegalArgumentException("lastSuccessfulWeatherUpdateTime must be positive or null !");
		
		this.lastSuccessfulWeatherUpdateTime = lastSuccessfulWeatherUpdateTime;
	}
	
	public long getLastSuccessfulAirQualityUpdateTime() {
		return lastSuccessfulAirQualityUpdateTime;
	}
	
	public void setLastSuccessfulAirQualityUpdateTime(long lastSuccessfulAirQualityUpdateTime) {
		if (lastSuccessfulAirQualityUpdateTime < 0)
			throw new IllegalArgumentException("lastSuccessfulAirQualityUpdateTime must be positive or null !");
		
		this.lastSuccessfulAirQualityUpdateTime = lastSuccessfulAirQualityUpdateTime;
	}
	
	public long getLastWeatherUpdateAttemptTime() {
		return lastWeatherUpdateAttemptTime;
	}
	
	public void setLastWeatherUpdateAttemptTime(long lastWeatherUpdateAttemptTime) {
		if (lastWeatherUpdateAttemptTime < 0)
			throw new IllegalArgumentException("lastWeatherUpdateAttemptTime must be positive or null !");
		
		this.lastWeatherUpdateAttemptTime = lastWeatherUpdateAttemptTime;
	}
	
	public long getLastAirQualityUpdateAttemptTime() {
		return lastAirQualityUpdateAttemptTime;
	}
	
	public void setLastAirQualityUpdateAttemptTime(long lastAirQualityUpdateAttemptTime) {
		if (lastAirQualityUpdateAttemptTime < 0)
			throw new IllegalArgumentException("lastAirQualityUpdateAttemptTime must be positive or null !");
		
		this.lastAirQualityUpdateAttemptTime = lastAirQualityUpdateAttemptTime;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	public long getLastAvailableWeatherDataTime() {
		return lastAvailableWeatherDataTime;
	}
	
	public void setLastAvailableWeatherDataTime(long lastAvailableWeatherDataTime) {
		if (lastAvailableWeatherDataTime < 0)
			throw new IllegalArgumentException("lastAvailableWeatherDataTime must be positive or null !");
		
		this.lastAvailableWeatherDataTime = lastAvailableWeatherDataTime;
	}
	
	public long getLastAvailableAirQualityDataTime() {
		return lastAvailableAirQualityDataTime;
	}
	
	public void setLastAvailableAirQualityDataTime(long lastAvailableAirQualityDataTime) {
		if (lastAvailableAirQualityDataTime < 0)
			throw new IllegalArgumentException("lastAvailableAirQualityDataTime must be positive or null !");
		
		this.lastAvailableAirQualityDataTime = lastAvailableAirQualityDataTime;
	}
	
	public int getTimeOffset() {
		return timeOffset;
	}
	
	public String getTimeZoneStringForm() {
		if (this.timeOffset == 0) return "UTC";
		else return String.format(Locale.getDefault(), "UTC%+d", this.timeOffset / 3600);
	}
	
	public TimeZone getTimeZone() {
		return new SimpleTimeZone(this.timeOffset * 1000, "UTC");
	}
	
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	public int getPlaceId() {
		return placeId;
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return "Properties{" +
				  "lastSuccessfulWeatherUpdateTime=" + lastSuccessfulWeatherUpdateTime +
				  ", lastSuccessfulAirQualityUpdateTime=" + lastSuccessfulAirQualityUpdateTime +
				  ", lastWeatherUpdateAttemptTime=" + lastWeatherUpdateAttemptTime +
				  ", lastAirQualityUpdateAttemptTime=" + lastAirQualityUpdateAttemptTime +
				  ", creationTime=" + creationTime +
				  ", lastAvailableWeatherDataTime=" + lastAvailableWeatherDataTime +
				  ", lastAvailableAirQualityDataTime=" + lastAvailableAirQualityDataTime +
				  ", timeOffset=" + timeOffset +
				  ", order=" + order +
				  ", id=" + placeId +
				  '}';
	}
}
