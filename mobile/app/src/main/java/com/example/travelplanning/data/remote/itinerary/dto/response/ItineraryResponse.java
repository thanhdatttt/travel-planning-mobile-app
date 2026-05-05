package com.example.travelplanning.data.remote.itinerary.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResponse {
    private String id;
    private String ownerId;
    private String title;
    private String description;
    private String privacy;
    private Date startDate;
    private Date endDate;
    private Date createdAt;
    private Date updatedAt;

    @SerializedName("user")
    private UserResponse user;

    @SerializedName("itineraryItems")
    private List<ItineraryItemResponse> itineraryItems;

    @SerializedName("imageUrl")
    private String imageUrl;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private String id;
        private String username;
        private String avatarUrl;
    }
}
