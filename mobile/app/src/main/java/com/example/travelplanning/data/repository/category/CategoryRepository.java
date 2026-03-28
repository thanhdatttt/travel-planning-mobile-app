package com.example.travelplanning.data.repository.category;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.category.CategoryMapper;
import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.category.CategoryApi;
import com.example.travelplanning.data.remote.category.dto.response.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    private final CategoryApi categoryApi;
    private final CategoryMapper categoryMapper;

    public CategoryRepository(Context context) {
        // Khởi tạo API thông qua Factory dùng chung của project
        this.categoryApi = ApiServiceFactory.create(context, CategoryApi.class);
        // Khởi tạo Mapper để chuyển đổi DTO sang Domain Model
        this.categoryMapper = new CategoryMapper();
    }

    // Interface callback để trả kết quả về cho ViewModel
    public interface CategoryListCallback {
        void onSuccess(List<Category> data);
        void onError(String errorMessage);
    }

    /**
     * Lấy toàn bộ danh sách danh mục địa điểm
     */
    public void getAllCategories(CategoryListCallback callback) {
        categoryApi.getAllCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CategoryResponse>>> call,
                                   @NonNull Response<ApiResponse<List<CategoryResponse>>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> dtoList = response.body().getData();
                    
                    if (dtoList != null) {
                        // Chuyển đổi danh sách DTO (Remote) sang danh sách Model (Domain)
                        List<Category> domainList = new ArrayList<>();
                        for (CategoryResponse dto : dtoList) {
                            domainList.add(categoryMapper.mapToDomain(dto));
                        }
                        callback.onSuccess(domainList);
                    } else {
                        callback.onError("Không có dữ liệu danh mục.");
                    }
                } else {
                    // Xử lý các lỗi từ phía Server (4xx, 5xx)
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CategoryResponse>>> call, @NonNull Throwable t) {
                // Xử lý lỗi kết nối hoặc lỗi parse dữ liệu
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }
}