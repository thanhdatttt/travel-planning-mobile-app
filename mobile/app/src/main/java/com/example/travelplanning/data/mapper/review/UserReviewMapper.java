package com.example.travelplanning.data.mapper.review;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.UserReview;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;
import com.example.travelplanning.data.remote.review.dto.response.ReviewResponse;
import java.util.ArrayList;
import java.util.List;

public class UserReviewMapper implements BaseMapper<ReviewResponse, UserReview> {

    private final ReviewMapper reviewMapper = new ReviewMapper();

    @Override
    public UserReview mapToDomain(ReviewResponse dto) {
        if (dto == null) return null;

        Review baseReview = reviewMapper.mapToDomain(dto);

        String thumbUrl = null;
        if (dto.getLocation() != null && dto.getLocation().getPhotos() != null
                && !dto.getLocation().getPhotos().isEmpty()) {
            thumbUrl = dto.getLocation().getPhotos().get(0).getUrl();

            for (LocationResponse.LocationPhotoResponse p : dto.getLocation().getPhotos()) {
                if (Boolean.TRUE.equals(p.getIsFeature())) {
                    thumbUrl = p.getUrl();
                    break;
                }
            }
        }

        return UserReview.builder()
                .review(baseReview)
                .locationId(dto.getLocationId())
                .locationName(dto.getLocation() != null ? dto.getLocation().getName() : "Unknown")
                .locationImage(thumbUrl)
                .build();
    }

    public List<UserReview> mapToDomainList(List<ReviewResponse> dtos) {
        if (dtos == null) return new ArrayList<>();
        List<UserReview> list = new ArrayList<>();
        for (ReviewResponse dto : dtos) {
            list.add(mapToDomain(dto));
        }
        return list;
    }
}