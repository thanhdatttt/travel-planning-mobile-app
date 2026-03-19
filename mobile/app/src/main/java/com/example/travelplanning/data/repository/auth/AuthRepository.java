package com.example.travelplanning.data.repository.auth;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.remote.auth.AuthApi;
import com.example.travelplanning.data.remote.auth.dto.request.OTPRequest;
import com.example.travelplanning.data.remote.auth.dto.request.OTPVerifyRequest;
import com.example.travelplanning.data.remote.auth.dto.request.ResetPasswordRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignInRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignOutRequest;
import com.example.travelplanning.data.remote.auth.dto.request.SignUpRequest;
import com.example.travelplanning.data.remote.auth.dto.response.SignInResponse;
import com.example.travelplanning.data.remote.auth.dto.response.SignUpResponse;
import com.example.travelplanning.data.remote.core.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.authApi = ApiServiceFactory.create(context, AuthApi.class);
    }
    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    // login api
    public void login(SignInRequest request, AuthCallback<SignInResponse> callback) {
        authApi.signin(request).enqueue(new Callback<ApiResponse<SignInResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SignInResponse>> call,
                                   @NonNull Response<ApiResponse<SignInResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SignInResponse> apiResponse = response.body();

                    if (apiResponse.getData() != null) {
                        SignInResponse signInData = apiResponse.getData();
                        TokenManager.saveTokens(context,
                                signInData.getAccessToken(),
                                signInData.getRefreshToken());

                        callback.onSuccess(signInData);
                    } else {
                        callback.onError(parseErrorMessage(response));
                    }
                } else {
                    callback.onError("Login failed.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<SignInResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network" + t.getMessage());
            }
        });
    }

    // register api
    public void register(SignUpRequest request, AuthCallback<SignUpResponse> callback) {
        authApi.signup(request).enqueue(new Callback<ApiResponse<SignUpResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<SignUpResponse>> call,
                                   @NonNull Response<ApiResponse<SignUpResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SignUpResponse> apiResponse = response.body();
                    if (apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(parseErrorMessage(response));
                    }
                } else {
                    callback.onError("Register failed.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<SignUpResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    // logout api
    public void logout(AuthCallback<Void> callback) {
        // get refresh token and create request
        String refreshToken = TokenManager.getRefreshToken(context);
        if (refreshToken == null) {
            callback.onError("No refresh token found.");
            return;
        }
        SignOutRequest request = SignOutRequest.builder().refreshToken(refreshToken).build();

        authApi.signout(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    // Clear tokens
                    TokenManager.clearTokens(context);
                    callback.onSuccess(null);
                } else {
                    callback.onError("Logout failed.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                TokenManager.clearTokens(context);
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    // send otp api
    public void sendOTP(OTPRequest request, AuthCallback<Void> callback) {
        authApi.sendOtp(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    // verify otp api
    public void verifyOTP(OTPVerifyRequest request, AuthCallback<Void> callback) {
        authApi.verifyOtp(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    // reset password api
    public void resetPassword(ResetPasswordRequest request, AuthCallback<Void> callback) {
        authApi.resetPassword(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return TokenManager.getAccessToken(context) != null;
    }

    // parse error message from response
    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                // error structure: { "error": "Error message" }
                int errorIndex = errorJson.indexOf("\"error\":\"");
                if (errorIndex != -1) {
                    int start = errorIndex + 9; // length of "\"error\":\""
                    int end = errorJson.indexOf("\"", start);
                    if (end != -1) {
                        return errorJson.substring(start, end);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "An unknown error occurred.";
    }
}