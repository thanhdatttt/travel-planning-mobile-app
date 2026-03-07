package com.example.travelplanning.core.network;

import android.content.Context;
import android.util.Log;

import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.remote.auth.AuthApi;
import com.example.travelplanning.data.remote.auth.dto.request.RefreshTokenRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d("Authenticator", "Access Token expired. Refreshing...");


        String refreshToken = TokenManager.getRefreshToken(context);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }
        synchronized (this) {

            AuthApi authApi = ApiServiceFactory.create(context, AuthApi.class);

            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken(refreshToken)
                    .build();

            retrofit2.Response<ApiResponse<SignInResponse>> refreshResponse = authApi.refreshToken(request).execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {

                String newAccessToken = refreshResponse.body().getData().getAccessToken();
                String newRefreshToken = refreshResponse.body().getData().getRefreshToken();

                TokenManager.saveAccessToken(context, newAccessToken);
                TokenManager.saveRefreshToken(context, newRefreshToken);

                Log.d("Authenticator", "Refreshed successfully.");


                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build();
            } else {

                Log.e("Authenticator", "Refresh token expired.");

                return null;
            }
        }
    }
}