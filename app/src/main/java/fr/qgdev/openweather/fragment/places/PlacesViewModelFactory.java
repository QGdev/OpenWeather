package fr.qgdev.openweather.fragment.places;

import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;


/**
 * The type Places view model factory.
 */
public class PlacesViewModelFactory implements ViewModelProvider.Factory {
    
    private static final String TAG = PlacesViewModelFactory.class.getSimpleName();
    private static final Logger logger = Logger.getLogger(TAG);
    
    private static volatile PlacesViewModelFactory factory;
    
    private PlacesViewModelFactory() {
    }
    
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static PlacesViewModelFactory getInstance() {
        if (factory == null) {
            synchronized (PlacesViewModelFactory.class) {
                if (factory == null) {
                    factory = new PlacesViewModelFactory();
                }
            }
        }
        return factory;
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
