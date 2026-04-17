package com.example.travelplanning.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.itinerary.Itinerary;

import com.example.travelplanning.data.local.category.CategoryDao;
import com.example.travelplanning.data.local.location.LocationDao;
import com.example.travelplanning.data.local.location.LocationConverters;
import com.example.travelplanning.data.local.itinerary.ItineraryDao;
import com.example.travelplanning.data.local.itinerary.ItineraryConverters;

@Database(entities = {Location.class, Itinerary.class, Category.class}, version = 3, exportSchema = false)
@TypeConverters({LocationConverters.class, ItineraryConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract LocationDao locationDao();
    public abstract ItineraryDao itineraryDao();
    public abstract CategoryDao categoryDao();

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