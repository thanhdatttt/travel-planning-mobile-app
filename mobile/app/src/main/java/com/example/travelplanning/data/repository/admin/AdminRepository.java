package com.example.travelplanning.data.repository.admin;

import android.content.Context;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.remote.admin.AdminApi;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.mapper.profile.UserProfileMapper;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;

import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {
    private final AdminApi adminApi;
    private final UserProfileMapper mapper;

    public AdminRepository(Context context) {
        this.adminApi = ApiServiceFactory.create(context, AdminApi.class);
        this.mapper = new UserProfileMapper();
    }

    public interface AdminCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getAllUsers(String usernameOrEmail, Boolean isBanned, Boolean isActive, String sortBy, String sortOrder, String role, Boolean isDeleted, AdminCallback<List<UserProfile>> callback) {
        adminApi.GetAllUsers(usernameOrEmail, isBanned, isActive, sortBy, sortOrder, role, isDeleted).enqueue(new Callback<ApiResponse<List<UserProfileResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserProfileResponse>>> call, Response<ApiResponse<List<UserProfileResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserProfile> domainUsers = response.body().getData().stream()
                            .map(mapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainUsers);
                } else {
                    callback.onError("Không thể lấy danh sách người dùng");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserProfileResponse>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void banUser(String id, Boolean ban, AdminCallback<UserProfile> callback) {
        adminApi.toggleBan(id, new BanUserRequest(ban)).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(mapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Thao tác thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfileResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    public void softDeleteUser(String id, Boolean delete, AdminCallback<UserProfile> callback) {
        adminApi.softDeleteUser(id, new SoftDeleteUserRequest(delete)).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if(response.isSuccessful() && response.body() != null){
                    callback.onSuccess(mapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Thao tác thất bại");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfileResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
