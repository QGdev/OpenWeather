
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

package fr.qgdev.openweather.utils;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

/**
 * Test utils.
 * <p>
 * Used to hold custom methods used in tests
 */
public class TestUtils {
	
	/**
	 * Used to test a group of doubles values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 * @param delta      the delta used to know if the values are equals
	 */
	public static void testAssertEqualsDoubleValues(double[] testValues, ParameterizedRunnable<Double> setter, Callable<Double> getter, double delta) throws Exception {
		for (double testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, getter.call(), delta);
		}
	}
	
	/**
	 * Used to test a group of float values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 * @param delta      the delta used to know if the values are equals
	 */
	public static void testAssertEqualsFloatValues(float[] testValues, ParameterizedRunnable<Float> setter, Callable<Float> getter, float delta) throws Exception {
		for (float testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, getter.call(), delta);
		}
	}
	
	/**
	 * Used to test a group of int values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 */
	public static void testAssertEqualsIntValues(int[] testValues, ParameterizedRunnable<Integer> setter, Callable<Integer> getter) throws Exception {
		for (int testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, (long) getter.call());
		}
	}
	
	/**
	 * Used to test a group of String values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 */
	public static void testAssertEqualsStringValues(String[] testValues, ParameterizedRunnable<String> setter, Callable<String> getter) throws Exception {
		for (String testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, getter.call());
		}
	}
	
	public static void testAssertEqualsLongValues(long[] testValues, ParameterizedRunnable<Long> setter, Callable<Long> getter) throws Exception {
		for (long testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, (long) getter.call());
		}
	}
	
	/**
	 * Used to test a group of boolean values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 */
	public static void testAssertEqualsBooleanValues(boolean[] testValues, ParameterizedRunnable<Boolean> setter, Callable<Boolean> getter) throws Exception {
		for (boolean testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, getter.call());
		}
	}
	
	/**
	 * Used to test a group of short values for a given setter and getter
	 *
	 * @param testValues the list of test values
	 * @param setter     the setter
	 * @param getter     the getter
	 */
	public static void testAssertEqualsShortValues(short[] testValues, ParameterizedRunnable<Short> setter, Callable<Short> getter) throws Exception {
		for (short testValue : testValues) {
			setter.run(testValue);
			assertEquals(testValue, (short) getter.call());
		}
	}
}
