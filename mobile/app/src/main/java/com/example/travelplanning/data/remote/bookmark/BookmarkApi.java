package com.example.travelplanning.data.remote.bookmark;

import com.example.travelplanning.data.model.bookmark.Bookmark;
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
}