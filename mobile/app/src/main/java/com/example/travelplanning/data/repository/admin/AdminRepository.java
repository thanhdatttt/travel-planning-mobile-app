package com.example.travelplanning.data.repository.admin;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.remote.admin.AdminApi;


public class AdminRepository {
    private final AdminApi adminApi;
    private final Context context;

    public AdminRepository(Context context) {
        this.context = context.getApplicationContext();
        this.adminApi = ApiServiceFactory.create(context, AdminApi.class);
    }
    public interface AdminCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
