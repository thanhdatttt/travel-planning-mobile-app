package com.example.travelplanning.data.local.bookmark;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.travelplanning.data.model.bookmark.Bookmark;
import com.example.travelplanning.data.model.location.Location;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmark(Bookmark bookmark);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBookmarks(List<Bookmark> bookmarks);

    @Query("DELETE FROM bookmarks WHERE locationId = :locationId AND userId = :userId")
    void deleteBookmark(String locationId, String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE locationId = :locationId AND userId = :userId)")
    boolean isBookmarked(String locationId, String userId);

    @Query("SELECT locations.* FROM locations INNER JOIN bookmarks ON locations.id = bookmarks.locationId WHERE bookmarks.userId = :userId")
    List<Location> getBookmarkedLocations(String userId);
}