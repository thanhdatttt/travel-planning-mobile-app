package com.example.travelplanning.data.remote.review.dto.response;

import lombok.Data;

import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;
import com.google.gson.annotations.SerializedName;

@Data
public class ReviewResponse {
    private String id;
    private String locationId;
    private String userId;
    private String title;
    private String body;
    private int rating;
    private String createdAt;

    @SerializedName("user")
    private UserReviewResponse user;

    @SerializedName("location")
    private LocationResponse location;

    @Data
    public static class UserReviewResponse {
        private String fullName;
        private String avatarUrl;
    }
}