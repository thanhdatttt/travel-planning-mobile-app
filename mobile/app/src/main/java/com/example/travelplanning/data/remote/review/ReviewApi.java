package com.example.travelplanning.data.remote.review;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.review.dto.request.ReviewRequest;
import com.example.travelplanning.data.remote.review.dto.response.RatingStatResponse;
import com.example.travelplanning.data.remote.review.dto.response.ReviewPaginationResponse;
import com.example.travelplanning.data.remote.review.dto.response.ReviewResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ReviewApi {

    @GET("api/reviews")
    Call<ApiResponse<List<ReviewResponse>>> getReviews(
            @Query("locationId") String locationId,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Lấy thống kê số sao
    @GET("api/reviews/stats/{id}")
    Call<ApiResponse<List<RatingStatResponse>>> getReviewStats(
            @Path("id") String locationId
    );

    @POST("api/reviews")
    Call<ApiResponse<ReviewResponse>> createReview(
            @Body ReviewRequest request
    );
}