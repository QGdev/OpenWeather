
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
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsFloatValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsIntValues;
import static fr.qgdev.openweather.utils.TestUtils.testAssertEqualsStringValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class AirQualityTest {
	
	private AirQuality airQuality;
	
	
	/**
	 * Set up the test.
	 */
	@Before
	public void setUp() {
		airQuality = new AirQuality();
	}
	
	/**
	 * Test the basic constructor.
	 * Test will not pass if at least one of the values isn't valid.
	 */
	@Test
	public void basicConstructor() {
		AirQuality airQuality = new AirQuality();
		
		String placeId = airQuality.getPlaceId();
		int aqi = airQuality.getAqi();
		float co = airQuality.getCo();
		float no = airQuality.getNo();
		float no2 = airQuality.getNo2();
		float o3 = airQuality.getO3();
		float so2 = airQuality.getSo2();
		float pm2_5 = airQuality.getPm2_5();
		float pm10 = airQuality.getPm10();
		float nh3 = airQuality.getNh3();
		
		assertEquals("", placeId);
		assertTrue(0 < aqi && aqi < 6);
		assertTrue(0 <= co);
		assertTrue(0 <= no);
		assertTrue(0 <= no2);
		assertTrue(0 <= o3);
		assertTrue(0 <= so2);
		assertTrue(0 <= pm2_5);
		assertTrue(0 <= pm10);
		assertTrue(0 <= nh3);
	}
	
	/**
	 * constructAirQualityJSON(float lon, float lat, int aqi, float co, float no,
	 * float no2, float o3, float so2, float pm2_5,
	 * float pm10, float nh3) throws JSONException
	 * <p>
	 * Will build a JSON object with the given parameters.
	 * Used to lighten the test code.
	 */
	private JSONObject constructAirQualityJSON(float lon, float lat, int aqi, float co, float no,
															 float no2, float o3, float so2, float pm2_5,
															 float pm10, float nh3) throws JSONException {
		JSONObject jsonCoord = new JSONObject();
		jsonCoord.put("lon", lon);
		jsonCoord.put("lat", lat);
		
		JSONObject jsonComponents = new JSONObject();
		jsonComponents.put("co", co);
		jsonComponents.put("no", no);
		jsonComponents.put("no2", no2);
		jsonComponents.put("o3", o3);
		jsonComponents.put("so2", so2);
		jsonComponents.put("pm2_5", pm2_5);
		jsonComponents.put("pm10", pm10);
		jsonComponents.put("nh3", nh3);
		
		JSONObject jsonAqi = new JSONObject();
		jsonAqi.put("aqi", aqi);
		
		JSONObject jsonMain = new JSONObject();
		jsonMain.put("main", jsonAqi);
		jsonMain.put("components", jsonComponents);
		
		JSONArray jsonList = new JSONArray();
		jsonList.put(jsonMain);
		
		JSONObject json = new JSONObject();
		json.put("coord", jsonCoord);
		json.put("list", jsonList);
		json.put("aqi", aqi);
		
		return json;
	}
	
	/**
	 * Test the JSON constructor.
	 * Test will pass only if all values are valid and
	 * if input values are the same as attributes values.
	 */
	@Test
	public void JSONConstructor() throws JSONException {
		
		int[] aqiTestValues = {1, 2, 3, 4, 5};
		float[] componentsTestValues = {0.0f, 1.0f, Float.MAX_VALUE};
		
		// Test all possible values
		// I know, it's ugly, but it's the only way to test all possible values
		for (int aqi : aqiTestValues) {
			for (float co : componentsTestValues) {
				for (float no : componentsTestValues) {
					for (float no2 : componentsTestValues) {
						for (float o3 : componentsTestValues) {
							for (float so2 : componentsTestValues) {
								for (float pm2_5 : componentsTestValues) {
									for (float pm10 : componentsTestValues) {
										for (float nh3 : componentsTestValues) {
											JSONObject json = constructAirQualityJSON(0.0f, 0.0f, aqi, co,
													  no, no2, o3, so2,
													  pm2_5, pm10, nh3);
											
											AirQuality airQuality = new AirQuality(json);
											
											assertNull(airQuality.getPlaceId());
											assertEquals(aqi, airQuality.getAqi());
											assertEquals(co, airQuality.getCo(), 0.0f);
											assertEquals(no, airQuality.getNo(), 0.0f);
											assertEquals(no2, airQuality.getNo2(), 0.0f);
											assertEquals(o3, airQuality.getO3(), 0.0f);
											assertEquals(so2, airQuality.getSo2(), 0.0f);
											assertEquals(pm2_5, airQuality.getPm2_5(), 0.0f);
											assertEquals(pm10, airQuality.getPm10(), 0.0f);
											assertEquals(nh3, airQuality.getNh3(), 0.0f);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Test the JSON constructor with out of bounds values.
	 * Test will pass only if the constructor throws an IllegalArgumentException
	 * each time it is called with an out of bounds value.
	 */
	@Test
	public void JSONConstructorOutBounds() throws JSONException {
		int[] aqiTestValues = {Integer.MIN_VALUE, 0, 6, Integer.MAX_VALUE};
		int aqiNormalValue = 1;
		
		float[] componentsTestValues = {-Float.MIN_VALUE, -1.0f};
		float componentsNormalValue = 0.0f;
		
		// Test all possible values
		//	I know and I'm sorry...
		// All of this is needed to test all possible values and fail cases
		
		// Test aqi
		for (int aqi : aqiTestValues) {
			JSONObject json = constructAirQualityJSON(0.0f, 0.0f, aqi,
					  componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue);
			
			assertThrows(IllegalArgumentException.class, () -> new AirQuality(json));
		}
		
		// Test each components
		for (int i = 0; i < 8; i++) {
			float[] componentsTestValue = {componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue,
					  componentsNormalValue, componentsNormalValue};
			for (float value : componentsTestValues) {
				componentsTestValue[i] = value;
				JSONObject json = constructAirQualityJSON(0.0f, 0.0f, aqiNormalValue,
						  componentsTestValue[0], componentsTestValue[1],
						  componentsTestValue[2], componentsTestValue[3],
						  componentsTestValue[4], componentsTestValue[5],
						  componentsTestValue[6], componentsTestValue[7]);
				
				assertThrows(IllegalArgumentException.class, () -> new AirQuality(json));
			}
		}
	}
	
	/**
	 * Test the placeId getter with all possibles values.
	 * Test will pass only if the placeId value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetPlaceId() throws Exception {
		String[] testValues = {"", "TeSt", "test", "TEST", "Test", "tEsT"};
		
		testAssertEqualsStringValues(testValues, id -> airQuality.setPlaceId(id), airQuality::getPlaceId);
	}
	
	/**
	 * Test the aqi getter and setter with all possibles values.
	 * Test will pass only if the aqi value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetAqi() throws Exception {
		int[] testValues = {1, 2, 3, 4, 5};
		
		testAssertEqualsIntValues(testValues, aqi -> airQuality.setAqi(aqi), airQuality::getAqi);
	}
	
	/**
	 * Test the aqi setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the aqi value is the same as before function call.
	 */
	@Test
	public void setAqiOutBounds() {
		int[] testValues = {Integer.MIN_VALUE, -1, 0, 6, Integer.MAX_VALUE};
		int expected = airQuality.getAqi();
		
		for (int testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setAqi(testValue));
			assertEquals(expected, airQuality.getAqi());
		}
	}
	
	/**
	 * Test the Co getter and setter with all possibles values.
	 * Test will pass only if the Co value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetCo() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, co -> airQuality.setCo(co), airQuality::getCo, 0.0f);
	}
	
	/**
	 * Test the Co setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the Co value is the same as before function call.
	 */
	@Test
	public void setCoOutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getCo();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setCo(testValue));
			assertEquals(expected, airQuality.getCo(), 0.0f);
		}
	}
	
	/**
	 * Test the No getter and setter with all possibles values.
	 * Test will pass only if the No value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetNo() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, no -> airQuality.setNo(no), airQuality::getNo, 0.0f);
	}
	
	/**
	 * Test the No setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the No value is the same as before function call.
	 */
	@Test
	public void setNoOutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getNo();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setNo(testValue));
			assertEquals(expected, airQuality.getNo(), 0.0f);
		}
	}
	
	/**
	 * Test the No2 getter and setter with all possibles values.
	 * Test will pass only if the No2 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetNo2() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, no2 -> airQuality.setNo2(no2), airQuality::getNo2, 0.0f);
	}
	
	/**
	 * Test the No2 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the No2 value is the same as before function call.
	 */
	@Test
	public void setNo2OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getNo2();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setNo2(testValue));
			assertEquals(expected, airQuality.getNo2(), 0.0f);
		}
	}
	
	/**
	 * Test the O3 getter and setter with all possibles values.
	 * Test will pass only if the O3 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetO3() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, o3 -> airQuality.setO3(o3), airQuality::getO3, 0.0f);
	}
	
	/**
	 * Test the O3 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the O3 value is the same as before function call.
	 */
	@Test
	public void setO3OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getO3();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setO3(testValue));
			assertEquals(expected, airQuality.getO3(), 0.0f);
		}
	}
	
	/**
	 * Test the So2 getter and setter with all possibles values.
	 * Test will pass only if the So2 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetSo2() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, so2 -> airQuality.setSo2(so2), airQuality::getSo2, 0.0f);
	}
	
	/**
	 * Test the So2 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the So2 value is the same as before function call.
	 */
	@Test
	public void setSo2OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getSo2();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setSo2(testValue));
			assertEquals(expected, airQuality.getSo2(), 0.0f);
		}
	}
	
	/**
	 * Test the Pm2.5 getter and setter with all possibles values.
	 * Test will pass only if the Pm2.5 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetPm2_5() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, pm2_5 -> airQuality.setPm2_5(pm2_5), airQuality::getPm2_5, 0.0f);
	}
	
	/**
	 * Test the Pm2.5 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the Pm2.5 value is the same as before function call.
	 */
	@Test
	public void setPm2_5OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getPm2_5();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setPm2_5(testValue));
			assertEquals(expected, airQuality.getPm2_5(), 0.0f);
		}
	}
	
	/**
	 * Test the Pm10 getter and setter with all possibles values.
	 * Test will pass only if the Pm10 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetPm10() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, pm10 -> airQuality.setPm10(pm10), airQuality::getPm10, 0.0f);
	}
	
	/**
	 * Test the Pm10 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the Pm10 value is the same as before function call.
	 */
	@Test
	public void setPm10OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getPm10();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setPm10(testValue));
			assertEquals(expected, airQuality.getPm10(), 0.0f);
		}
	}
	
	/**
	 * Test the Nh3 getter and setter with all possibles values.
	 * Test will pass only if the Nh3 value is the same as the returned value
	 *
	 * @throws Exception Throws an execution if the test is misconfigured
	 */
	@Test
	public void getAndSetNh3() throws Exception {
		float[] testValues = {0, 1, Float.MAX_VALUE};
		
		testAssertEqualsFloatValues(testValues, nh3 -> airQuality.setNh3(nh3), airQuality::getNh3, 0.0f);
	}
	
	/**
	 * Test the Nh3 setter with out of bounds values.
	 * Test will pass only if for each values an exception is thrown
	 * and if the Nh3 value is the same as before function call.
	 */
	@Test
	public void setNh3OutBounds() {
		float[] testValues = {-Float.MIN_VALUE, -1};
		float expected = airQuality.getNh3();
		
		for (float testValue : testValues) {
			assertThrows(IllegalArgumentException.class, () -> airQuality.setNh3(testValue));
			assertEquals(expected, airQuality.getNh3(), 0.0f);
		}
	}
}