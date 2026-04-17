package com.example.travelplanning.data.remote.favorite.dto.response;

import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;

import lombok.Data;

@Data
public class FavoriteResponse {
    private String id;
    private String userId;
    private String itineraryId;
    private String createdAt;
    private ItineraryResponse itinerary;
}
