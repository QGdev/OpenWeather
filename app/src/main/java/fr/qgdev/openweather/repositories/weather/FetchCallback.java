package fr.qgdev.openweather.repositories.weather;

public interface FetchCallback {
    void onSuccess();
    
    void onError(RequestStatus requestStatus);
}
