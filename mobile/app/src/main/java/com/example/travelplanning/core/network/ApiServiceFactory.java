package com.example.travelplanning.core.network;

import android.content.Context;

public class ApiServiceFactory {
    public static <T> T create(Context context, Class<T> service) {
        return RetrofitClient.getClient(context).create(service);
    }
}
