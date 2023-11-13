
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsBooleanValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsFloatValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsIntValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsLongValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsShortValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsStringValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class CurrentWeatherTest {
	
	private CurrentWeather currentWeather;
	
	
	/**
	 * Sets up.
	 */
	@Before
	public void setUp() {
		currentWeather = new CurrentWeather();
	}
	
	/**
	 * Test the basic constructor.
	 * Test will not pass if at least one of the values isn't valid
	 */
	@Test
	public void basicConstructor() {
		CurrentWeather currentWeather = new CurrentWeather();
		
		String placeId = currentWeather.getPlaceId();
		long dt = currentWeather.getDt();
		String weather = currentWeather.getWeather();
		String weatherDescription = currentWeather.getWeatherDescription();
		int weatherCode = currentWeather.getWeatherCode();
		
		int pressure = currentWeather.getPressure();
		int humidity = currentWeather.getHumidity();
		
		int cloudiness = currentWeather.getCloudiness();
		int uvIndex = currentWeather.getUvIndex();
		int visibility = currentWeather.getVisibility();
		
		long sunriseDt = currentWeather.getSunriseDt();
		long sunsetDt = currentWeather.getSunsetDt();
		
		float windSpeed = currentWeather.getWindSpeed();
		float windGustSpeed = currentWeather.getWindGustSpeed();
		short windDirection = currentWeather.getWindDirection();
		
		float rain = currentWeather.getRain();
		float snow = currentWeather.getSnow();
		
		assertNull(placeId);
		assertTrue(dt >= 0);
		assertEquals("", weather);
		assertEquals("", weatherDescription);
		assertTrue(weatherCode >= 0);
		
		assertTrue(pressure >= 0);
		assertTrue(humidity >= 0);
		
		assertTrue(cloudiness >= 0);
		assertTrue(uvIndex >= 0);
		assertTrue(visibility >= 0);
		
		assertTrue(sunriseDt >= 0);
		assertTrue(sunsetDt >= 0);
		
		assertTrue(windSpeed >= 0);
		assertTrue(windGustSpeed >= 0);
		assertTrue(windDirection >= 0 && windDirection <= 360);
		
		assertTrue(rain >= 0);
		assertTrue(snow >= 0);
	}
	
	/**
	 * Used to construct a json object with all the values
	 *
	 * @param dt                   Unix timestamp in seconds
	 * @param weather              Weather main description
	 * @param weatherDescription   Weather description
	 * @param weatherCode          Weather code
	 * @param temperature          Temperature in Kelvin
	 * @param feelsLikeTemperature Feels like temperature in Kelvin
	 * @param pressure             Atmospheric pressure in hPa
	 * @param humidity             Humidity in %
	 * @param dewPoint             Dew point in Kelvin
	 * @param cloudiness           Cloudiness in %
	 * @param uvIndex              UV index
	 * @param visibility           Visibility in meters
	 * @param sunriseDt            Sunrise time in Unix timestamp in seconds
	 * @param sunsetDt             Sunset time in Unix timestamp in seconds
	 * @param windSpeed            Wind speed in meters per second
	 * @param windGustSpeed        Wind gust speed in meters per second
	 * @param windDirection        Wind direction in degrees
	 * @param rain                 Rain volume in millimeters for the last hour
	 * @param snow                 Snow volume in millimeters for the last hour
	 * @return A json object with all the values required for a test of the constructor
	 * @throws JSONException Throws an exception if the json object cannot be created
	 */
	private JSONObject constructCurrentWeatherJson(long dt, String weather, String weatherDescription, int weatherCode,
																  float temperature, float feelsLikeTemperature, int pressure,
																  int humidity, float dewPoint, int cloudiness, int uvIndex, int visibility,
																  long sunriseDt, long sunsetDt, float windSpeed, float windGustSpeed, short windDirection,
																  float rain, float snow) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("dt", dt);
		
		JSONObject weatherObject = new JSONObject();
		weatherObject.put("main", weather);
		weatherObject.put("description", weatherDescription);
		weatherObject.put("id", weatherCode);
		
		JSONArray weatherArray = new JSONArray();
		weatherArray.put(weatherObject);
		jsonObject.put("weather", weatherArray);
		
		jsonObject.put("temp", temperature);
		jsonObject.put("feels_like", feelsLikeTemperature);
		
		jsonObject.put("pressure", pressure);
		jsonObject.put("humidity", humidity);
		jsonObject.put("dew_point", dewPoint);
		
		jsonObject.put("uvi", uvIndex);
		jsonObject.put("clouds", cloudiness);
		jsonObject.put("visibility", visibility);
		
		jsonObject.put("sunrise", sunriseDt);
		jsonObject.put("sunset", sunsetDt);
		
		jsonObject.put("wind_speed", windSpeed);
		jsonObject.put("wind_gust", windGustSpeed);
		jsonObject.put("wind_deg", windDirection);
		
		jsonObject.put("rain", new JSONObject().put("1h", rain));
		jsonObject.put("snow", new JSONObject().put("1h", snow));
		
		return jsonObject;
	}
	
	/**
	 * Test the json constructor with "all" possible out of bounds values to test safeness
	 *
	 * @throws JSONException Throws an exception if the json object cannot be created
	 */
	@Test
	public void jsonConstructorOutBoundsTest() throws JSONException {
		long[] dtTestValues = {-9223372036854775L, -1, Long.MAX_VALUE};
		String[] stringTestValues = {null};
		//	For this following line, cannot get forbidden values on an
		int[] positiveIntTestValues = {Integer.MIN_VALUE, -1};
		float[] positiveFloatTestValues = {-Float.MIN_VALUE, -1};
		int[] percentTestValues = {-1, 101};
		short[] directionTestValues = {-1, 361};
		
		// We will test each values with abnormal values
		// Beginning with dt values
		for (int i = 0; i < 3; i++) {
			long[] testedValues = {0, 0, 0};
			for (long dt : dtTestValues) {
				testedValues[i] = dt;
				JSONObject jsonObject = constructCurrentWeatherJson(testedValues[0], "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, testedValues[1], testedValues[2], 0, 0, (short) 0, 0, 0);
				assertThrows(IllegalArgumentException.class, () -> new CurrentWeather(jsonObject));
			}
		}
		
		// Then string values
		for (int i = 0; i < 1; i++) {
			String[] testedValues = {"", ""};
			for (String string : stringTestValues) {
				testedValues[i] = string;
				JSONObject jsonObject = constructCurrentWeatherJson(0, testedValues[0], testedValues[1], 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (short) 0, 0, 0);
				assertThrows(Exception.class, () -> new CurrentWeather(jsonObject));
			}
		}
		
		// Then float values like wind speed and precipitation
		for (int i = 0; i < 4; i++) {
			float[] testedValues = {0, 0, 0, 0};
			for (float floatValue : positiveFloatTestValues) {
				testedValues[i] = floatValue;
				JSONObject jsonObject = constructCurrentWeatherJson(0, "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, testedValues[0], testedValues[1], (short) 0, testedValues[2], testedValues[3]);
				assertThrows(IllegalArgumentException.class, () -> new CurrentWeather(jsonObject));
			}
		}
		
		// Then int values like pressure, uv index and visibility
		for (int i = 0; i < 3; i++) {
			int[] testedValues = {0, 0, 0};
			for (int intValue : positiveIntTestValues) {
				testedValues[i] = intValue;
				JSONObject jsonObject = constructCurrentWeatherJson(0, "", "", 0, 0, 0, testedValues[0], 0, 0, 0, testedValues[1], testedValues[2], 0, 0, 0, 0, (short) 0, 0, 0);
				assertThrows(IllegalArgumentException.class, () -> new CurrentWeather(jsonObject));
			}
		}
		
		// Then percent values like humidity and cloudiness
		for (int i = 0; i < 2; i++) {
			int[] testedValues = {0, 0};
			for (int percentValue : percentTestValues) {
				testedValues[i] = percentValue;
				JSONObject jsonObject = constructCurrentWeatherJson(0, "", "", 0, 0, 0, 0, testedValues[0], 0, testedValues[1], 0, 0, 0, 0, 0, 0, (short) 0, 0, 0);
				assertThrows(IllegalArgumentException.class, () -> new CurrentWeather(jsonObject));
			}
		}
		
		// Then we will finish with direction values
		for (short directionValue : directionTestValues) {
			JSONObject jsonObject = constructCurrentWeatherJson(0, "", "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, directionValue, 0, 0);
			assertThrows(IllegalArgumentException.class, () -> new CurrentWeather(jsonObject));
		}
	}
	
	/**
	 * Test the json constructor with all possible values.
	 * Test will pass only if the value is the same as the returned value
	 *
	 * @throws JSONException Throws an exception if the json object cannot be created
	 */
	@Test
	public void jsonConstructorTest() throws JSONException {
		long[] dtTestValues = {0, 1, 9223372036854775L};
		String[] stringTestValues = {"", "test", "test test"};
		float[] floatTestValues = {-Float.MIN_VALUE, -1, 0, 1, Float.MAX_VALUE};
		int[] positiveIntTestValues = {0, 1, Integer.MAX_VALUE};
		float[] positiveFloatTestValues = {0, 1, Float.MAX_VALUE};
		int[] percentTestValues = {0, 50, 100};
		short[] directionTestValues = {0, 180, 360};
		
		// Test all possible combinations of values (Kind of)
		//	Know nested loops are ugly but it's the only way to test "all" possible combinations
		for (long dtValue : dtTestValues) {
			for (String stringValue : stringTestValues) {
				for (int intValue : positiveIntTestValues) {
					for (float floatValue : floatTestValues) {
						for (short shortValue : directionTestValues) {
							for (int percentValue : percentTestValues) {
								for (float positiveFloatValue : positiveFloatTestValues) {
									JSONObject jsonObject = constructCurrentWeatherJson(dtValue, stringValue, stringValue, intValue,
											  floatValue, floatValue, intValue, percentValue, floatValue, percentValue, intValue, intValue,
											  dtValue, dtValue, positiveFloatValue, positiveFloatValue, shortValue, positiveFloatValue,
											  positiveFloatValue);
									
									CurrentWeather currentWeather = new CurrentWeather(jsonObject);
									
									long dtMillis = dtValue * 1000;
									
									assertEquals(dtMillis, currentWeather.getDt());
									assertEquals(stringValue, currentWeather.getWeather());
									assertEquals(stringValue, currentWeather.getWeatherDescription());
									assertEquals(intValue, currentWeather.getWeatherCode());
									
									assertEquals(intValue, currentWeather.getPressure());
									assertEquals(percentValue, currentWeather.getHumidity());
									
									assertEquals(percentValue, currentWeather.getCloudiness());
									assertEquals(intValue, currentWeather.getUvIndex());
									assertEquals(intValue, currentWeather.getVisibility());
									
									assertEquals(dtMillis, currentWeather.getSunriseDt());
									assertEquals(dtMillis, currentWeather.getSunsetDt());
									
									assertEquals(positiveFloatValue, currentWeather.getWindSpeed(), 0F);
									assertEquals(positiveFloatValue, currentWeather.getWindGustSpeed(), 0F);
									assertEquals(shortValue, currentWeather.getWindDirection());
									
									assertEquals(positiveFloatValue, currentWeather.getRain(), 0F);
									assertEquals(positiveFloatValue, currentWeather.getSnow(), 0F);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Test placeId getter and setter with all possibles values.
	 * Test will pass only if the placeId returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetPlaceIdTest() throws Exception {
		String[] testValues = {"", "TeSt", "test", "TEST", "Test", "tEsT"};
		
		testAssertEqualsStringValues(testValues, id -> currentWeather.setPlaceId(id), currentWeather::getPlaceId);
	}
	
	/**
	 * Test dt getter and setter with all possibles values.
	 * Test will pass only if the dt returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetDtTest() throws Exception {
		long[] testValues = {0, 1, Long.MAX_VALUE};
		
		testAssertEqualsLongValues(testValues, currentWeather::setDt, () -> currentWeather.getDt());
	}
	
	/**
	 * Test the dt setter with all possibles out of bounds values.
	 * Test will pass only if the dt value is the same as the previous value.
	 */
	@Test
	public void setDtOutBoundsTest() {
		long[] testValues = {Long.MIN_VALUE, -1};
		long expected = currentWeather.getDt();
		
		for (long testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setDt(testValue));
			assertEquals(expected, currentWeather.getDt());
		}
	}
	
	/**
	 * Test the weather setter with all possibles out of bounds values.
	 * Test will pass only if the weather value is the same as the previous value.
	 */
	@Test
	public void setWeatherOutBoundsTest() {
		String[] testValues = {null};
		String expected = currentWeather.getWeather();
		
		for (String testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWeather(testValue));
			assertEquals(expected, currentWeather.getWeather());
		}
	}
	
	/**
	 * Test weather getter and setter with all possibles values.
	 * Test will pass only if the weather returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetWeatherTest() throws Exception {
		String[] testValues = {"", "TeSt", "test", "TEST", "Test", "tEsT"};
		
		testAssertEqualsStringValues(testValues, currentWeather::setWeather, () -> currentWeather.getWeather());
	}
	
	/**
	 * Test the weatherDescription setter with all possibles out of bounds values.
	 * Test will pass only if the weatherDescription value is the same as the previous value.
	 */
	@Test
	public void setWeatherDescriptionOutBoundsTest() {
		String[] testValues = {null};
		String expected = currentWeather.getWeatherDescription();
		
		for (String testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWeatherDescription(testValue));
			assertEquals(expected, currentWeather.getWeatherDescription());
		}
	}
	
	/**
	 * Test weatherDescription getter and setter with all possibles values.
	 * Test will pass only if the weatherDescription returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetWeatherDescriptionTest() throws Exception {
		String[] testValues = {"", "TeSt", "test", "TEST", "Test", "tEsT"};
		
		testAssertEqualsStringValues(testValues, currentWeather::setWeatherDescription, () -> currentWeather.getWeatherDescription());
	}
	
	/**
	 * Test the weatherCode setter with all possibles out of bounds values.
	 * Test will pass only if the weatherCode value is the same as the previous value.
	 */
	@Test
	public void setWeatherCodeOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1};
		int expected = currentWeather.getWeatherCode();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWeatherCode(testValue));
			assertEquals(expected, currentWeather.getWeatherCode());
		}
	}
	
	/**
	 * Test weatherCode getter and setter with all possibles values.
	 * Test will pass only if the weatherCode returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetAndSetWeatherCodeTest() throws Exception {
		int[] testValues = {0, 1, Integer.MAX_VALUE};
		
		testAssertEqualsIntValues(testValues, currentWeather::setWeatherCode, () -> currentWeather.getWeatherCode());
	}
	
	/**
	 * Test temperature getter and setter with all possibles values.
	 * Test will pass only if the temperature returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetAndSetTemperatureTest() throws Exception {
		float[] testValues = {-Float.MIN_VALUE, -1, 0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setTemperature, () -> currentWeather.getTemperature(), 0F);
	}
	
	/**
	 * Test temperatureFeelsLike getter and setter with all possibles values.
	 * Test will pass only if the temperature returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetTemperatureFeelsLikeTest() throws Exception {
		float[] testValues = {-Float.MIN_VALUE, -1, 0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setTemperatureFeelsLike, () -> currentWeather.getTemperatureFeelsLike(), 0F);
	}
	
	/**
	 * Test the pressure setter with all possibles out of bounds values.
	 * Test will pass only if the pressure value is the same as the previous value.
	 */
	@Test
	public void setPressureOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1};
		int expected = currentWeather.getPressure();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setPressure(testValue));
			assertEquals(expected, currentWeather.getPressure());
		}
	}
	
	/**
	 * Test pressure getter and setter with all possibles values.
	 * Test will pass only if the pressure returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetPressureTest() throws Exception {
		int[] testValues = {0, 1, Integer.MAX_VALUE};
		
		testAssertEqualsIntValues(testValues, currentWeather::setPressure, () -> currentWeather.getPressure());
	}
	
	/**
	 * Test the humidity setter with all possibles out of bounds values.
	 * Test will pass only if the humidity value is the same as the previous value.
	 */
	@Test
	public void setHumidityOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1, 101, Integer.MAX_VALUE};
		int expected = currentWeather.getHumidity();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setHumidity(testValue));
			assertEquals(expected, currentWeather.getHumidity());
		}
	}
	
	/**
	 * Test humidity getter and setter with all possibles values.
	 * Test will pass only if the humidity returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetHumidityTest() throws Exception {
		int[] testValues = {0, 25, 50, 75, 100};
		
		testAssertEqualsIntValues(testValues, currentWeather::setHumidity, () -> currentWeather.getHumidity());
	}
	
	/**
	 * Test dewPoint getter and setter with all possibles values.
	 * Test will pass only if the dewPoint returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetDewPointTest() throws Exception {
		float[] testValues = {-Float.MIN_VALUE, -1, 0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setDewPoint, () -> currentWeather.getDewPoint(), 0F);
	}
	
	/**
	 * Test the cloudiness setter with all possibles out of bounds values.
	 * Test will pass only if the cloudiness value is the same as the previous value.
	 */
	@Test
	public void setCloudinessOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1, 101, Integer.MAX_VALUE};
		int expected = currentWeather.getCloudiness();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setCloudiness(testValue));
			assertEquals(expected, currentWeather.getCloudiness());
		}
	}
	
	/**
	 * Test cloudiness getter and setter with all possibles values.
	 * Test will pass only if the cloudiness returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetCloudinessTest() throws Exception {
		int[] testValues = {0, 25, 50, 75, 100};
		
		testAssertEqualsIntValues(testValues, currentWeather::setCloudiness, () -> currentWeather.getCloudiness());
	}
	
	/**
	 * Test the uvIndex setter with all possibles out of bounds values.
	 * Test will pass only if the uvIndex value is the same as the previous value.
	 */
	@Test
	public void setUvIndexOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1};
		int expected = currentWeather.getUvIndex();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setUvIndex(testValue));
			assertEquals(expected, currentWeather.getUvIndex());
		}
	}
	
	/**
	 * Test uvIndex getter and setter with all possibles values.
	 * Test will pass only if the uvIndex returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetUvIndexTest() throws Exception {
		int[] testValues = {0, 1, Integer.MAX_VALUE};
		
		testAssertEqualsIntValues(testValues, currentWeather::setUvIndex, () -> currentWeather.getUvIndex());
	}
	
	/**
	 * Test the visibility setter with all possibles out of bounds values.
	 * Test will pass only if the visibility value is the same as the previous value.
	 */
	@Test
	public void setVisibilityOutBoundsTest() {
		int[] testValues = {Integer.MIN_VALUE, -1};
		int expected = currentWeather.getVisibility();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setVisibility(testValue));
			assertEquals(expected, currentWeather.getVisibility());
		}
	}
	
	/**
	 * Test visibility getter and setter with all possibles values.
	 * Test will pass only if the visibility returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetVisibilityTest() throws Exception {
		int[] testValues = {0, 1, Integer.MAX_VALUE};
		
		testAssertEqualsIntValues(testValues, currentWeather::setVisibility, () -> currentWeather.getVisibility());
	}
	
	/**
	 * Test the sunriseDt setter with all possibles out of bounds values.
	 * Test will pass only if the sunriseDt value is the same as the previous value.
	 */
	@Test
	public void setSunriseDtOutBoundsTest() {
		long[] testValues = {Long.MIN_VALUE, -1};
		long expected = currentWeather.getSunriseDt();
		
		for (long testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setSunriseDt(testValue));
			assertEquals(expected, currentWeather.getSunriseDt());
		}
	}
	
	/**
	 * Test sunriseDt getter and setter with all possibles values.
	 * Test will pass only if the sunsetDt value is the same as the previous value.
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetSunriseDtTest() throws Exception {
		long[] testValues = {0, 1, Long.MAX_VALUE};
		
		testAssertEqualsLongValues(testValues, currentWeather::setSunriseDt, () -> currentWeather.getSunriseDt());
	}
	
	/**
	 * Test the sunsetDt setter with all possibles out of bounds values.
	 * Test will pass only if the sunsetDt value is the same as the previous value.
	 */
	@Test
	public void setSunsetDtOutBoundsTest() {
		long[] testValues = {Long.MIN_VALUE, -1};
		long expected = currentWeather.getSunsetDt();
		
		for (long testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setSunsetDt(testValue));
			assertEquals(expected, currentWeather.getSunsetDt());
		}
	}
	
	/**
	 * Test sunsetDt getter and setter with all possibles values.
	 * Test will pass only if the sunsetDt returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetSunsetDtTest() throws Exception {
		long[] testValues = {0, 1, Long.MAX_VALUE};
		
		testAssertEqualsLongValues(testValues, currentWeather::setSunsetDt, () -> currentWeather.getSunsetDt());
	}
	
	/**
	 * Test the isDaytime method with all possibles values.
	 * Test will pass only if the isDaytime returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void isDaytimeTest() throws Exception {
		long[] testValues = {0, 10000, 20000, 30000, 40000, 50000, 60000, 70000};
		boolean[] expectedResults = {false, false, false, true, true, false, false, false};
		currentWeather.setSunriseDt(testValues[2]);
		currentWeather.setSunsetDt(testValues[5]);
		
		if (testValues.length != expectedResults.length)
			throw new Exception("testValues and expectedResults needs to have the same sizes");
		
		for (int i = 0; i < testValues.length; i++) {
			currentWeather.setDt(testValues[i]);
			assertEquals(expectedResults[i], currentWeather.isDaytime());
		}
	}
	
	/**
	 * Test the windSpeed setter with all possibles out of bounds values.
	 * Test will pass only if the windSpeed value is the same as the previous value.
	 */
	@Test
	public void setWindSpeedOutBoundsTest() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = currentWeather.getWindSpeed();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWindSpeed(testValue));
			assertEquals(expected, currentWeather.getWindSpeed(), 0F);
		}
	}
	
	/**
	 * Test windSpeed getter and setter with all possibles values.
	 * Test will pass only if the windGustSpeed value is the same as the previous value.
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetWindSpeedTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setWindSpeed, () -> currentWeather.getWindSpeed(), 0F);
	}
	
	/**
	 * Test the windGustSpeed setter with all possibles out of bounds values.
	 * Test will pass only if the windGustSpeed value is the same as the previous value.
	 */
	@Test
	public void setWindGustSpeedOutBoundsTest() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = currentWeather.getWindGustSpeed();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWindGustSpeed(testValue));
			assertEquals(expected, currentWeather.getWindGustSpeed(), 0F);
		}
	}
	
	/**
	 * Test windGustSpeed getter and setter with all possibles values.
	 * Test will pass only if the windGustSpeed returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetWindGustSpeedTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setWindGustSpeed, () -> currentWeather.getWindGustSpeed(), 0F);
	}
	
	/**
	 * Test isWindDirectionReadable getter and setter method with all possibles values.
	 * Test will pass only if the windDirectionReadable returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetIsWindDirectionReadableTest() throws Exception {
		boolean[] testValues = {true, false};
		
		testAssertEqualsBooleanValues(testValues, currentWeather::setWindDirectionReadable, () -> currentWeather.isWindDirectionReadable());
	}
	
	/**
	 * Test the windDirection setter with all possibles out of bounds values.
	 * Test will pass only if the windDirection value is the same as the previous value.
	 */
	@Test
	public void setWindDirectionOutBoundsTest() {
		short[] testValues = {Short.MIN_VALUE, -1, 361, Short.MAX_VALUE};
		short expected = currentWeather.getWindDirection();
		
		for (short testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setWindDirection(testValue));
			assertEquals(expected, currentWeather.getWindDirection());
		}
	}
	
	/**
	 * Test windDirection getter and setter with all possibles values.
	 * Test will pass only if the windDirection returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetWindDirectionTest() throws Exception {
		short[] testValues = {0, 1, 180, 359, 360};
		
		testAssertEqualsShortValues(testValues, currentWeather::setWindDirection, () -> currentWeather.getWindDirection());
	}
	
	/**
	 * Test the rain setter with all possibles out of bounds values.
	 * Test will pass only if the rain value is the same as the previous value.
	 */
	@Test
	public void setRainOutBoundsTest() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = currentWeather.getRain();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setRain(testValue));
			assertEquals(expected, currentWeather.getRain(), 0F);
		}
	}
	
	/**
	 * Test rain getter and setter with all possibles values.
	 * Test will pass only if the rain returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetRainTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setRain, () -> currentWeather.getRain(), 0F);
	}
	
	/**
	 * Test thereIsRain method with all possibles values.
	 * Test will pass only if the thereIsRain returned value is the same as the original value (the one that have been set).
	 */
	@Test
	public void thereIsRainTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		boolean[] expectedResults = {false, true, true};
		
		if (testValues.length != expectedResults.length)
			throw new Exception("testValues and expectedResults needs to have the same sizes");
		
		for (int i = 0; i < testValues.length; i++) {
			currentWeather.setRain(testValues[i]);
			assertEquals(expectedResults[i], currentWeather.thereIsRain());
		}
	}
	
	/**
	 * Test the snow setter with all possibles out of bounds values.
	 * Test will pass only if the snow value is the same as the previous value.
	 */
	@Test
	public void setSnowOutBoundsTest() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = currentWeather.getSnow();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> currentWeather.setSnow(testValue));
			assertEquals(expected, currentWeather.getSnow(), 0F);
		}
	}
	
	/**
	 * Test snow getter and setter with all possibles values.
	 * Test will pass only if the snow returned value is the same as the original value (the one that have been set).
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetSnowTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, currentWeather::setSnow, () -> currentWeather.getSnow(), 0F);
	}
	
	/**
	 * Test thereIsSnow method with all possibles values.
	 * Test will pass only if the thereIsSnow returned value is the same as the original value (the one that have been set).
	 */
	@Test
	public void thereIsSnowTest() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		boolean[] expectedResults = {false, true, true};
		
		if (testValues.length != expectedResults.length)
			throw new Exception("testValues and expectedResults needs to have the same sizes");
		
		for (int i = 0; i < testValues.length; i++) {
			currentWeather.setSnow(testValues[i]);
			assertEquals(expectedResults[i], currentWeather.thereIsSnow());
		}
	}
}