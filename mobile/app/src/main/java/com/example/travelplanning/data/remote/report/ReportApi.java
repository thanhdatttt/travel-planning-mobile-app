package com.example.travelplanning.data.remote.report;

import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.report.dto.request.ReportRequest;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReportApi {
    @POST("api/reports")
    Call<ApiResponse<ReportResponse>> createReport(@Body ReportRequest request);
}