package com.example.travelplanning.viewmodel.admin;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.repository.admin.AdminRepository;
import java.util.List;
import lombok.Getter;

@Getter
public class AdminViewModel extends AndroidViewModel {
    private final AdminRepository adminRepository;
    private final MutableLiveData<List<UserProfile>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AdminViewModel(Application app) {
        super(app);
        this.adminRepository = new AdminRepository(app);
    }

    public void fetchUsers(String usernameOrEmail, Boolean isBanned, Boolean isInActive, String sortBy, String sortOrder, String role, Boolean isDeleted) {
        isLoading.setValue(true);
        adminRepository.getAllUsers(usernameOrEmail, isBanned, isInActive, sortBy, sortOrder, role, isDeleted, new AdminRepository.AdminCallback<List<UserProfile>>() {
            @Override
            public void onSuccess(List<UserProfile> data) {
                isLoading.setValue(false);
                users.setValue(data);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void toggleBanStatus(UserProfile user) {
        adminRepository.banUser(user.getId(), !user.getIsBanned(), new AdminRepository.AdminCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile updatedUser) {
                // Cập nhật lại danh sách local để UI thay đổi ngay lập tức
                fetchUsers(user.getUsername(), user.getIsBanned(), true, "username", "asc", "user", false);
            }

            @Override
            public void onError(String err) {
                error.setValue(err);
            }
        });
    }

    public void toggleSoftDelete(UserProfile user){
        isLoading.setValue(true);
        adminRepository.softDeleteUser(user.getId(), !user.getIsDeleted(), new AdminRepository.AdminCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
                fetchUsers(user.getUsername(), user.getIsBanned(), true, "username", "asc", "user", false);
            }

            @Override
            public void onError(String err) {
                error.setValue(err);
            }
        });
    }
}