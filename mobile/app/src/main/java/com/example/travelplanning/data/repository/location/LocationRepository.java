package com.example.travelplanning.data.repository.location;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.util.AndroidStringProvider;
import com.example.travelplanning.core.util.FileUtils;
import com.example.travelplanning.core.util.StringProvider;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.location.LocationApi;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationRepository {
    private final LocationApi locationApi;
    private final LocationMapper locationMapper;
    private final Context context;

    public LocationRepository(Context context) {
        StringProvider stringProvider = new AndroidStringProvider(context);
        this.locationMapper = new LocationMapper();
        this.locationApi = ApiServiceFactory.create(context, LocationApi.class);
        this.context = context;
    }

    public interface LocationListCallback {
        void onSuccess(List<Location> data);
        void onError(String errorMessage);
    }

    public interface PhotoUploadCallback {
        void onSuccess(Photo photo);
        void onError(String errorMessage);
    }

    public void getNearbyLocations(double lat, double lng, Integer radius, Integer categoryId, LocationListCallback callback) {
        locationApi.getNearbyLocations(lat, lng, radius, categoryId).enqueue(new Callback<ApiResponse<List<LocationResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<LocationResponse>>> call,
                                   @NonNull Response<ApiResponse<List<LocationResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationResponse> dtoList = response.body().getData();
                    if (dtoList != null) {
                        List<Location> domainList = new ArrayList<>();
                        for (LocationResponse dto : dtoList) {
                            domainList.add(locationMapper.mapToDomain(dto));
                        }
                        callback.onSuccess(domainList);
                    } else {
                        callback.onError("Không có dữ liệu địa điểm.");
                    }
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<LocationResponse>>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public interface LocationSearchCallback {
        void onSuccess(List<Location> data, MetaResponse meta);
        void onError(String errorMessage);
    }

    public void searchLocations(String query, Integer categoryId, Integer priceLevel, int page, int limit, LocationSearchCallback callback) {
        locationApi.searchLocations(query, categoryId, priceLevel, page, limit).enqueue(new Callback<ApiResponse<PaginatedData<LocationResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaginatedData<LocationResponse>>> call,
                                   @NonNull Response<ApiResponse<PaginatedData<LocationResponse>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PaginatedData<LocationResponse>> apiResponse = response.body();
                    PaginatedData<LocationResponse> paginatedData = apiResponse.getData();

                    if (paginatedData != null && paginatedData.getItems() != null) {
                    
                        List<Location> domainList = new ArrayList<>();
                        for (LocationResponse dto : paginatedData.getItems()) {
                            domainList.add(locationMapper.mapToDomain(dto));
                        }

                        callback.onSuccess(domainList, paginatedData.getMeta());

                    } else {
                        callback.onError("Không tìm thấy kết quả phù hợp.");
                    }
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PaginatedData<LocationResponse>>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public interface LocationDetailCallback {
        void onSuccess(Location data);
        void onError(String errorMessage);
    }
    public void getLocationById(String id, LocationDetailCallback callback) {
        locationApi.getLocationById(id).enqueue(new Callback<ApiResponse<LocationResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LocationResponse>> call,
                                   @NonNull Response<ApiResponse<LocationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LocationResponse dto = response.body().getData();
                    if (dto != null) {
                        callback.onSuccess(locationMapper.mapToDomain(dto));
                    } else {
                        callback.onError("Không tìm thấy thông tin chi tiết.");
                    }
                } else {
                    callback.onError("Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LocationResponse>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public void uploadLocationPhoto(String locationId, Uri imageUri, PhotoUploadCallback callback) {
        try {
            byte[] bytes = FileUtils.getBytes(context, imageUri);
            if (bytes == null) {
                callback.onError("Không thể đọc dữ liệu ảnh.");
                return;
            }

            String mimeType = context.getContentResolver().getType(imageUri);
            RequestBody requestFile = RequestBody.create(bytes, MediaType.parse(mimeType != null ? mimeType : "image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("photo", "location_photo.jpg", requestFile);

            locationApi.uploadPhoto(locationId, body).enqueue(new Callback<ApiResponse<LocationResponse.LocationPhotoResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<LocationResponse.LocationPhotoResponse>> call,
                                       @NonNull Response<ApiResponse<LocationResponse.LocationPhotoResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LocationResponse.LocationPhotoResponse res = response.body().getData();

                        // Map thủ công sang Model Photo của bạn
                        Photo newPhoto = Photo.builder()
                                .id(res.getId())
                                .url(res.getUrl())
                                .isFeature(res.getIsFeature() != null ? res.getIsFeature() : false)
                                .build();

                        callback.onSuccess(newPhoto);
                    } else {
                        callback.onError("Lỗi máy chủ: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<LocationResponse.LocationPhotoResponse>> call, @NonNull Throwable t) {
                    callback.onError("Lỗi kết nối: " + t.getMessage());
                }
            });
        } catch (IOException e) {
            callback.onError("Lỗi file: " + e.getMessage());
        }
    }
}