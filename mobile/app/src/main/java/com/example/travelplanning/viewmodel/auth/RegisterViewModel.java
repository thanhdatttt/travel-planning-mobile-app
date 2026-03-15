package com.example.travelplanning.viewmodel.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.remote.auth.dto.request.OTPRequest;
import com.example.travelplanning.data.remote.auth.dto.request.OTPVerifyRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.repository.auth.AuthRepository;
import lombok.Getter;

@Getter
public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpVerified = new MutableLiveData<>();
    private final MutableLiveData<SignUpResponse> registerSuccess = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    // step 1: send otp
    public void senOTP(String email) {
        if (email.isEmpty()) {
            errorMessage.setValue("Please enter email.");
            return;
        }

        isLoading.setValue(true);
        OTPRequest request = OTPRequest.builder()
                .email(email)
                .type("register")
                .build();
        authRepository.sendOTP(request, new AuthRepository.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                otpSent.setValue(true); // just indicate success
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // step 2: verify otp
    public void verifyOTP(String email, String otp, String type) {
        if (email.isEmpty() || otp.isEmpty()) {
            errorMessage.setValue("Please enter email and OTP.");
            return;
        }

        isLoading.setValue(true);
        OTPVerifyRequest request = OTPVerifyRequest.builder()
                .email(email)
                .otp(otp)
                .type(type)
                .build();
        authRepository.verifyOTP(request, new AuthRepository.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                otpVerified.setValue(true); // just indicate success
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // step 3: register
    public void register(String email, String username, String password, String confirmPassword) {
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage.setValue("Please enter required fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match.");
            return;
        }

        isLoading.setValue(true);
        SignUpRequest request = SignUpRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();
        authRepository.register(request, new AuthRepository.AuthCallback<SignUpResponse>() {
            @Override
            public void onSuccess(SignUpResponse data) {
                isLoading.setValue(false);
                registerSuccess.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
