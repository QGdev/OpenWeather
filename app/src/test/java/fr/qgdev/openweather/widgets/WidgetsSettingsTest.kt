/*
 *  Copyright (c) 2019 - 2026
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

package fr.qgdev.openweather.widgets

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetsSettingsTest {

    /**
     * Test the settings written then read back.
     * Test will not pass if a field is lost or renamed in the JSON.
     */
    @Test
    fun `settings survive a round trip through their JSON`() {
        val settings = WidgetsSettings("place-key", 42, 60, false)
        assertEquals(settings, WidgetsSettings.fromJson(JSONObject(settings.toJsonString())))
    }

    /**
     * Test the settings saved before the transparency and details options existed.
     * Test will not pass if they don't read as opaque and showing details.
     */
    @Test
    fun `older settings read as opaque and showing details`() {
        val settings = WidgetsSettings.fromJson(JSONObject("""{"placeId":"place-key","widgetId":42}"""))
        assertEquals(WidgetsSettings("place-key", 42, 0, true), settings)
    }

    /**
     * Test a transparency out of its range.
     * Test will not pass if it is accepted.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `a transparency above 100 is refused`() {
        WidgetsSettings("place-key", 42, 101, true)
    }
}
