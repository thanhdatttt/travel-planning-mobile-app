package com.example.travelplanning.data.remote.profile;

import com.example.travelplanning.data.mapper.profile.UserProfileMapper;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.profile.dto.request.UpdateMeRequest;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface UserApi {
    @GET("api/user/me")
    Call<ApiResponse<UserProfileResponse>> getProfile();

    @POST("api/user")
    Call<ApiResponse<UserProfileResponse>> updateProfile(@Body UpdateMeRequest request);
}
