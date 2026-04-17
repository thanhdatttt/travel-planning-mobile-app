package com.example.travelplanning.viewmodel.profile;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.repository.profile.UserProfileRepository;

import lombok.Getter;

@Getter
public class ProfileViewModel extends AndroidViewModel {
    private final UserProfileRepository userProfileRepository;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> successMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application){
        super(application);
        this.userProfileRepository = new UserProfileRepository(application);
    }

    public void fetchUserProfile(){
//        Log.d("DEBUG_REPO", "getUserProfile called-------------------------------");
        isLoading.setValue(true);
        userProfileRepository.getUserProfile(new UserProfileRepository.UserProfileCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
                userProfile.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void updateUserProfile(UserProfile updatedUserProfile){
        isLoading.setValue(true);
        userProfileRepository.updateUserProfile(updatedUserProfile, new UserProfileRepository.UserProfileCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
                userProfile.setValue(data);
                successMessage.setValue(R.string.update_profile_success);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void uploadAvatar(Uri imageUri) {
        isLoading.setValue(true);
        userProfileRepository.uploadAvatar(imageUri, new UserProfileRepository.UserProfileCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
                userProfile.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

}
