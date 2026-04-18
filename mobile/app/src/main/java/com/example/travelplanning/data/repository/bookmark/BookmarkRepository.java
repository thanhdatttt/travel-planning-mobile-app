package com.example.travelplanning.data.repository.bookmark;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.bookmark.BookmarkDao;
import com.example.travelplanning.data.local.location.LocationDao;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.model.bookmark.Bookmark;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.bookmark.BookmarkApi;
import com.example.travelplanning.data.remote.bookmark.dto.response.BookmarkResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkRepository {
    private final BookmarkApi bookmarkApi;
    private final LocationMapper locationMapper;
    private final Context context;

    private final BookmarkDao bookmarkDao;
    private final LocationDao locationDao;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BookmarkRepository(Context context) {
        this.context = context.getApplicationContext();
        this.bookmarkApi = ApiServiceFactory.create(context, BookmarkApi.class);
        this.locationMapper = new LocationMapper();
        
        AppDatabase db = AppDatabase.getInstance(context);
        this.bookmarkDao = db.bookmarkDao();
        this.locationDao = db.locationDao();
    }

    public interface BookmarkCallback<T> {
        void onSuccess(T data, int lastPage);
        void onError(String errorMessage);
    }

    public void checkBookmarkStatus(String locationId, BookmarkCallback<Boolean> callback) {
        String userId = TokenManager.getUserId(context);
        
        if (userId != null) {
            executorService.execute(() -> {
                boolean isSavedLocally = bookmarkDao.isBookmarked(locationId, userId);
                mainHandler.post(() -> callback.onSuccess(isSavedLocally, 1));
            });
        }

        bookmarkApi.checkStatus(locationId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isBookmarkedOnServer = response.body().getData();
                    
                    if (userId != null) {
                        executorService.execute(() -> {
                            if (isBookmarkedOnServer) {
                                bookmarkDao.insertBookmark(new Bookmark(userId + "_" + locationId, userId, locationId, null));
                            } else {
                                bookmarkDao.deleteBookmark(locationId, userId);
                            }
                        });
                    }
                    callback.onSuccess(isBookmarkedOnServer, 1);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
            }
        });
    }

    public void toggleBookmark(String locationId, BookmarkCallback<String> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("locationId", locationId);
        String userId = TokenManager.getUserId(context);

        bookmarkApi.toggleBookmark(body).enqueue(new Callback<ApiResponse<BookmarkResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BookmarkResponse>> call,
                                   @NonNull Response<ApiResponse<BookmarkResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    
                    if (userId != null) {
                        executorService.execute(() -> {
                            boolean wasBookmarked = bookmarkDao.isBookmarked(locationId, userId);
                            if (wasBookmarked) {
                                bookmarkDao.deleteBookmark(locationId, userId);
                            } else {
                                bookmarkDao.insertBookmark(new Bookmark(userId + "_" + locationId, userId, locationId, null));
                            }
                        });
                    }
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
        String userId = TokenManager.getUserId(context);

        if (userId != null && page == 1) {
            executorService.execute(() -> {
                List<Location> cachedLocations = bookmarkDao.getBookmarkedLocations(userId);
                if (cachedLocations != null && !cachedLocations.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(cachedLocations, 1));
                }
            });
        }

        bookmarkApi.getAllBookmarks(page, limit).enqueue(new Callback<ApiResponse<List<BookmarkResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<BookmarkResponse>>> call,
                                   @NonNull Response<ApiResponse<List<BookmarkResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<BookmarkResponse>> body = response.body();
                    List<Location> locations = new ArrayList<>();
                    List<Bookmark> bookmarks = new ArrayList<>();

                    if (body.getData() != null) {
                        for (BookmarkResponse res : body.getData()) {
                            if (res.getLocation() != null) {
                                Location loc = locationMapper.mapToDomain(res.getLocation());
                                locations.add(loc);

                                if (userId != null) {
                                    Bookmark bm = new Bookmark(userId + "_" + loc.getId(), userId, loc.getId(), null);
                                    bookmarks.add(bm);
                                }
                            }
                        }

                        executorService.execute(() -> {
                            if (!locations.isEmpty()) {
                                locationDao.insertLocations(locations); 
                                bookmarkDao.insertBookmarks(bookmarks); 
                            }
                        });
                    }

                    int lastPage = (body.getMetadata() != null) ? body.getMetadata().getTotalPages() : 1;
                    callback.onSuccess(locations, lastPage);
                } else {
                    callback.onError("Lỗi tải danh sách bookmark");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<BookmarkResponse>>> call, @NonNull Throwable t) {
            }
        });
    }
}