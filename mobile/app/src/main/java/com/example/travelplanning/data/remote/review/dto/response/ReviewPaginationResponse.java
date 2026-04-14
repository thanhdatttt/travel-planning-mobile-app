package com.example.travelplanning.data.remote.review.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Data;

@Data
public class ReviewPaginationResponse {
    @SerializedName("data")
    private List<ReviewResponse> reviews;

    private Metadata metadata;

    @Data
    public static class Metadata {
        private int total;
        private int page;
        private int lastPage;
    }
}