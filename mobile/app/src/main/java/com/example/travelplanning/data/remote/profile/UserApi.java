package com.example.travelplanning.data.remote.profile;

import com.example.travelplanning.data.mapper.profile.UserProfileMapper;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.profile.dto.request.UpdateMeRequest;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {
    @GET("api/users/me")
    Call<ApiResponse<UserProfileResponse>> getProfile();

    @POST("api/users")
    Call<ApiResponse<UserProfileResponse>> updateProfile(@Body UpdateMeRequest request);

    @Multipart
    @POST("/api/users/upload-avatar")
    Call<ApiResponse<UserProfileResponse>> uploadAvatarApi(
            @Part MultipartBody.Part avatar
    );
}
