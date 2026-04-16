package com.example.travelplanning.data.repository.bookmark;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.bookmark.BookmarkApi;
import com.example.travelplanning.data.remote.bookmark.dto.response.BookmarkResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkRepository {
    private final BookmarkApi bookmarkApi;
    private final LocationMapper locationMapper;

    public BookmarkRepository(Context context) {
        this.bookmarkApi = ApiServiceFactory.create(context, BookmarkApi.class);
        this.locationMapper = new LocationMapper();
    }

    public interface BookmarkCallback<T> {
        void onSuccess(T data, int lastPage);
        void onError(String errorMessage);
    }

    public void toggleBookmark(String locationId, BookmarkCallback<String> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("locationId", locationId);

        bookmarkApi.toggleBookmark(body).enqueue(new Callback<ApiResponse<BookmarkResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BookmarkResponse>> call,
                                   @NonNull Response<ApiResponse<BookmarkResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessage(), 1);
                } else {
                    callback.onError("Không thể thực hiện bookmark");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BookmarkResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllBookmarks(int page, int limit, BookmarkCallback<List<Location>> callback) {
        bookmarkApi.getAllBookmarks(page, limit).enqueue(new Callback<ApiResponse<List<BookmarkResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<BookmarkResponse>>> call,
                                   @NonNull Response<ApiResponse<List<BookmarkResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<BookmarkResponse>> body = response.body();
                    List<Location> locations = new ArrayList<>();

                    for (BookmarkResponse res : body.getData()) {
                        if (res.getLocation() != null) {
                            locations.add(locationMapper.mapToDomain(res.getLocation()));
                        }
                    }

                    int lastPage = (body.getMetadata() != null) ? body.getMetadata().getTotalPages() : 1;
                    callback.onSuccess(locations, lastPage);
                } else {
                    callback.onError("Lỗi tải danh sách bookmark");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<BookmarkResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void checkBookmarkStatus(String locationId, BookmarkCallback<Boolean> callback) {
        bookmarkApi.checkStatus(locationId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData(), 1);
                } else {
                    callback.onSuccess(false, 1);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}