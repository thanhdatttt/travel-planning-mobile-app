package com.example.travelplanning.data.remote.location.dto.response;

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
    private Integer priceLevel;
    private Double latitude;
    private Double longitude;
    private Double distance;

    private CategoryResponse category;

    // Đổi tên Class con cho khớp với khai báo List
    private List<LocationPhotoResponse> photos;

    private Double avgRating;
    private Integer ratingCount;

    @Data
    public static class CategoryResponse {
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
}