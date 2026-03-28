package com.example.travelplanning.data.remote.admin;

//import com.example.travelplanning.data.remote.admin.dto.request;
import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteUserRequest;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface AdminApi{
    @GET("/api/admin/user/list")
    Call<ApiResponse<List<UserProfileResponse>>> GetAllUsers(@Query("usernameOrEmail") String usernameOrEmail,
                                                             @Query("isBanned") Boolean isBanned,
                                                             @Query("isInactive") Boolean isInactive,
                                                             @Query("sortBy") String sortBy,
                                                             @Query("sortOrder") String sortOrder,
                                                             @Query("role") String role,
                                                             @Query("isDeleted") Boolean isDeleted);

    @POST("api/admin/user/ban/{id}")
    Call<ApiResponse<UserProfileResponse>> toggleBan(
            @Path("id") String id,
            @Body BanUserRequest request
    );

    @POST("api/admin/user/soft-delete/{id}")
    Call<ApiResponse<UserProfileResponse>> softDeleteUser(
            @Path("id") String id,
            @Body SoftDeleteUserRequest request
    );
}