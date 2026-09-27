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
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.Geolocation

/**
 * LegacyPlaceDatabase
 * <p>
 * Reads the places of the Room database of 0.9.x (appDB), in order to restore them in the DataStore.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
internal object LegacyPlaceDatabase {

    private const val NAME = "appDB"

    private const val ORDERED_QUERY =
        "SELECT g.city, g.countryCode, g.latitude, g.longitude FROM geolocation g " +
                "LEFT JOIN properties p ON p.placeId = g.placeId " +
                "ORDER BY p.`order` IS NULL, p.`order`"

    private const val UNORDERED_QUERY =
        "SELECT city, countryCode, latitude, longitude FROM geolocation"

    /** Whether the 0.9.x database is still on the device. */
    fun exists(context: Context): Boolean = context.getDatabasePath(NAME).exists()

    /** Deletes the 0.9.x database. */
    fun delete(context: Context) {
        context.deleteDatabase(NAME)
    }

    /**
     * Reads the places of the 0.9.x database in their display order.
     * Opened read-write since Room left it in WAL mode.
     */
    fun readPlaces(context: Context): List<Geolocation> =
        SQLiteDatabase.openDatabase(context.getDatabasePath(NAME).path, null, SQLiteDatabase.OPEN_READWRITE)
            .use { database ->
                try {
                    database.rawQuery(ORDERED_QUERY, null).use { it.toGeolocations() }
                } catch (_: SQLiteException) {
                    database.rawQuery(UNORDERED_QUERY, null).use { it.toGeolocations() }
                }
            }

    /** Converts every row having coordinates into a Geolocation. */
    private fun Cursor.toGeolocations(): List<Geolocation> {
        val geolocations = mutableListOf<Geolocation>()
        while (moveToNext()) {
            if (isNull(2) || isNull(3)) continue

            val builder = Geolocation.newBuilder()
                .setCoordinates(Coordinates.newBuilder().setLatitude(getDouble(2)).setLongitude(getDouble(3)))
            if (!isNull(0)) builder.setCity(getString(0))
            if (!isNull(1)) builder.setCountryCode(getString(1))
            geolocations.add(builder.build())
        }
        return geolocations
    }
}
