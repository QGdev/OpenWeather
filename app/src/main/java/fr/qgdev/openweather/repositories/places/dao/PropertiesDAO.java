package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.repositories.places.Properties;

@Dao
public interface PropertiesDAO {
	
	@Transaction
	@Query("SELECT placeId FROM properties ORDER BY `order` ASC")
	List<Integer> getAllIDs();
	
	@Transaction
	@Query("SELECT * FROM properties ORDER BY `order` ASC")
	List<Properties> getProperties();
	
	@Query("SELECT placeId FROM properties WHERE `order` = :order")
	int getIDFromPlaceOrder(int order);
	
	@Query("SELECT * FROM properties WHERE placeId = :placeID")
	Properties getFromId(int placeID);
	
	@Query("UPDATE properties SET `order` = :newOrder WHERE placeId = :placeID")
	void updateOrderFromPlaceID(int placeID, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` - 1 WHERE `order` >= :crtOrder AND `order` <= :newOrder")
	void updatePlaceOrdersMvBgn(int crtOrder, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` + 1 WHERE `order` >= :newOrder AND `order` <= :crtOrder")
	void updatePlaceOrdersMvEnd(int crtOrder, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` - 1 WHERE `order` > :order")
	void updatePlaceOrdersAfterDeletion(int order);
	
	@Query("DELETE FROM properties WHERE placeId = :id")
	void deleteFromPlaceID(int id);
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	void insert(Properties properties);
	
	@Delete
	void delete(Properties properties);
	
	@Update()
	void update(Properties properties);
}
