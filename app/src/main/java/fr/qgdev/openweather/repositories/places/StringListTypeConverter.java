/*
 *  Copyright (c) 2019 - 2024
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

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * StringListTypeConverter
 * <p>
 *    A class to convert a list of string to a string and vice versa.
 *    It's used to store a list of string in the database.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
public class StringListTypeConverter {
	
	private StringListTypeConverter() {
	}
	
	@TypeConverter
	public static List<String> fromString(String value) {
		Type listType = new TypeToken<List<String>>() {
		}.getType();
		return new Gson().fromJson(value, listType);
	}
	
	@TypeConverter
	public static String fromList(List<String> list) {
		Gson gson = new Gson();
		return gson.toJson(list);
	}
}