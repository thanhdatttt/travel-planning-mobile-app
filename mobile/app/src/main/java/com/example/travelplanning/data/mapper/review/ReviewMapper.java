package com.example.travelplanning.data.mapper.review;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.remote.review.dto.response.ReviewResponse;
import java.util.ArrayList;
import java.util.List;

public class ReviewMapper implements BaseMapper<ReviewResponse, Review> {

    @Override
    public Review mapToDomain(ReviewResponse dto) {
        if (dto == null) return null;

        return Review.builder()
                .id(dto.getId())
                .locationId(dto.getLocationId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .body(dto.getBody())
                .rating(dto.getRating())
                .createdAt(dto.getCreatedAt())
                .userName(dto.getUser() != null ? dto.getUser().getFullName() : "Anonymous")
                .build();
    }

    public List<Review> mapToDomainList(List<ReviewResponse> dtos) {
        if (dtos == null) return new ArrayList<>();
        List<Review> list = new ArrayList<>();
        for (ReviewResponse dto : dtos) {
            list.add(mapToDomain(dto));
        }
        return list;
    }
}
