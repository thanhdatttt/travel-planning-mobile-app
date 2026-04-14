package com.example.travelplanning.data.remote.moderator;

import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ModeratorApi {
    @GET("api/moderator/reports")
    Call<ApiResponse<List<ReportResponse>>> getReports(
            @Query("targetType") String targetType,
            @Query("skip") int skip,
            @Query("take") int take
    );

    @POST("api/moderator/ban-user/{id}")
    Call<ApiResponse<UserProfileResponse>> toggleBan(
            @Path("id") String id,
            @Body BanUserRequest request
    );

    @POST("api/moderator/dismiss/{id}")
    Call<ApiResponse<ReportResponse>> dismissReport(
            @Path("id") String id
    );
}