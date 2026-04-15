package com.example.travelplanning.data.remote.location.dto.response;

import com.example.travelplanning.data.model.location.LocationHour;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {
    private String id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String website;
    private String email;
    private Integer priceLevel;
    private Double latitude;
    private Double longitude;
    private Double distance;

    private CategoryResponse category;

    @SerializedName(value = "photos", alternate = {"locationPhotos"})
    private List<LocationPhotoResponse> photos;

    @SerializedName("imageUrl")
    private String imageUrl;

    private Double avgRating;
    private Integer ratingCount;

    @SerializedName("opening_hours")
    private List<LocationHourResponse> openingHours;

    @Data
    public static class CategoryResponse {
        private  String slug;
        private String nameVi;
        private String icon;
    }

    @Data
    public static class LocationPhotoResponse {
        @SerializedName("id")
        private String id;
        @SerializedName("url")
        private String url;
        @SerializedName("isFeature")
        private Boolean isFeature;
    }

    @Data
    public static class LocationHourResponse{
        private String id;
        private int dayOfWeek;
        private int openTime;
        private int closeTime;
    }
}