package com.example.travelplanning.data.local.favorite;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.travelplanning.data.model.favorite.Favorite;
import com.example.travelplanning.data.model.itinerary.Itinerary;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Favorite favorite);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorites(List<Favorite> favorites);

    @Query("DELETE FROM favorites WHERE itineraryId = :itineraryId AND userId = :userId")
    void deleteFavorite(String itineraryId, String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itineraryId = :itineraryId AND userId = :userId)")
    boolean isFavorited(String itineraryId, String userId);

    @Query("SELECT itineraries.* FROM itineraries INNER JOIN favorites ON itineraries.id = favorites.itineraryId WHERE favorites.userId = :userId")
    List<Itinerary> getFavoritedItineraries(String userId);
}