package com.example.travelplanning.data.repository.category;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.category.CategoryDao;
import com.example.travelplanning.data.mapper.category.CategoryMapper;
import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.category.CategoryApi;
import com.example.travelplanning.data.remote.category.dto.response.CategoryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    private final CategoryApi categoryApi;
    private final CategoryMapper categoryMapper;
    private final CategoryDao categoryDao;

    // Background thread xử lý SQLite
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CategoryRepository(Context context) {
        this.categoryApi = ApiServiceFactory.create(context, CategoryApi.class);
        this.categoryMapper = new CategoryMapper();
        this.categoryDao = AppDatabase.getInstance(context).categoryDao();
    }

    public interface CategoryListCallback {
        void onSuccess(List<Category> data);
        void onError(String errorMessage);
    }

    public void getAllCategories(CategoryListCallback callback) {
        executorService.execute(() -> {
            List<Category> cachedList = categoryDao.getAllCategories();
            if (cachedList != null && !cachedList.isEmpty()) {
                mainHandler.post(() -> callback.onSuccess(cachedList));
            }
        });

        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CategoryResponse>>> call,
                                   @NonNull Response<ApiResponse<List<CategoryResponse>>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> dtoList = response.body().getData();
                    
                    if (dtoList != null) {
                        List<Category> domainList = new ArrayList<>();
                        for (CategoryResponse dto : dtoList) {
                            domainList.add(categoryMapper.mapToDomain(dto));
                        }
                        
                        executorService.execute(() -> categoryDao.insertCategories(domainList));

                        callback.onSuccess(domainList);
                    } else {
                        callback.onError("Không có dữ liệu danh mục.");
                    }
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Throwable t) {
            }
        });
    }
}