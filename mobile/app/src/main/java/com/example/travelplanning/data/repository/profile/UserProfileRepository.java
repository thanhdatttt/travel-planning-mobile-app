package com.example.travelplanning.data.repository.profile;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;
import com.example.travelplanning.data.remote.profile.UserApi;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.mapper.profile.UserProfileMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileRepository {

    private final UserApi userApi;
    private final Context context;
    private final UserProfileMapper userProfileMapper; // Khai báo thợ chuyển đổi

    public UserProfileRepository(Context context) {
        this.context = context.getApplicationContext();
        this.userApi = ApiServiceFactory.create(context, UserApi.class);
        this.userProfileMapper = new UserProfileMapper(); // Khởi tạo Mapper
    }

    public interface UserProfileCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public void getUserProfile(UserProfileCallback<UserProfile> callback) {
        userApi.getProfile().enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call,
                                   @NonNull Response<ApiResponse<UserProfileResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserProfileResponse> apiResponse = response.body();

                    if (apiResponse.getData() != null) {

                        UserProfile cleanUser = userProfileMapper.mapToDomain(apiResponse.getData());

                        callback.onSuccess(cleanUser);

                    } else {
                        callback.onError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Không tìm thấy dữ liệu người dùng");
                    }
                } else {
                    callback.onError("Lỗi máy chủ. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }
}
