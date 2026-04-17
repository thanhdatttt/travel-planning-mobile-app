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

    @Query("SELECT * FROM itineraries LIMIT :limit")
    List<Itinerary> getCachedItineraries(int limit);

    @Query("DELETE FROM itineraries WHERE id = :id")
    void deleteItinerary(String id);

    @Query("SELECT * FROM itineraries WHERE privacy = 'public' LIMIT :limit")
    List<Itinerary> getCachedPublicItineraries(int limit);
}