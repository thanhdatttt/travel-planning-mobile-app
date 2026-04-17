package com.example.travelplanning.data.repository.review;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.review.UserReviewMapper;
import com.example.travelplanning.data.model.review.ReviewPagination;
import com.example.travelplanning.data.model.review.UserReview;
import com.example.travelplanning.data.model.review.UserReviewPagination;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.remote.review.ReviewApi;
import com.example.travelplanning.data.remote.review.dto.request.ReviewRequest;
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
    private final UserReviewMapper userReviewMapper;

    public ReviewRepository(Context context) {
        this.reviewApi = ApiServiceFactory.create(context, ReviewApi.class);
        this.reviewMapper = new ReviewMapper();
        this.userReviewMapper = new UserReviewMapper();
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

                    List<Review> domainReviews = reviewMapper.mapToDomainList(body.getData());

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

    public void createReview(ReviewRequest request, ReviewCallback<Review> callback) {
        reviewApi.createReview(request).enqueue(new Callback<ApiResponse<ReviewResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReviewResponse>> call, @NonNull Response<ApiResponse<ReviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Review domainReview = reviewMapper.mapToDomain(response.body().getData());
                    callback.onSuccess(domainReview);
                } else {
                    callback.onError("Bạn đã đánh giá địa điểm này rồi hoặc có lỗi xảy ra");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReviewResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteReview(String reviewId, ReviewCallback<String> callback) {
        reviewApi.deleteReview(reviewId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Xóa đánh giá thành công");
                } else {
                    callback.onError("Không thể xóa đánh giá");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getMyReviews(int page, ReviewCallback<UserReviewPagination> callback) {
        reviewApi.getMyReviews(page, 10).enqueue(new Callback<ApiResponse<List<ReviewResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ReviewResponse>>> call,
                                   @NonNull Response<ApiResponse<List<ReviewResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ReviewResponse>> body = response.body();

                    List<UserReview> domainList = userReviewMapper.mapToDomainList(body.getData());

                    int lastPage = 1;
                    if (body.getMetadata() != null) {
                        lastPage = body.getMetadata().getTotalPages();
                    }

                    callback.onSuccess(new UserReviewPagination(domainList, lastPage));
                } else {
                    callback.onError("Không thể tải danh sách đánh giá");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ReviewResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}