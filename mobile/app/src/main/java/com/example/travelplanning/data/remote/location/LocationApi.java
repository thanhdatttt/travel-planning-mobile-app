package com.example.travelplanning.data.remote.location;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("api/locations/{id}")
    Call<ApiResponse<LocationResponse>> getLocationById(@Path("id") String id);

    @Multipart
    @POST("api/locations/{id}/photos")
    Call<ApiResponse<LocationResponse.LocationPhotoResponse>> uploadPhoto(
            @Path("id") String locationId,
            @Part MultipartBody.Part photo
    );
}