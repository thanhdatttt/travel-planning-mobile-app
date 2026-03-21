package com.example.travelplanning.data.remote.map;

import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotonApiService {

    @GET("api/")
    Call<PhotonResponse> searchPlaces(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("bbox") String bbox
    );
}