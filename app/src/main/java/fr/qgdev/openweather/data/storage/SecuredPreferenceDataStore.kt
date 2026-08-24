/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import androidx.preference.PreferenceDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Opens [open], and if that fails once, calls [discardCorruptedState] and tries a single further
 * time.
 *
 * Extracted from [SecuredPreferenceDataStore] so the recovery policy can be tested without a
 * Context or the Android keystore.
 *
 * Deliberately only one retry: if opening still fails after the stored state has been discarded,
 * the problem is not corruption and retrying again would loop. In that case the second failure is
 * thrown with the first attached as a suppressed exception, so both causes survive - the previous
 * implementation threw a bare `RuntimeException("Unable to initialize a secure environment")` and
 * discarded the real one, which made the failure impossible to diagnose from a crash report.
 */
internal fun <T> openWithOneRecoveryAttempt(
    open: () -> T,
    discardCorruptedState: () -> Unit
): T =
    try {
        open()
    } catch (firstAttempt: Exception) {
        discardCorruptedState()
        try {
            open()
        } catch (secondAttempt: Exception) {
            throw IllegalStateException(
                "Unable to open the encrypted preferences, even after discarding them",
                secondAttempt.apply { addSuppressed(firstAttempt) }
            )
        }
    }

/**
 * SecuredPreferenceDataStore
 *
 * A class to store data in a secure way.
 * It uses the AndroidX Security library to encrypt the data.
 * It's used to store the settings of the application.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see PreferenceDataStore
 */
class SecuredPreferenceDataStore(context: Context, filename: String) : PreferenceDataStore() {
    private val sharedPreferences: SharedPreferences

    init {
        //  Opening these preferences can fail when the master key is no longer usable - after a
        //  device-to-device restore, or on OEM keystore implementations that drop keys. The stored
        //  file then cannot be decrypted, and every later launch fails the same way, so the app is
        //  permanently broken until its data is cleared by hand.
        //
        //  Discarding the file and recreating it recovers from that: the user loses their saved
        //  settings and has to re-enter the API key, which is a far better outcome than an app
        //  that cannot start.
        sharedPreferences = openWithOneRecoveryAttempt(
            open = {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    filename,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            },
            discardCorruptedState = { context.deleteSharedPreferences(filename) }
        )
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Sets a [String] value to the data store.
     *
     * Once the value is set the data store is responsible for holding it.
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     * @see getString
     */
    override fun putString(key: String, @Nullable value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Sets an [Int] value to the data store.
     *
     * Once the value is set the data store is responsible for holding it.
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     * @see getInt
     */
    override fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    /**
     * Sets a [Long] value to the data store.
     *
     * Once the value is set the data store is responsible for holding it.
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     * @see getLong
     */
    override fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    /**
     * Sets a [Float] value to the data store.
     *
     * Once the value is set the data store is responsible for holding it.
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     * @see getFloat
     */
    override fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    /**
     * Sets a [Boolean] value to the data store.
     *
     * Once the value is set the data store is responsible for holding it.
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     * @see getBoolean
     */
    override fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Retrieves a [String] value from the data store.
     *
     * @param key      The name of the preference to retrieve
     * @param defValue Value to return if this preference does not exist in the storage
     * @return The value from the data store or the default return value
     * @see putString
     */
    @Nullable
    override fun getString(key: String, @Nullable defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    /**
     * Retrieves an [Int] value from the data store.
     *
     * @param key      The name of the preference to retrieve
     * @param defValue Value to return if this preference does not exist in the storage
     * @return The value from the data store or the default return value
     * @see putInt
     */
    override fun getInt(key: String, defValue: Int): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    /**
     * Retrieves a [Long] value from the data store.
     *
     * @param key      The name of the preference to retrieve
     * @param defValue Value to return if this preference does not exist in the storage
     * @return The value from the data store or the default return value
     * @see putLong
     */
    override fun getLong(key: String, defValue: Long): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    /**
     * Retrieves a [Float] value from the data store.
     *
     * @param key      The name of the preference to retrieve
     * @param defValue Value to return if this preference does not exist in the storage
     * @return The value from the data store or the default return value
     * @see putFloat
     */
    override fun getFloat(key: String, defValue: Float): Float {
        return sharedPreferences.getFloat(key, defValue)
    }

    /**
     * Retrieves a [Boolean] value from the data store.
     *
     * @param key      The name of the preference to retrieve
     * @param defValue Value to return if this preference does not exist in the storage
     * @return the value from the data store or the default return value
     * @see getBoolean
     */
    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }

    /**
     * Removes a preference from the data store.
     *
     * @param key The name of the preference to remove
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    /**
     * Checks whether the data store contains a preference.
     *
     * @param key The name of the preference to check
     * @return `true` if the preference exists in the data store, `false` otherwise
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
