package fr.qgdev.openweather.repositories.places.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public abstract class PlaceDAO {
	
	@Query("SELECT COUNT(*) FROM properties")
	public abstract LiveData<Integer> getPlacesCountLiveData();
	
	@Query("SELECT COUNT(*) FROM properties")
	public abstract Integer getPlacesCount();
}
