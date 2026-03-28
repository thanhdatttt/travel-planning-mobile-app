package com.example.travelplanning.data.remote.location;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationApi {

    @GET("api/locations/near-by")
    Call<ApiResponse<List<LocationResponse>>> getNearbyLocations(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radius") Integer radius,
            @Query("categoryId") Integer categoryId
    );

    @GET("api/locations/search")
    Call<ApiResponse<PaginatedData<LocationResponse>>> searchLocations(
            @Query("q") String query,
            @Query("categoryId") Integer categoryId,
            @Query("priceLevel") Integer priceLevel,
            @Query("page") int page,
            @Query("limit") int limit
    );
}