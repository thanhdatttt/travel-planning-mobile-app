package com.example.travelplanning.data.repository.auth;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.remote.auth.AuthApi;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.authApi = ApiServiceFactory.create(context, AuthApi.class);
    }
    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public void login(SignInRequest request, AuthCallback<SignInResponse> callback) {
        authApi.signin(request).enqueue(new Callback<ApiResponse<SignInResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SignInResponse>> call,
                                   @NonNull Response<ApiResponse<SignInResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SignInResponse> apiResponse = response.body();

                    if (apiResponse.getData() != null) {
                        SignInResponse signInData = apiResponse.getData();
                        TokenManager.saveTokens(context,
                                signInData.getAccessToken(),
                                signInData.getRefreshToken());

                        callback.onSuccess(signInData);
                    } else {
                        callback.onError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Invalid input");
                    }
                } else {
                    callback.onError("Login failed.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<SignInResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network" + t.getMessage());
            }
        });
    }
    public void register(SignUpRequest request, AuthCallback<SignUpResponse> callback) {
        authApi.signup(request).enqueue(new Callback<ApiResponse<SignUpResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SignUpResponse>> call,
                                   @NonNull Response<ApiResponse<SignUpResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Register failed.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<SignUpResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return TokenManager.getAccessToken(context) != null;
    }

    public void logout() {
        TokenManager.clearTokens(context);
    }
}