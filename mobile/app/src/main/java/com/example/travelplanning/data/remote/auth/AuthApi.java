package com.example.travelplanning.data.remote.auth;

import com.example.travelplanning.data.remote.auth.dto.request.RefreshTokenRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;

import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;

import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/signin")
    Call<ApiResponse<SignInResponse>> signin(@Body SignInRequest request);

    @POST("/api/auth/signup")
    Call<ApiResponse<SignUpResponse>> signup(@Body SignUpRequest request);

    @POST("/api/auth/refresh-token")
    Call<ApiResponse<SignInResponse>> refreshToken(@Body RefreshTokenRequest request);
}
