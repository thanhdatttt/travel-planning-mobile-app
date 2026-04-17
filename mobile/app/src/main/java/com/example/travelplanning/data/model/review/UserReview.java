package com.example.travelplanning.data.model.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReview {
    private Review review;
    private String locationName;
    private String locationImage;
    private String locationId;
}
