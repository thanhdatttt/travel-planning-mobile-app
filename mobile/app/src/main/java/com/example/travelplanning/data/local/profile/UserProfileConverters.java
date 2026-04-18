package com.example.travelplanning.data.local.profile;

import androidx.room.TypeConverter;
import com.example.travelplanning.data.model.profile.UserRole;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;

public class UserProfileConverters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.toString();
    }
    @TypeConverter
    public static LocalDate toLocalDate(String dateString) {
        return dateString == null ? null : LocalDate.parse(dateString);
    }

    @TypeConverter
    public static String fromMap(Map<String, Object> map) {
        return map == null ? null : gson.toJson(map);
    }
    @TypeConverter
    public static Map<String, Object> toMap(String json) {
        if (json == null) return null;
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @TypeConverter
    public static String fromUserRole(UserRole role) {
        return role == null ? null : role.name();
    }
    @TypeConverter
    public static UserRole toUserRole(String roleString) {
        return roleString == null ? null : UserRole.valueOf(roleString);
    }
}