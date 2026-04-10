package com.example.travelplanning.data.remote.admin.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLocationResponse {
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

    private Integer categoryId;

    // Đổi tên Class con cho khớp với khai báo List
    private List<LocationPhotoResponse> photos;

    private Double avgRating;
    private Integer ratingCount;

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