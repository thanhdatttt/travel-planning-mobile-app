package com.example.travelplanning.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.bookmark.Bookmark;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.model.review.UserReview;
import com.example.travelplanning.data.model.favorite.Favorite;

import com.example.travelplanning.data.local.favorite.FavoriteDao;
import com.example.travelplanning.data.local.review.ReviewDao;
import com.example.travelplanning.data.local.bookmark.BookmarkDao;
import com.example.travelplanning.data.local.profile.UserProfileDao;
import com.example.travelplanning.data.local.profile.UserProfileConverters;
import com.example.travelplanning.data.local.category.CategoryDao;
import com.example.travelplanning.data.local.location.LocationDao;
import com.example.travelplanning.data.local.location.LocationConverters;
import com.example.travelplanning.data.local.itinerary.ItineraryDao;
import com.example.travelplanning.data.local.itinerary.ItineraryConverters;

@Database(entities = {Location.class, Itinerary.class, Category.class, UserProfile.class, Bookmark.class, Review.class, RatingStat.class, UserReview.class, Favorite.class}, version = 7, exportSchema = false)
@TypeConverters({LocationConverters.class, ItineraryConverters.class, UserProfileConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookmarkDao bookmarkDao();
    public abstract LocationDao locationDao();
    public abstract ItineraryDao itineraryDao();
    public abstract CategoryDao categoryDao();
    public abstract UserProfileDao userProfileDao();
    public abstract ReviewDao reviewDao();
    public abstract FavoriteDao favoriteDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "travel_planning_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}