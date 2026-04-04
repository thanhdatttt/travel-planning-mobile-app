package com.example.travelplanning.data.repository.admin;

import android.content.Context;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.remote.admin.AdminApi;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.mapper.admin.AdminUserProfileMapper;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private final AdminApi adminApi;
    private final AdminUserProfileMapper userMapper;
    private final LocationMapper locationMapper;


    public AdminRepository(Context context) {
        this.adminApi = ApiServiceFactory.create(context, AdminApi.class);
        this.userMapper = new AdminUserProfileMapper();
        this.locationMapper = new LocationMapper();
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

    public void getAllLocations(String name, String sortBy, String sortOrder, int minPrice, int maxPrice, int minRating, int maxRating, List<String> categoryId, int skip, int take, AdminCallback<List<Location>> callback){
        String categoryParam = (categoryId.isEmpty()) ? null : String.join(",", categoryId);
        adminApi.getAllLocations(name, sortBy, sortOrder, minPrice, maxPrice, minRating, maxRating, categoryParam, skip, take).enqueue(new Callback<ApiResponse<List<LocationResponse>>>(){
            @Override
            public void onResponse(Call<ApiResponse<List<LocationResponse>>> call, Response<ApiResponse<List<LocationResponse>>> response) {
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
            public void onFailure(Call<ApiResponse<List<LocationResponse>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
