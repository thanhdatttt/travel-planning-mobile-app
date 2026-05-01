package com.example.travelplanning.data.remote.favorite.dto.response;

import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class FavoriteResponse {
    private String id;
    private String userId;
    private String itineraryId;
    private String createdAt;

    @SerializedName("itineraryTitle")
    private String itineraryTitle;

    @SerializedName("itineraryDescription")
    private String itineraryDescription;

    @SerializedName("imageUrl")
    private String imageUrl;

}
