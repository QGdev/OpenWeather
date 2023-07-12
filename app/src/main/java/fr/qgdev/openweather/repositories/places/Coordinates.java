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
