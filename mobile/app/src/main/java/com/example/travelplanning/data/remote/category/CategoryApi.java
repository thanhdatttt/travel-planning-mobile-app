package com.example.travelplanning.data.remote.category;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.category.dto.response.CategoryResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET; 
public interface CategoryApi {
    @GET("api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getAllCategories();
}