package com.example.travelplanning.viewmodel.category;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.repository.category.CategoryRepository;

import java.util.List;

import lombok.Getter;

@Getter
public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;

    // Danh sách category dùng để hiển thị trong Filter hoặc màn hình chính
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo repository tương ứng
        this.categoryRepository = new CategoryRepository(application);
    }

    /**
     * Lấy toàn bộ danh sách danh mục địa điểm
     */
    public void fetchAllCategories() {
        isLoading.setValue(true);
        categoryRepository.getAllCategories(new CategoryRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> data) {
                isLoading.setValue(false);
                // Vì category thường không phân trang, ta set toàn bộ list nhận được
                categories.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}