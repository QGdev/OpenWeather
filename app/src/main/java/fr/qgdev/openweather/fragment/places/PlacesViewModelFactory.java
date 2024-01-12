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

package fr.qgdev.openweather.fragment.places;

import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.atomic.AtomicReference;


/**
 * PlacesViewModelFactory
 * <p>
 *    Factory for the PlacesViewModel
 *    It's a singleton
 *    It's used to get the same instance of the PlacesViewModel
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see androidx.lifecycle.ViewModelProvider.Factory
 */
public class PlacesViewModelFactory implements ViewModelProvider.Factory {
    
    private static final AtomicReference<PlacesViewModel> factory = new AtomicReference<>(null);
    
    private PlacesViewModelFactory() {
        super();
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static PlacesViewModel getInstance() {
        if (factory.get() == null) {
            synchronized (PlacesViewModelFactory.class) {
                factory.compareAndSet(null, new PlacesViewModel());
            }
        }
        return factory.get();
    }
}
