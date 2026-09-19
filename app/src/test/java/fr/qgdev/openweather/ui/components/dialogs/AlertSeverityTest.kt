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

package fr.qgdev.openweather.ui.components.dialogs

import org.junit.Assert.assertEquals
import org.junit.Test

class AlertSeverityTest {

    /**
     * Test the colour given by traffic light names.
     * Test will not pass if one of the names doesn't give its colour.
     */
    @Test
    fun `traffic-light names give the colour`() {
        assertEquals(AlertSeverity.ORANGE, severityOf("Orange Thunderstorm Warning"))
        assertEquals(AlertSeverity.YELLOW, severityOf("Vigilance jaune orages"))
        assertEquals(AlertSeverity.RED, severityOf("Allerta rossa"))
    }

    /**
     * Test the colour given by the CAP severity scale.
     * Test will not pass if one of the severities doesn't give its colour.
     */
    @Test
    fun `the CAP severity scale gives the colour too`() {
        assertEquals(AlertSeverity.YELLOW, severityOf("Moderate thunderstorm warning"))
        assertEquals(AlertSeverity.ORANGE, severityOf("Severe wind warning"))
        assertEquals(AlertSeverity.RED, severityOf("Extreme flood warning"))
    }

    /**
     * Test a wording holding no known word.
     * Test will not pass if it gets a colour.
     */
    @Test
    fun `wording without a known word stays uncoloured`() {
        assertEquals(AlertSeverity.UNKNOWN, severityOf("Minor coastal event"))
        assertEquals(AlertSeverity.UNKNOWN, severityOf("Thunderstorm warning"))
    }
}
