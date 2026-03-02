package com.example.travelplanning.apis;

import com.example.travelplanning.models.User;
import com.example.travelplanning.models.responses.ApiResponse;
import com.example.travelplanning.models.responses.AuthResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/auth/otp/send")
    Call<ApiResponse<Void>> sendOTP(@Body Map<String, String> body);

    @POST("/auth/otp/verify")
    Call<ApiResponse<Void>> verifyOTP(@Body Map<String, String> body);

    @POST("/auth/signup")
    Call<ApiResponse<User>> signUp(@Body Map<String, String> body);

    @POST("/auth/signin")
    Call<ApiResponse<AuthResponse>> signIn(@Body Map<String, String> body);

    @POST("/auth/refresh")
    Call<ApiResponse<AuthResponse>> refresh(@Body Map<String, String> body);
}
