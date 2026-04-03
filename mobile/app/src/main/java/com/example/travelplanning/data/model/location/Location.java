package com.example.travelplanning.data.model.location;

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
public class Location {
    String id;
    String name;
    String address;
    Double avgRating;
    Integer priceLevel;
    String description;
    Double latitude;
    Double longitude;
    Double distance;

    String categoryName;
    String categoryIcon;
    String imageUrl;
    Integer ratingCount;
}