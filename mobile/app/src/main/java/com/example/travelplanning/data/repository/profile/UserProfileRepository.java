package com.example.travelplanning.data.repository.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.profile.UserProfileDao;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.profile.dto.request.UpdateMeRequest;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;
import com.example.travelplanning.data.remote.profile.UserApi;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.mapper.profile.UserProfileMapper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileRepository {

    private final UserApi userApi;
    private final Context context;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileDao userProfileDao;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserProfileRepository(Context context) {
        this.context = context.getApplicationContext();
        this.userApi = ApiServiceFactory.create(context, UserApi.class);
        this.userProfileMapper = new UserProfileMapper();
        this.userProfileDao = AppDatabase.getInstance(context).userProfileDao();
    }

    public interface UserProfileCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    public void getUserProfile(UserProfileCallback<UserProfile> callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            executorService.execute(() -> {
                UserProfile cachedProfile = userProfileDao.getProfileById(currentUserId);
                if (cachedProfile != null) {
                    mainHandler.post(() -> callback.onSuccess(cachedProfile));
                }
            });
        }

        userApi.getProfile().enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call,
                                   @NonNull Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getData() != null) {
                        UserProfile cleanUser = userProfileMapper.mapToDomain(response.body().getData());
                        
                        executorService.execute(() -> userProfileDao.insertProfile(cleanUser));
                        callback.onSuccess(cleanUser);
                    } else {
                        callback.onError("Không tìm thấy dữ liệu người dùng");
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

    public void updateUserProfile(UserProfile userProfile, UserProfileCallback<UserProfile> callback) {
        UpdateMeRequest request = userProfileMapper.mapToRequest(userProfile);

        userApi.updateProfile(request).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call,
                                   @NonNull Response<ApiResponse<UserProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getData() != null) {
                        UserProfile updatedUser = userProfileMapper.mapToDomain(response.body().getData());
                        
                        executorService.execute(() -> userProfileDao.insertProfile(updatedUser));
                        callback.onSuccess(updatedUser);
                    } else {
                        callback.onError("Cập nhật thông tin thất bại");
                    }
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + "). Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                callback.onError("Không thể kết nối đến máy chủ: " + t.getLocalizedMessage());
            }
        });
    }

    public void uploadAvatar(Uri imageUri, UserProfileCallback<UserProfile> callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("Không thể mở tệp tin");
                return;
            }

            byte[] bytes = getBytes(inputStream);
            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";

            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse(mimeType));
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) extension = "jpg";

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "avatar", "user_avatar_" + System.currentTimeMillis() + "." + extension, requestFile);

            userApi.uploadAvatarApi(body).enqueue(new Callback<ApiResponse<UserProfileResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<UserProfileResponse>> call,
                                       @NonNull Response<ApiResponse<UserProfileResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserProfile updatedUser = userProfileMapper.mapToDomain(response.body().getData());
                        
                        executorService.execute(() -> userProfileDao.insertProfile(updatedUser));
                        callback.onSuccess(updatedUser);
                    } else {
                        callback.onError("Lỗi máy chủ khi upload ảnh: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<UserProfileResponse>> call, @NonNull Throwable t) {
                    callback.onError("Lỗi kết nối mạng: " + t.getLocalizedMessage());
                }
            });

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            callback.onError("Lỗi xử lý file: " + e.getMessage());
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public String getCurrentUserId() {
        return TokenManager.getUserId(context);
    }
}