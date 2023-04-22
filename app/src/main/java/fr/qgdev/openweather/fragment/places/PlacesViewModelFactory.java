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
