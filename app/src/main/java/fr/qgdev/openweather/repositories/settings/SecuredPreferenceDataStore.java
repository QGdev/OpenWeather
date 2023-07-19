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

package fr.qgdev.openweather.repositories.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.util.Set;


public class SecuredPreferenceDataStore extends PreferenceDataStore {
	private static final String SP_FILENAME = "fr.qgdev.openweather_preferences";
	private final SharedPreferences sharedPreferences;
	
	public SecuredPreferenceDataStore(Context context) {
		try {
			String masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
			sharedPreferences = EncryptedSharedPreferences
					  .create(SP_FILENAME,
								 masterKey,
								 context,
								 EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
								 EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize a secure environment");
		}
	}
	
	public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	
	/**
	 * Sets a {@link String} value to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 * @see #getString(String, String)
	 */
	@Override
	public void putString(String key, @Nullable @org.jetbrains.annotations.Nullable String value) {
		sharedPreferences.edit().putString(key, value).apply();
	}
	
	/**
	 * Sets a set of {@link String}s to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key    The name of the preference to modify
	 * @param values The set of new values for the preference
	 * @see #getStringSet(String, Set)
	 */
	@Override
	public void putStringSet(String key, @Nullable @org.jetbrains.annotations.Nullable Set<String> values) {
		sharedPreferences.edit().putStringSet(key, values).apply();
	}
	
	/**
	 * Sets an {@link Integer} value to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 * @see #getInt(String, int)
	 */
	@Override
	public void putInt(String key, int value) {
		sharedPreferences.edit().putInt(key, value).apply();
	}
	
	/**
	 * Sets a {@link Long} value to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 * @see #getLong(String, long)
	 */
	@Override
	public void putLong(String key, long value) {
		sharedPreferences.edit().putLong(key, value).apply();
	}
	
	/**
	 * Sets a {@link Float} value to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 * @see #getFloat(String, float)
	 */
	@Override
	public void putFloat(String key, float value) {
		sharedPreferences.edit().putFloat(key, value).apply();
	}
	
	/**
	 * Sets a {@link Boolean} value to the data store.
	 *
	 * <p>Once the value is set the data store is responsible for holding it.
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 * @see #getBoolean(String, boolean)
	 */
	@Override
	public void putBoolean(String key, boolean value) {
		sharedPreferences.edit().putBoolean(key, value).apply();
	}
	
	/**
	 * Retrieves a {@link String} value from the data store.
	 *
	 * @param key      The name of the preference to retrieve
	 * @param defValue Value to return if this preference does not exist in the storage
	 * @return The value from the data store or the default return value
	 * @see #putString(String, String)
	 */
	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public String getString(String key, @Nullable @org.jetbrains.annotations.Nullable String defValue) {
		return sharedPreferences.getString(key, defValue);
	}
	
	/**
	 * Retrieves a set of Strings from the data store.
	 *
	 * @param key       The name of the preference to retrieve
	 * @param defValues Values to return if this preference does not exist in the storage
	 * @return The values from the data store or the default return values
	 * @see #putStringSet(String, Set)
	 */
	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public Set<String> getStringSet(String key, @Nullable @org.jetbrains.annotations.Nullable Set<String> defValues) {
		return sharedPreferences.getStringSet(key, defValues);
	}
	
	/**
	 * Retrieves an {@link Integer} value from the data store.
	 *
	 * @param key      The name of the preference to retrieve
	 * @param defValue Value to return if this preference does not exist in the storage
	 * @return The value from the data store or the default return value
	 * @see #putInt(String, int)
	 */
	@Override
	public int getInt(String key, int defValue) {
		return sharedPreferences.getInt(key, defValue);
	}
	
	/**
	 * Retrieves a {@link Long} value from the data store.
	 *
	 * @param key      The name of the preference to retrieve
	 * @param defValue Value to return if this preference does not exist in the storage
	 * @return The value from the data store or the default return value
	 * @see #putLong(String, long)
	 */
	@Override
	public long getLong(String key, long defValue) {
		return sharedPreferences.getLong(key, defValue);
	}
	
	/**
	 * Retrieves a {@link Float} value from the data store.
	 *
	 * @param key      The name of the preference to retrieve
	 * @param defValue Value to return if this preference does not exist in the storage
	 * @return The value from the data store or the default return value
	 * @see #putFloat(String, float)
	 */
	@Override
	public float getFloat(String key, float defValue) {
		return sharedPreferences.getFloat(key, defValue);
	}
	
	/**
	 * Retrieves a {@link Boolean} value from the data store.
	 *
	 * @param key      The name of the preference to retrieve
	 * @param defValue Value to return if this preference does not exist in the storage
	 * @return the value from the data store or the default return value
	 * @see #getBoolean(String, boolean)
	 */
	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return sharedPreferences.getBoolean(key, defValue);
	}
	
	/**
	 * Checks whether the data store contains a preference.
	 *
	 * @param key The name of the preference to check
	 */
	public void remove(String key) {
		sharedPreferences.edit().remove(key).apply();
	}
	
	/**
	 * Checks whether the data store contains a preference.
	 *
	 * @param key The name of the preference to check
	 * @return {@code true} if the preference exists in the data store, {@code false} otherwise
	 */
	public boolean contains(String key) {
		return sharedPreferences.contains(key);
	}
}
