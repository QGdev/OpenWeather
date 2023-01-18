package fr.qgdev.openweather.repositories.places;

public class Coordinates {
	private double latitude;
	private double longitude;
	
	public Coordinates(double latitude, double longitude) {
		if (!isCorrectForLatitude(latitude))
			throw new IllegalArgumentException("Latitude must be included in [-90°; 90°] !");
		if (!isCorrectForLongitude(longitude))
			throw new IllegalArgumentException("Longitude must be included in [-180°; 180°] !");
		
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	private boolean isCorrectForLatitude(double value) {
		return value <= 90 && value >= -90;
	}
	
	private boolean isCorrectForLongitude(double value) {
		return value <= 180 && value >= -180;
	}
	
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		if (!isCorrectForLatitude(latitude))
			throw new IllegalArgumentException("Latitude must be included in [-90°; 90°] !");
		
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		if (!isCorrectForLongitude(longitude))
			throw new IllegalArgumentException("Longitude must be included in [-180°; 180°] !");
		
		this.longitude = longitude;
	}
}
