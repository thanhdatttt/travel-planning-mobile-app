package com.example.travelplanning.data.remote.auth;

import com.example.travelplanning.data.remote.auth.dto.request.OTPRequest;
import com.example.travelplanning.data.remote.auth.dto.request.OTPVerifyRequest;
import com.example.travelplanning.data.remote.auth.dto.request.RefreshTokenRequest;
import com.example.travelplanning.data.remote.auth.dto.request.ResetPasswordRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;

import com.example.travelplanning.data.remote.auth.dto.request.SignOutRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;

import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    // basic auth
    @POST("/api/auth/signin")
    Call<ApiResponse<SignInResponse>> signin(@Body SignInRequest request);

    @POST("/api/auth/signup")
    Call<ApiResponse<SignUpResponse>> signup(@Body SignUpRequest request);

    @POST("/api/auth/refresh")
    Call<ApiResponse<SignInResponse>> refreshToken(@Body RefreshTokenRequest request);

    @POST("/api/auth/signout")
    Call<ApiResponse<Void>> signout(@Body SignOutRequest request);

    @POST("/api/auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest request);

    // otp
    @POST("/api/auth/otp/send")
    Call<ApiResponse<Void>> sendOtp(@Body OTPRequest request);

    @POST("/api/auth/otp/verify")
    Call<ApiResponse<Void>> verifyOtp(@Body OTPVerifyRequest request);
}
