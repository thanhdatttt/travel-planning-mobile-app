package com.example.travelplanning.core.network;

import android.content.Context;

import androidx.annotation.NonNull;
import com.example.travelplanning.core.storage.TokenManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        String token = TokenManager.getAccessToken(context);
        Request request = chain.request();

        // assign token to request
        if (token != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }
}