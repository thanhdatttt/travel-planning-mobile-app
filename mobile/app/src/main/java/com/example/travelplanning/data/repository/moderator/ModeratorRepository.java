package com.example.travelplanning.data.repository.moderator;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.admin.AdminUserProfileMapper;
import com.example.travelplanning.data.mapper.moderator.ItineraryReportMapper;
import com.example.travelplanning.data.mapper.moderator.LocationReportMapper;
import com.example.travelplanning.data.mapper.moderator.ReviewReportMapper;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.moderator.ItineraryReport;
import com.example.travelplanning.data.model.moderator.LocationReport;
import com.example.travelplanning.data.model.moderator.ReviewReport;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.moderator.ModeratorApi;
import com.example.travelplanning.data.remote.moderator.dto.response.ItineraryReportResponse;
import com.example.travelplanning.data.remote.moderator.dto.response.LocationReportResponse;
import com.example.travelplanning.data.remote.moderator.dto.response.ReviewReportResponse;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModeratorRepository {
    private final ModeratorApi moderatorApi;
    private final ReviewReportMapper reviewMapper;
    private final LocationReportMapper locationMapper;
    private final ItineraryReportMapper itineraryMapper;
    private final AdminUserProfileMapper userMapper;

    public ModeratorRepository(Context context) {
        this.moderatorApi = ApiServiceFactory.create(context, ModeratorApi.class);
        this.reviewMapper = new ReviewReportMapper();
        this.locationMapper = new LocationReportMapper();
        this.itineraryMapper = new ItineraryReportMapper();
        this.userMapper = new AdminUserProfileMapper();
    }

    public interface ModeratorCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getReportsReview(int skip, int take, ModeratorCallback<List<ReviewReport>> callback) {
        moderatorApi.getReportsReview(skip, take).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ReviewReportResponse>>> call, @NonNull Response<ApiResponse<List<ReviewReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewReport> domainReports = response.body().getData().stream()
                            .map(reviewMapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainReports);
                } else {
                    callback.onError("Failed to fetch review reports");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ReviewReportResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getReportsLocation(int skip, int take, ModeratorCallback<List<LocationReport>> callback) {
        moderatorApi.getReportsLocation(skip, take).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<LocationReportResponse>>> call, @NonNull Response<ApiResponse<List<LocationReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationReport> domainReports = response.body().getData().stream()
                            .map(locationMapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainReports);
                } else {
                    callback.onError("Failed to fetch location reports");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<LocationReportResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getReportsItinerary(int skip, int take, ModeratorCallback<List<ItineraryReport>> callback) {
        moderatorApi.getReportsItinerary(skip, take).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ItineraryReportResponse>>> call, @NonNull Response<ApiResponse<List<ItineraryReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItineraryReport> domainReports = response.body().getData().stream()
                            .map(itineraryMapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainReports);
                } else {
                    callback.onError("Failed to fetch itinerary reports");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ItineraryReportResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void toggleUserBan(String userId, boolean ban, ModeratorCallback<UserProfile> callback) {
        moderatorApi.toggleBan(userId, new BanUserRequest(ban)).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(userMapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to update ban status");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // You can leave this returning a generic response or update it to a generic boolean success later
    public void dismissReport(String id, ModeratorCallback<Boolean> callback) {
        moderatorApi.dismissReport(id).enqueue(new Callback<ApiResponse<ReportResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Response<ApiResponse<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(true);
                } else {
                    callback.onError("Failed to dismiss report");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}