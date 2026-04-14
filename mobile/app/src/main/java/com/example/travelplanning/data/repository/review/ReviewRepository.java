package com.example.travelplanning.data.repository.review;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.model.review.ReviewPagination;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.remote.review.ReviewApi;
import com.example.travelplanning.data.remote.review.dto.response.ReviewPaginationResponse;
import com.example.travelplanning.data.remote.review.dto.response.ReviewResponse;
import com.example.travelplanning.data.remote.review.dto.response.RatingStatResponse;
import com.example.travelplanning.data.mapper.review.ReviewMapper;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewRepository {
    private final ReviewApi reviewApi;
    private final ReviewMapper reviewMapper;

    public ReviewRepository(Context context) {
        this.reviewApi = ApiServiceFactory.create(context, ReviewApi.class);
        this.reviewMapper = new ReviewMapper();
    }

    public interface ReviewCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public void getReviewStats(String locationId, ReviewCallback<List<RatingStat>> callback) {
        reviewApi.getReviewStats(locationId).enqueue(new Callback<ApiResponse<List<RatingStatResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<RatingStatResponse>>> call,
                                   @NonNull Response<ApiResponse<List<RatingStatResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RatingStat> stats = new ArrayList<>();
                    for (RatingStatResponse res : response.body().getData()) {
                        stats.add(new RatingStat(res.getRating(), res.getCount()));
                    }
                    callback.onSuccess(stats);
                } else {
                    callback.onError("Không thể lấy thống kê đánh giá");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<RatingStatResponse>>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public void getReviewsByLocation(String locationId, int page, int limit, ReviewCallback<ReviewPagination> callback) {
        reviewApi.getReviews(locationId, page, limit).enqueue(new Callback<ApiResponse<List<ReviewResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ReviewResponse>>> call,
                                   @NonNull Response<ApiResponse<List<ReviewResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ReviewResponse>> body = response.body();

                    // 1. Chuyển đổi List DTO sang Domain
                    List<Review> domainReviews = reviewMapper.mapToDomainList(body.getData());

                    // 2. Lấy metadata (check null để tránh crash)
                    int lastPage = 1;
                    if (body.getMetadata() != null) {
                        lastPage = body.getMetadata().getTotalPages();
                    }

                    callback.onSuccess(new ReviewPagination(domainReviews, lastPage));
                } else {
                    callback.onError("Không thể tải đánh giá");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ReviewResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}