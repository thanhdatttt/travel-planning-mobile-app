package com.example.travelplanning.data.model.favorite;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Favorite {
    private String id;
    private String userId;
    private String itineraryId;
    private String createdAt;
}