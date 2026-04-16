package com.example.travelplanning.data.model.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserReviewPagination {
    private List<UserReview> userReviews;
    private int lastPage;
}
