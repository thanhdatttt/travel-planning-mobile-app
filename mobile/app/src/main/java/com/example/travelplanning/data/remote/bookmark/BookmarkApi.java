package com.example.travelplanning.data.remote.bookmark;

import com.example.travelplanning.data.remote.core.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.example.travelplanning.data.remote.bookmark.dto.response.BookmarkResponse;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

public interface BookmarkApi {

    @GET("api/bookmarks")
    Call<ApiResponse<List<BookmarkResponse>>> getAllBookmarks(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @POST("api/bookmarks")
    Call<ApiResponse<BookmarkResponse>> toggleBookmark(
            @Body Map<String, String> body
    );

    @DELETE("api/bookmarks/{id}")
    Call<ApiResponse<Void>> deleteBookmark(
            @Path("id") String bookmarkId
    );

    @GET("api/bookmarks/check")
    Call<ApiResponse<Boolean>> checkStatus(@Query("locationId") String locationId);

    @GET("api/bookmarks/me")
    Call<ApiResponse<List<LocationResponse>>> getAllByUserId(
            @Query("page") int page,
            @Query("limit") int limit
    );
}