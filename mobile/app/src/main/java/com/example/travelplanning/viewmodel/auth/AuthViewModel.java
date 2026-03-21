package com.example.travelplanning.viewmodel.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.remote.auth.dto.request.FacebookRequest;
import com.example.travelplanning.data.remote.auth.dto.request.GoogleRequest;
import com.example.travelplanning.data.remote.auth.dto.request.OTPRequest;
import com.example.travelplanning.data.remote.auth.dto.request.OTPVerifyRequest;
import com.example.travelplanning.data.remote.auth.dto.request.ResetPasswordRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SocialResponse;
import com.example.travelplanning.data.repository.auth.AuthRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthViewModel extends AndroidViewModel {
    private String currentEmail = "";
    private String currentOtpType = "";

    private final AuthRepository authRepository;

    // general states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // login, register, reset states
    private final MutableLiveData<SignInResponse> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<SignUpResponse> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> otpSentSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> otpVerifySuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<SocialResponse> socialLoginSuccess = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    // login
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
                logoutSuccess.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                loginSuccess.setValue(null);
                errorMessage.setValue(message);
            }
        });
    }

    // logout
    public void logout() {
        isLoading.setValue(true);
        authRepository.logout(new AuthRepository.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                logoutSuccess.setValue(true);
                loginSuccess.setValue(null);
                socialLoginSuccess.setValue(null);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                logoutSuccess.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // send otp
    public void sendOTP(String email, String type) {
        if (email.isEmpty()) {
            errorMessage.setValue("Please enter email.");
            return;
        }

        isLoading.setValue(true);
        OTPRequest request = OTPRequest.builder()
                .email(email)
                .type(type)
                .build();
        authRepository.sendOTP(request, new AuthRepository.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                otpSentSuccess.setValue(true);
                otpVerifySuccess.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                otpSentSuccess.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // verify otp
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
                otpVerifySuccess.setValue(true);
                otpSentSuccess.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                otpVerifySuccess.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // register
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
                registerSuccess.setValue(null);
                errorMessage.setValue(message);
            }
        });
    }

    // reset password
    public void resetPassword(String email, String newPassword, String confirmPassword) {
        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage.setValue("Please enter required fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match.");
            return;
        }

        isLoading.setValue(true);
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email(email)
                .newPassword(newPassword)
                .build();
        authRepository.resetPassword(request, new AuthRepository.AuthCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                isLoading.setValue(false);
                resetPasswordSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                resetPasswordSuccess.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    // social login
    public void loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            errorMessage.setValue("Google Token is invalid.");
            return;
        }

        isLoading.setValue(true);
        GoogleRequest request = GoogleRequest.builder()
                .idToken(idToken)
                .build();
        authRepository.loginWithGoogle(request, new AuthRepository.AuthCallback<SocialResponse>() {
            @Override
            public void onSuccess(SocialResponse data) {
                isLoading.setValue(false);
                socialLoginSuccess.setValue(data);
                logoutSuccess.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                socialLoginSuccess.setValue(null);
                errorMessage.setValue(message);
            }
        });
    }

    public void loginWithFacebook(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            errorMessage.setValue("Facebook Token is invalid.");
            return;
        }

        isLoading.setValue(true);
        FacebookRequest request = FacebookRequest.builder()
                .accessToken(accessToken)
                .build();

        authRepository.loginWithFacebook(request, new AuthRepository.AuthCallback<SocialResponse>() {
            @Override
            public void onSuccess(SocialResponse data) {
                isLoading.setValue(false);
                socialLoginSuccess.setValue(data);
                logoutSuccess.setValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                socialLoginSuccess.setValue(null);
                errorMessage.setValue(message);
            }
        });
    }
}
