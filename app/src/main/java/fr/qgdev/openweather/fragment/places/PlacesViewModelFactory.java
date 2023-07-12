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

package fr.qgdev.openweather.fragment.places;

import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;


/**
 * The type Places view model factory.
 */
public class PlacesViewModelFactory implements ViewModelProvider.Factory {
    
    private static final AtomicReference<PlacesViewModelFactory> factory = new AtomicReference<>(null);
    
    private PlacesViewModelFactory() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static PlacesViewModelFactory getInstance() {
        factory.compareAndSet(null, new PlacesViewModelFactory());
        return factory.get();
    }
    
    /**
     * Create places view model.
     *
     * @return the places view model
     */
    @NotNull
    public PlacesViewModel create() {
        return new PlacesViewModel();
    }
}
