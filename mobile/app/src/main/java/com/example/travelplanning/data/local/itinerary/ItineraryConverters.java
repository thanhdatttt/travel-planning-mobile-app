package com.example.travelplanning.data.local.itinerary;

import androidx.room.TypeConverter;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class ItineraryConverters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Itinerary.User fromUserString(String value) {
        if (value == null) return null;
        return gson.fromJson(value, Itinerary.User.class);
    }
    @TypeConverter
    public static String toUserString(Itinerary.User user) {
        return gson.toJson(user);
    }

    @TypeConverter
    public static List<ItineraryItem> fromItineraryItemListString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<ItineraryItem>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    @TypeConverter
    public static String toItineraryItemListString(List<ItineraryItem> list) {
        return gson.toJson(list);
    }
}