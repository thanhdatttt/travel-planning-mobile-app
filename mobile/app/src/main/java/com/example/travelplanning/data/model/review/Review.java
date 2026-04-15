package com.example.travelplanning.data.model.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    String id;
    String locationId;
    String userId;
    String title;
    String body;
    int rating;
    String createdAt;
    String userName; // tên người đánh giá
}