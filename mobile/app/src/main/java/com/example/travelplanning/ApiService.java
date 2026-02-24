package com.example.travelplanning;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/auth/signup")
    Call<LoginResponse> register(@Body LoginRequest request);
}
