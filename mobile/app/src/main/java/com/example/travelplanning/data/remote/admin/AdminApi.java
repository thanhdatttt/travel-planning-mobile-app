package com.example.travelplanning.data.remote.admin;

import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.CreateLocationRequest;
import com.example.travelplanning.data.remote.admin.dto.request.EditLocationRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteLocationRequest;
import com.example.travelplanning.data.remote.admin.dto.request.SoftDeleteUserRequest;
import com.example.travelplanning.data.remote.admin.dto.request.EditUserProfileRequest;

import com.example.travelplanning.data.remote.admin.dto.response.AdminStatResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;
import com.example.travelplanning.data.remote.admin.dto.response.AdminLocationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface AdminApi{
    @GET("api/admin/user/list")
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

    @POST("api/admin/user/{id}")
    Call<ApiResponse<UserProfileResponse>> updateProfile(
            @Path("id") String id,
            @Body  EditUserProfileRequest request
    );

    @GET("api/admin/location/list")
    Call<ApiResponse<List<AdminLocationResponse>>> getAllLocations(@Query("name") String name,
                                                            @Query("sortBy") String sortBy,
                                                            @Query("sortOrder") String sortOrder,
                                                            @Query("minPrice") int minPrice,
                                                            @Query("maxPrice") int maxPrice,
                                                            @Query("minRating") int minRating,
                                                            @Query("maxRating") int maxRating,
                                                            @Query("categoryId") String categoryId,
                                                            @Query("isDeleted") Boolean isDeleted,
                                                            @Query("skip") int skip,
                                                            @Query("take") int take);

    @PATCH("api/admin/location/{id}")
    Call<ApiResponse<AdminLocationResponse>> updateLocation(
            @Path("id") String id,
            @Body EditLocationRequest request
    );

    @PATCH("api/admin/location/soft-delete/{id}")
    Call<ApiResponse<AdminLocationResponse>> softDeleteLocation(
            @Path("id") String id,
            @Body SoftDeleteLocationRequest request
    );

    @GET("api/admin/stat/all")
    Call<ApiResponse<AdminStatResponse>> getAdminStats(
            @Query("month") Integer month,
            @Query("year") Integer year
    );

    @POST("api/admin/location/create")
    Call<ApiResponse<AdminLocationResponse>> createLocation(
            @Body CreateLocationRequest request
    );

    @Multipart
    @POST("api/admin/location/upload-photos")
    Call<ApiResponse<List<String>>> uploadLocationPhotosApi(
            @Part List<MultipartBody.Part> photos
    );
}