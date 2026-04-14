package com.example.travelplanning.data.remote.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReviewRequest {
    private String title;
    private String body;
    private int rating;
    private String locationId;
}