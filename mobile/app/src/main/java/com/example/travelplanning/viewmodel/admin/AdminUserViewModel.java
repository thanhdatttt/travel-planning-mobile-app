package com.example.travelplanning.viewmodel.admin;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.repository.admin.AdminRepository;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class AdminUserViewModel extends AndroidViewModel {
    private final AdminRepository adminRepository;
    private final MutableLiveData<List<UserProfile>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String searchQuery = "";
    private List<UserRole> selectedRoles = new ArrayList<>();
    private boolean isBanned = false;
    private String sortBy = "username";
    private String sortOrder = "asc";
    private boolean isInactive = false;
    private boolean isDeleted = false;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public AdminUserViewModel(Application app) {
        super(app);
        this.adminRepository = new AdminRepository(app);
    }

    private void debounceFetch() {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        searchRunnable = () -> fetchUsers();
        searchHandler.postDelayed(searchRunnable, 300); // Wait 300ms
    }

    public void onSearchQueryChanged(String query) {
        this.searchQuery = query;
        debounceFetch();
    }

    public void fetchUsers() {
        isLoading.setValue(true);

        adminRepository.getAllUsers(searchQuery, isBanned, isInactive, sortBy, sortOrder, selectedRoles, isDeleted, new AdminRepository.AdminCallback<List<UserProfile>>() {
            @Override
            public void onSuccess(List<UserProfile> data) {
                isLoading.setValue(false);
                users.setValue(data);
                System.out.println(data);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void toggleBanStatus(UserProfile user) {
        adminRepository.banUser(user.getId(), Boolean.FALSE.equals(user.getIsBanned()), new AdminRepository.AdminCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile updatedUser) {
            }

            @Override
            public void onError(String err) {
                error.setValue(err);
            }
        });
    }

    public void toggleSoftDelete(UserProfile user){
        isLoading.setValue(true);
        adminRepository.softDeleteUser(user.getId(), Boolean.FALSE.equals(user.getIsDeleted()), new AdminRepository.AdminCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String err) {
                error.setValue(err);
            }
        });
    }

    public void editUser(UserProfile user) {
        isLoading.setValue(true);
        String dobString = null;
        if (user != null && user.getDob() != null) {
            dobString = user.getDob().toString();
        }
        adminRepository.updateProfile(user.getId(), user.getFullName(), user.getEmail(), user.getAddress(), user.getPhone(), dobString, user.getRole(), new AdminRepository.AdminCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String err) {
                error.setValue(err);
            }
        });
    }


    public void applyFilters(boolean banned, boolean deleted, boolean inactive, String sortBy, String sortOrder, List<UserRole> roles) {
        this.isBanned = banned;
        this.isDeleted = deleted;
        this.isInactive = inactive;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.selectedRoles = roles;
        fetchUsers();
    }

    public void resetFilters() {
        this.selectedRoles = new ArrayList<>();
        this.isBanned = false;
        this.isInactive = false;
        this.sortBy = "username";
        this.sortOrder = "asc";
        fetchUsers();
    }
}