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

package fr.qgdev.openweather.repositories.places.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import fr.qgdev.openweather.repositories.places.Properties;

/**
 * PropertiesDAO
 * <p>
 *    DAO for the Properties class.
 *    It's used to access the database.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Dao
public interface PropertiesDAO {
	
	@Transaction
	@Query("SELECT placeId FROM properties ORDER BY `order` ASC")
	List<String> getAllIDs();
	
	@Transaction
	@Query("SELECT * FROM properties ORDER BY `order` ASC")
	List<Properties> getProperties();
	
	@Query("SELECT placeId FROM properties WHERE `order` = :order")
	String getIDFromPlaceOrder(int order);
	
	@Query("SELECT * FROM properties WHERE placeId = :placeID")
	Properties getFromPlaceId(String placeID);
	
	@Query("UPDATE properties SET `order` = :newOrder WHERE placeId = :placeID")
	void updateOrderFromPlaceID(String placeID, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` - 1 WHERE `order` >= :crtOrder AND `order` <= :newOrder")
	void updatePlaceOrdersMvEnd(int crtOrder, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` + 1 WHERE `order` >= :newOrder AND `order` <= :crtOrder")
	void updatePlaceOrdersMvBgn(int crtOrder, int newOrder);
	
	@Query("UPDATE properties SET `order` = `order` - 1 WHERE `order` > :order")
	void updatePlaceOrdersAfterDeletion(int order);
	
	@Query("DELETE FROM properties WHERE placeId = :id")
	void deleteFromPlaceID(String id);
	
	@Insert
	void insert(Properties properties);
	
	@Delete
	void delete(Properties properties);
	
	@Update()
	void update(Properties properties);
}
