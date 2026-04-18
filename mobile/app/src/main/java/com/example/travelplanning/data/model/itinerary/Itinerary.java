package com.example.travelplanning.data.model.itinerary;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(tableName = "itineraries")
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Itinerary {
    @PrimaryKey
    @NonNull
    String id;
    String ownerId;
    String title;
    String description;
    String privacy;
    Date startDate;
    Date endDate;
    Date createdAt;
    Date updatedAt;
    User user;
    List<ItineraryItem> itineraryItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(onConstructor_ = {@Ignore})
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class User {
        String id;
        String username;
        String avatarUrl;
    }
}
