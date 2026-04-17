package com.example.travelplanning.data.repository.favorite;

import android.content.Context;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.itinerary.ItineraryMapper;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.favorite.FavoriteApi;
import com.example.travelplanning.data.remote.favorite.dto.response.FavoriteResponse;
import com.example.travelplanning.data.repository.bookmark.BookmarkRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRepository {
    private final FavoriteApi favoriteApi;
    private final ItineraryMapper itineraryMapper;

    public FavoriteRepository(Context context) {
        this.favoriteApi = ApiServiceFactory.create(context, FavoriteApi.class);
        this.itineraryMapper = new ItineraryMapper();
    }

    public interface FavoriteCallback<T> {
        void onSuccess(T data, int lastPage);
        void onError(String errorMessage);
    }


    public void toggleFavorite(String itineraryId, FavoriteCallback<String> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("itineraryId", itineraryId);

        favoriteApi.toggleFavorite(body).enqueue(new Callback<ApiResponse<FavoriteResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FavoriteResponse>> call, Response<ApiResponse<FavoriteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getMessage(), 1);
                } else {
                    callback.onError("Failed to toggle favorite");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<FavoriteResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllFavorites(int page, int limit, FavoriteCallback<List<Itinerary>> callback) {
        favoriteApi.getAllFavorites(page, limit).enqueue(new Callback<ApiResponse<List<FavoriteResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FavoriteResponse>>> call, Response<ApiResponse<List<FavoriteResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FavoriteResponse> favList = response.body().getData();
                    List<Itinerary> resultList = new ArrayList<>();
                    ItineraryMapper mapper = new ItineraryMapper();

                    if (favList != null) {
//                        android.util.Log.d("REPO_DEBUG", "FavList size from API: " + favList.size());
                        for (FavoriteResponse fav : favList) {
                            if (fav.getItinerary() != null) {
                                try {
                                    Itinerary domainModel = mapper.mapToDomain(fav.getItinerary());
                                    if (domainModel != null) {
                                        resultList.add(domainModel);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("REPO_DEBUG", "Mapping error: " + e.getMessage());
                                }
                            } else {
                                android.util.Log.w("REPO_DEBUG", "Itinerary object inside FavoriteResponse is NULL");
                            }
                        }
                    }

                    int lp = 1;
                    if (response.body().getMetadata() != null) {
                        lp = response.body().getMetadata().getTotalPages();
                    }

//                    android.util.Log.d("REPO_DEBUG", "Final ResultList size: " + resultList.size());
                    callback.onSuccess(resultList, lp);
                } else {
                    callback.onError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FavoriteResponse>>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void checkFavoriteStatus(String itineraryId, FavoriteCallback<Boolean> callback) {
        favoriteApi.checkStatus(itineraryId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData(), 1);
                } else callback.onSuccess(false, 1);
            }
            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
