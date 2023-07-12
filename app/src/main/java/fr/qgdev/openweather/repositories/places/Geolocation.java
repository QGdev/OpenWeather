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

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.StringJoiner;

@Entity(tableName = "geolocation",
		  primaryKeys = {"placeId"})
public class Geolocation {
	
	private final int placeId;
	private String city;
	private String countryCode;
	
	@Embedded
	private Coordinates coordinates;
	
	@Ignore
	public Geolocation(int placeId, @NonNull Coordinates coordinates) {
		this.placeId = placeId;
		this.coordinates = coordinates;
	}
	
	public Geolocation(int placeId, @NonNull String city, @NonNull String countryCode, @NonNull Coordinates coordinates) {
		this.placeId = placeId;
		this.city = city;
		this.countryCode = countryCode;
		this.coordinates = coordinates;
	}
	
	public int getPlaceId() {
		return this.placeId;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(@NonNull String city) {
		this.city = city;
	}
	
	public String getCountryCode() {
		return countryCode;
	}
	
	public void setCountryCode(@NonNull String countryCode) {
		this.countryCode = countryCode;
	}
	
	public Coordinates getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(@NonNull Coordinates coordinates) {
		this.coordinates = coordinates;
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", Geolocation.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("city='" + city + "'")
				  .add("countryCode='" + countryCode + "'")
				  .add("coordinates=" + coordinates)
				  .toString();
	}
}
