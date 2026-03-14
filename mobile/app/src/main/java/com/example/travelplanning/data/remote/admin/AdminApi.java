package com.example.travelplanning.data.remote.admin;

//import com.example.travelplanning.data.remote.admin.dto.request;
import com.example.travelplanning.data.model.User;
import com.example.travelplanning.data.remote.admin.dto.respond.UserListResponse;

import com.example.travelplanning.data.remote.core.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface AdminApi{
    @GET("admin/users")
    Call<ApiResponse<UserListResponse>> GetAllUsers();
}