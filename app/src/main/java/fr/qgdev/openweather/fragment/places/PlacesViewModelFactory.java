package fr.qgdev.openweather.fragment.places;

import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;


public class PlacesViewModelFactory implements ViewModelProvider.Factory {
    
    private static final String TAG = PlacesViewModelFactory.class.getSimpleName();
    private static final Logger logger = Logger.getLogger(TAG);
    
    private static volatile PlacesViewModelFactory factory;
    
    private PlacesViewModelFactory() {
    }
    
    public static PlacesViewModelFactory getInstance() {
        synchronized (PlacesViewModelFactory.class) {
            if (factory == null) {
                factory = new PlacesViewModelFactory();
            }
        }
        return factory;
    }
    
    @NotNull
    public PlacesViewModel create() {
        return new PlacesViewModel();
    }
}
