package com.example.travelplanning.data.repository.report;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.report.ReportApi;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.remote.report.dto.request.ReportRequest;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;
import com.example.travelplanning.data.mapper.report.ReportMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportRepository {
    private final ReportApi reportApi;
    private final ReportMapper mapper;

    public ReportRepository(Context context) {
        this.reportApi = ApiServiceFactory.create(context, ReportApi.class);
        this.mapper = new ReportMapper();
    }

    public interface ReportCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void reportReview(String reviewId, String reason, ReportCallback<Report> callback) {
        ReportRequest request = ReportRequest.builder()
                .targetType("review")
                .targetId(reviewId)
                .reason(reason)
                .build();

        reportApi.createReport(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Response<ApiResponse<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(mapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to submit review report");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void reportLocation(String locationId, String reason, ReportCallback<Report> callback) {
        ReportRequest request = ReportRequest.builder()
                .targetType("location")
                .targetId(locationId)
                .reason(reason)
                .build();

        reportApi.createReport(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Response<ApiResponse<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(mapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to submit location report");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void reportItinerary(String itineraryId, String reason, ReportCallback<Report> callback) {
        ReportRequest request = ReportRequest.builder()
                .targetType("itinerary")
                .targetId(itineraryId)
                .reason(reason)
                .build();

        reportApi.createReport(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Response<ApiResponse<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(mapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to submit itinerary report");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}