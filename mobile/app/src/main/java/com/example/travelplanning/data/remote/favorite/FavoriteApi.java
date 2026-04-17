package com.example.travelplanning.data.remote.favorite;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.favorite.dto.response.FavoriteResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FavoriteApi {
    @GET("api/favorites")
    Call<ApiResponse<List<FavoriteResponse>>> getAllFavorites(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @POST("api/favorites")
    Call<ApiResponse<FavoriteResponse>> toggleFavorite(
            @Body Map<String, String> body
    );

    @DELETE("api/favorites/{id}")
    Call<ApiResponse<Void>> deleteFavorite(@Path("id") String favoriteId);

    @GET("api/favorites/check")
    Call<ApiResponse<Boolean>> checkStatus(@Query("itineraryId") String itineraryId);
}