package com.example.travelplanning.data.local.location;

import androidx.room.TypeConverter;
import com.example.travelplanning.data.model.location.LocationHour;
import com.example.travelplanning.data.model.location.Photo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class LocationConverters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<Photo> fromPhotoListString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<Photo>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toPhotoListString(List<Photo> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<LocationHour> fromLocationHourListString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<LocationHour>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toLocationHourListString(List<LocationHour> list) {
        return gson.toJson(list);
    }
}