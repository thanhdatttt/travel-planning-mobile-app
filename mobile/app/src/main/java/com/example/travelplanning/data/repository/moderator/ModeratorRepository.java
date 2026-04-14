package com.example.travelplanning.data.repository.moderator;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.admin.AdminUserProfileMapper;
import com.example.travelplanning.data.mapper.report.ReportMapper;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.moderator.ModeratorApi;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModeratorRepository {
    private final ModeratorApi moderatorApi;
    private final ReportMapper reportMapper;
    private final AdminUserProfileMapper userMapper;


    public ModeratorRepository(Context context) {
        this.moderatorApi = ApiServiceFactory.create(context, ModeratorApi.class);
        this.reportMapper = new ReportMapper();
        this.userMapper = new AdminUserProfileMapper();
    }

    public interface ModeratorCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getReports(String targetType, int skip, int take, ModeratorCallback<List<Report>> callback) {
        moderatorApi.getReports(targetType, skip, take).enqueue(new Callback<ApiResponse<List<ReportResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReportResponse>>> call, Response<ApiResponse<List<ReportResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Report> domainReports = response.body().getData().stream()
                            .map(reportMapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainReports);
                } else {
                    callback.onError("Failed to fetch reports");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ReportResponse>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void toggleUserBan(String userId, boolean ban, ModeratorCallback<UserProfile> callback) {
        moderatorApi.toggleBan(userId, new BanUserRequest(ban)).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(userMapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to update ban status");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfileResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void dismissReport(String id, ModeratorCallback<Report> callback) {
        moderatorApi.dismissReport(id).enqueue(new Callback<ApiResponse<ReportResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ReportResponse>> call, Response<ApiResponse<ReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(reportMapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to update ban status");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ReportResponse>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}