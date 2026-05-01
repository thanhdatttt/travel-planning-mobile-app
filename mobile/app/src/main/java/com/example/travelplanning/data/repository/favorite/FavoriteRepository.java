package com.example.travelplanning.data.repository.favorite;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.favorite.FavoriteDao;
import com.example.travelplanning.data.local.itinerary.ItineraryDao;
import com.example.travelplanning.data.mapper.itinerary.ItineraryMapper;
import com.example.travelplanning.data.model.favorite.Favorite;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.favorite.FavoriteApi;
import com.example.travelplanning.data.remote.favorite.dto.response.FavoriteResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {
    private final FavoriteApi favoriteApi;
    private final ItineraryMapper itineraryMapper;
    private final Context context;

    private final FavoriteDao favoriteDao;
    private final ItineraryDao itineraryDao;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public FavoriteRepository(Context context) {
        this.context = context.getApplicationContext();
        this.favoriteApi = ApiServiceFactory.create(context, FavoriteApi.class);
        this.itineraryMapper = new ItineraryMapper();

        AppDatabase db = AppDatabase.getInstance(context);
        this.favoriteDao = db.favoriteDao();
        this.itineraryDao = db.itineraryDao();
    }

    public interface FavoriteCallback<T> {
        void onSuccess(T data, int lastPage);
        void onError(String errorMessage);
    }

    public void checkFavoriteStatus(String itineraryId, FavoriteCallback<Boolean> callback) {
        String userId = TokenManager.getUserId(context);
        if (userId != null) {
            executorService.execute(() -> {
                boolean isSavedLocally = favoriteDao.isFavorited(itineraryId, userId);
                mainHandler.post(() -> callback.onSuccess(isSavedLocally, 1));
            });
        }

        favoriteApi.checkStatus(itineraryId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFavoritedOnServer = response.body().getData();
                    if (userId != null) {
                        executorService.execute(() -> {
                            if (isFavoritedOnServer) {
                                favoriteDao.insertFavorite(new Favorite(userId + "_" + itineraryId, userId, itineraryId, null));
                            } else {
                                favoriteDao.deleteFavorite(itineraryId, userId);
                            }
                        });
                    }
                    callback.onSuccess(isFavoritedOnServer, 1);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {}
        });
    }

    public void toggleFavorite(String itineraryId, FavoriteCallback<String> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("itineraryId", itineraryId);
        String userId = TokenManager.getUserId(context);

        favoriteApi.toggleFavorite(body).enqueue(new Callback<ApiResponse<FavoriteResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FavoriteResponse>> call, @NonNull Response<ApiResponse<FavoriteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (userId != null) {
                        executorService.execute(() -> {
                            boolean wasFavorited = favoriteDao.isFavorited(itineraryId, userId);
                            if (wasFavorited) {
                                favoriteDao.deleteFavorite(itineraryId, userId);
                            } else {
                                favoriteDao.insertFavorite(new Favorite(userId + "_" + itineraryId, userId, itineraryId, null));
                            }
                        });
                    }
                    callback.onSuccess(response.body().getMessage(), 1);
                } else {
                    callback.onError("Failed to toggle favorite");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<FavoriteResponse>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllFavorites(int page, int limit, FavoriteCallback<List<Itinerary>> callback) {
        String userId = TokenManager.getUserId(context);

        // 1. Trả về Cache trước (Chỉ cho trang 1)
        if (userId != null && page == 1) {
            executorService.execute(() -> {
                List<Itinerary> cachedItineraries = favoriteDao.getFavoritedItineraries(userId);
                if (cachedItineraries != null && !cachedItineraries.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(cachedItineraries, 1));
                }
            });
        }

        // 2. Gọi API Fetch data mới
        favoriteApi.getAllFavorites(page, limit).enqueue(new Callback<ApiResponse<List<FavoriteResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<FavoriteResponse>>> call, @NonNull Response<ApiResponse<List<FavoriteResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FavoriteResponse> favList = response.body().getData();
                    List<Itinerary> itineraries = new ArrayList<>();
                    List<Favorite> favorites = new ArrayList<>();

                    if (favList != null) {
                        for (FavoriteResponse fav : favList) {
                            Itinerary domainModel = Itinerary.builder()
                                    .id(fav.getItineraryId())
                                    .title(fav.getItineraryTitle())
                                    .description(fav.getItineraryDescription())
                                    .image(fav.getImageUrl())
                                    .build();

                            if (domainModel != null) {
                                itineraries.add(domainModel);
                                if (userId != null) {
                                    favorites.add(new Favorite(userId + "_" + domainModel.getId(), userId, domainModel.getId(), null));
                                }
                            }
                        }

                        // Lưu vào Room DB
                        executorService.execute(() -> {
                            if (!itineraries.isEmpty()) {
                                itineraryDao.insertItineraries(itineraries);
                                favoriteDao.insertFavorites(favorites);
                            }
                        });
                    }

                    int lp = (response.body().getMetadata() != null) ? response.body().getMetadata().getTotalPages() : 1;
                    callback.onSuccess(itineraries, lp);
                } else {
                    callback.onError("Server error: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<FavoriteResponse>>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}