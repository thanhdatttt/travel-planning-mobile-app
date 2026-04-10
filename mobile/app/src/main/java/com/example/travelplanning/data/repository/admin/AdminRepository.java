package com.example.travelplanning.data.repository.admin;

import android.content.Context;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.remote.admin.AdminApi;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.EditLocationRequest;
import com.example.travelplanning.data.remote.admin.dto.request.EditUserProfileRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteLocationRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteUserRequest;
import com.example.travelplanning.data.remote.admin.dto.response.AdminStatResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.mapper.admin.AdminUserProfileMapper;
import com.example.travelplanning.data.mapper.admin.AdminLocationMapper;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;
import com.example.travelplanning.data.remote.admin.dto.response.AdminLocationResponse;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private final AdminApi adminApi;
    private final AdminUserProfileMapper userMapper;
    private final AdminLocationMapper locationMapper;


    public AdminRepository(Context context) {
        this.adminApi = ApiServiceFactory.create(context, AdminApi.class);
        this.userMapper = new AdminUserProfileMapper();
        this.locationMapper = new AdminLocationMapper();
    }

    public interface AdminCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getAllUsers(String usernameOrEmail, Boolean isBanned, Boolean isInActive, String sortBy, String sortOrder, List<UserRole> roles, Boolean isDeleted, AdminCallback<List<UserProfile>> callback) {
        List<String> roleStrings = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        String roleParam = (roleStrings.isEmpty()) ? null : String.join(",", roleStrings);
        adminApi.GetAllUsers(usernameOrEmail, isBanned, isInActive, sortBy, sortOrder, roleParam, isDeleted).enqueue(new Callback<ApiResponse<List<UserProfileResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserProfileResponse>>> call, Response<ApiResponse<List<UserProfileResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserProfile> domainUsers = response.body().getData().stream()
                            .map(userMapper::mapToDomain)
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
        boolean safeBan = (ban != null) ? ban : false;

        adminApi.toggleBan(id, new BanUserRequest(safeBan)).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(userMapper.mapToDomain(response.body().getData()));
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

    public void updateProfile(String id, String fullname, String email, String address, String phone, String dob, UserRole role, AdminCallback<UserProfile> callback){
        String roleString = role.getStringValue().toLowerCase();
        EditUserProfileRequest request = new EditUserProfileRequest(fullname, email, address, phone, dob, roleString);
        adminApi.updateProfile(id, request).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {

            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if(response.isSuccessful() && response.body() != null){
                    callback.onSuccess(userMapper.mapToDomain(response.body().getData()));
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
        boolean safeDelete = (delete != null) ? delete : false;

        adminApi.softDeleteUser(id, new SoftDeleteUserRequest(safeDelete)).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfileResponse>> call, Response<ApiResponse<UserProfileResponse>> response) {
                if(response.isSuccessful() && response.body() != null){
                    callback.onSuccess(userMapper.mapToDomain(response.body().getData()));
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

    public void getAllLocations(String name, String sortBy, String sortOrder, int minPrice, int maxPrice, int minRating, int maxRating, List<String> categoryId, Boolean isDeleted, int skip, int take, AdminCallback<List<Location>> callback){
        String categoryParam = (categoryId.isEmpty()) ? null : String.join(",", categoryId);
        adminApi.getAllLocations(name, sortBy, sortOrder, minPrice, maxPrice, minRating, maxRating, categoryParam, isDeleted, skip, take).enqueue(new Callback<ApiResponse<List<AdminLocationResponse>>>(){
            @Override
            public void onResponse(Call<ApiResponse<List<AdminLocationResponse>>> call, Response<ApiResponse<List<AdminLocationResponse>>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<Location> domainLocations = response.body().getData().stream()
                            .map(locationMapper::mapToDomain)
                            .collect(Collectors.toList());
                    callback.onSuccess(domainLocations);
                } else {
                    callback.onError("Không thể lấy danh sách địa điểm");

                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AdminLocationResponse>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateLocation(String id, String name, String address, String phone, int priceLevel, double avgRating, String imageUrl, String categoryName, AdminCallback<Location> callback){
        EditLocationRequest request = new EditLocationRequest(name, address, phone, priceLevel, avgRating, imageUrl, categoryName);

        adminApi.updateLocation(id, request).enqueue(new Callback<ApiResponse<AdminLocationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminLocationResponse>> call, Response<ApiResponse<AdminLocationResponse>> response) {
                if(response.isSuccessful() && response.body() != null){
                    callback.onSuccess(locationMapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to update user!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminLocationResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void softDeleteLocation(String id, boolean delete, AdminCallback<Location> callback){
        SoftDeleteLocationRequest request = new SoftDeleteLocationRequest(delete);
        adminApi.softDeleteLocation(id, request).enqueue(new Callback<ApiResponse<AdminLocationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminLocationResponse>> call, Response<ApiResponse<AdminLocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(locationMapper.mapToDomain(response.body().getData()));
                } else {
                    callback.onError("Failed to delete user!");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminLocationResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getStatistics(Integer month, Integer year, AdminCallback<AdminStatResponse> callback) {
        adminApi.getAdminStats(month, year).enqueue(new Callback<ApiResponse<AdminStatResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AdminStatResponse>> call, Response<ApiResponse<AdminStatResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Failed to fetch statistics");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AdminStatResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
