package com.example.travelplanning.data.model.itinerary;

import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Itinerary {
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
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class User {
        String id;
        String username;
        String avatarUrl;
    }
}
