package com.example.travelplanning.data.local.itinerary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import java.util.List;

@Dao
public interface ItineraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItineraries(List<Itinerary> itineraries);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItinerary(Itinerary itinerary);

    @Query("SELECT * FROM itineraries WHERE id = :id")
    Itinerary getItineraryById(String id);

    @Query("SELECT * FROM itineraries WHERE ownerId = :userId ORDER BY createdAt DESC LIMIT :limit")
    List<Itinerary> getMyCachedItineraries(String userId, int limit);

    @Query("DELETE FROM itineraries WHERE id = :id")
    void deleteItinerary(String id);

    @Query("SELECT * FROM itineraries WHERE privacy = 'public' LIMIT :limit")
    List<Itinerary> getCachedPublicItineraries(int limit);

    @Query("DELETE FROM itineraries WHERE ownerId = :userId")
    void clearMyItineraries(String userId);
}