package com.example.travelplanning.data.repository.map;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.data.remote.map.PhotonApiService;
import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapRepository {

    private final PhotonApiService apiService;
    private final Context context;

    public MapRepository(Context context) {
        this.context = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://photon.komoot.io/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(PhotonApiService.class);
    }

    public interface PlaceCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public void searchPlaces(String query, double lat, double lon, String bbox, PlaceCallback<PhotonResponse> callback) {
        String vietnamBBox = "102.0,8.0,110.0,24.0";

        apiService.searchPlaces(query, 5, lat, lon, bbox).enqueue(new Callback<PhotonResponse>() {
            @Override
            public void onResponse(@NonNull Call<PhotonResponse> call,
                                   @NonNull Response<PhotonResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi máy chủ tìm kiếm: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PhotonResponse> call, @NonNull Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}