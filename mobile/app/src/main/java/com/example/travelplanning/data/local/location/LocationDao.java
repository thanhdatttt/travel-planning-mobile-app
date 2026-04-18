package com.example.travelplanning.data.local.location;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.travelplanning.data.model.location.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocations(List<Location> locations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(Location location);

    @Query("SELECT * FROM locations WHERE id = :locationId")
    Location getLocationById(String locationId);

    @Query("SELECT * FROM locations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' LIMIT :limit")
    List<Location> searchOffline(String query, int limit);
}