package com.example.travelplanning.data.remote.moderator;

import com.example.travelplanning.data.remote.admin.dto.request.BanUserRequest;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.moderator.dto.response.ItineraryReportResponse;
import com.example.travelplanning.data.remote.moderator.dto.response.LocationReportResponse;
import com.example.travelplanning.data.remote.moderator.dto.response.ReviewReportResponse;
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
    @GET("api/moderator/reports/review")
    Call<ApiResponse<List<ReviewReportResponse>>> getReportsReview(
            @Query("skip") int skip,
            @Query("take") int take
    );

    @GET("api/moderator/reports/location")
    Call<ApiResponse<List<LocationReportResponse>>> getReportsLocation(
            @Query("skip") int skip,
            @Query("take") int take
    );

    @GET("api/moderator/reports/itinerary")
    Call<ApiResponse<List<ItineraryReportResponse>>> getReportsItinerary(
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