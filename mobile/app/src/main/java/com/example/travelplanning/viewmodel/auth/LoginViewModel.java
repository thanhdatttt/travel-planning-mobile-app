package com.example.travelplanning.viewmodel.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.repository.auth.AuthRepository;
import lombok.Getter;

@Getter
public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<SignInResponse> loginSuccess = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Please enter required fields.");
            return;
        }

        isLoading.setValue(true);
        SignInRequest request = SignInRequest.builder()
                .usernameOrEmail(email)
                .password(password)
                .build();

        authRepository.login(request, new AuthRepository.AuthCallback<SignInResponse>() {
            @Override
            public void onSuccess(SignInResponse data) {
                isLoading.setValue(false);
                loginSuccess.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}