package com.example.travelplanning.data.model.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewPagination {
    private List<Review> reviews;
    private int lastPage;
}
