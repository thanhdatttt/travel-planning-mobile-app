package com.example.travelplanning.data.repository.location;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.util.FileUtils;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.location.LocationDao;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationRepository {
    private final LocationApi locationApi;
    private final LocationMapper locationMapper;
    private final LocationDao locationDao;
    private final Context context;

    // Executor để chạy các tác vụ Database ở Background (tránh lỗi khóa Main Thread)
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    // Handler để đẩy dữ liệu từ Background Thread về Main Thread cập nhật UI
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LocationRepository(Context context) {
        this.context = context.getApplicationContext();
        this.locationMapper = new LocationMapper();
        this.locationApi = ApiServiceFactory.create(context, LocationApi.class);
        
        // Khởi tạo Database DAO
        this.locationDao = AppDatabase.getInstance(context).locationDao();
    }

    // --- CÁC INTERFACE CALLBACK ---
    public interface LocationListCallback {
        void onSuccess(List<Location> data);
        void onError(String errorMessage);
    }

    public interface LocationSearchCallback {
        void onSuccess(List<Location> data, MetaResponse meta);
        void onError(String errorMessage);
    }

    public interface LocationDetailCallback {
        void onSuccess(Location data);
        void onError(String errorMessage);
    }

    public interface PhotoUploadCallback {
        void onSuccess(Photo photo);
        void onError(String errorMessage);
    }

    // --------------------------------------------------------
    // 1. CHI TIẾT ĐỊA ĐIỂM (OFFLINE-FIRST)
    // --------------------------------------------------------
    public void getLocationById(String id, LocationDetailCallback callback) {
        // Bước 1: Lấy từ Local Cache trước cho UI hiển thị ngay lập tức
        executorService.execute(() -> {
            Location cachedLocation = locationDao.getLocationById(id);
            if (cachedLocation != null) {
                mainHandler.post(() -> callback.onSuccess(cachedLocation));
            }

            // Bước 2: Gọi API ngầm để lấy phiên bản mới nhất từ Server
            locationApi.getLocationById(id).enqueue(new Callback<ApiResponse<LocationResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<LocationResponse>> call,
                                       @NonNull Response<ApiResponse<LocationResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LocationResponse dto = response.body().getData();
                        if (dto != null) {
                            Location freshLocation = locationMapper.mapToDomain(dto);
                            
                            // Bước 3: Lưu đè dữ liệu mới vào Local Cache và báo cho UI cập nhật
                            executorService.execute(() -> {
                                locationDao.insertLocation(freshLocation);
                                mainHandler.post(() -> callback.onSuccess(freshLocation));
                            });
                        }
                    } else if (cachedLocation == null) {
                        // Nếu Cache rỗng mà Server báo lỗi thì mới báo lỗi ra UI
                        callback.onError("Không tìm thấy thông tin chi tiết. (Lỗi: " + response.code() + ")");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<LocationResponse>> call, @NonNull Throwable t) {
                    // Nếu mất mạng và Cache cũng rỗng thì báo lỗi
                    if (cachedLocation == null) {
                        callback.onError("Lỗi kết nối và không có dữ liệu Offline: " + t.getLocalizedMessage());
                    }
                }
            });
        });
    }

    // --------------------------------------------------------
    // 2. TÌM KIẾM ĐỊA ĐIỂM (OFFLINE-FIRST CHO TRANG 1)
    // --------------------------------------------------------
    public void searchLocations(String query, Integer categoryId, Integer priceLevel, int page, int limit, LocationSearchCallback callback) {
        // Tìm trong Cache trước (Chỉ áp dụng cho trang đầu tiên để load nhanh)
        if (page == 1) {
            executorService.execute(() -> {
                String safeQuery = (query != null) ? query : "";
                List<Location> cachedResults = locationDao.searchOffline(safeQuery, limit);
                if (cachedResults != null && !cachedResults.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(cachedResults, null));
                }
            });
        }

        // Luôn gọi API để lấy kết quả chính xác nhất
        locationApi.searchLocations(query, categoryId, priceLevel, page, limit).enqueue(new Callback<ApiResponse<PaginatedData<LocationResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaginatedData<LocationResponse>>> call,
                                   @NonNull Response<ApiResponse<PaginatedData<LocationResponse>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedData<LocationResponse> paginatedData = response.body().getData();

                    if (paginatedData != null && paginatedData.getItems() != null) {
                        List<Location> domainList = new ArrayList<>();
                        for (LocationResponse dto : paginatedData.getItems()) {
                            domainList.add(locationMapper.mapToDomain(dto));
                        }
                        
                        // Lưu mẻ data này vào Room để lần sau dùng Offline
                        executorService.execute(() -> locationDao.insertLocations(domainList));
                        
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
                // Nếu đang ở page 1 và lỗi mạng, chúng ta không ném lỗi nữa vì đã hiện Cache rồi.
                // Trừ khi load thêm trang 2, trang 3 mà rớt mạng thì mới báo lỗi.
                if (page > 1) {
                    callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
                }
            }
        });
    }

    // --------------------------------------------------------
    // 3. ĐỊA ĐIỂM GẦN ĐÂY (API LÀ CHÍNH, CACHE BACKGROUND)
    // --------------------------------------------------------
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
                        
                        // Lưu Background để dùng cho Search Offline sau này
                        executorService.execute(() -> locationDao.insertLocations(domainList));
                        
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

    // --------------------------------------------------------
    // 4. UPLOAD HÌNH ẢNH (GIỮ NGUYÊN)
    // --------------------------------------------------------
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