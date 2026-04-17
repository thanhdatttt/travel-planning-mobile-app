package com.example.travelplanning.data.repository.itinerary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.local.AppDatabase;
import com.example.travelplanning.data.local.itinerary.ItineraryDao;
import com.example.travelplanning.data.mapper.itinerary.ItineraryItemMapper;
import com.example.travelplanning.data.mapper.itinerary.ItineraryMapper;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.itinerary.ItineraryApi;
import com.example.travelplanning.data.remote.itinerary.dto.request.*;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItineraryRepository {
    private final ItineraryApi itineraryApi;
    private final ItineraryMapper itineraryMapper;
    private final ItineraryItemMapper itineraryItemMapper;
    private final ItineraryDao itineraryDao;
    private final Context context;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ItineraryRepository(Context context) {
        this.itineraryApi = ApiServiceFactory.create(context, ItineraryApi.class);
        this.itineraryMapper = new ItineraryMapper();
        this.itineraryItemMapper = new ItineraryItemMapper();
        this.itineraryDao = AppDatabase.getInstance(context).itineraryDao();
        this.context = context.getApplicationContext();
    }

    // --- CALLBACKS (Giữ nguyên của bạn) ---
    public interface ItineraryListCallback {
        void onSuccess(List<Itinerary> data, MetaResponse meta);
        void onError(String errorMessage);
    }
    public interface ItineraryCallback {
        void onSuccess(Itinerary data);
        void onError(String errorMessage);
    }
    public interface ItineraryItemCallback {
        void onSuccess(ItineraryItem data);
        void onError(String errorMessage);
    }
    public interface DeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }


    public void getUserItineraries(int page, int limit, ItineraryListCallback callback) {
        String currentUserId = com.example.travelplanning.core.storage.TokenManager.getUserId(context);

        if (page == 1 && currentUserId != null) {
            executorService.execute(() -> {
                List<Itinerary> cachedList = itineraryDao.getMyCachedItineraries(currentUserId, limit);
                if (cachedList != null && !cachedList.isEmpty()) {
                    mainHandler.post(() -> {
                        callback.onSuccess(cachedList, null);
                    });
                }
            });
        }

        itineraryApi.getUserItineraries(page, limit).enqueue(new Callback<ApiResponse<PaginatedData<ItineraryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call,
                                @NonNull Response<ApiResponse<PaginatedData<ItineraryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedData<ItineraryResponse> paginatedData = response.body().getData();
                    List<Itinerary> domainList = new ArrayList<>();
                    
                    if (paginatedData.getItems() != null) {
                        for (ItineraryResponse res : paginatedData.getItems()) {
                            domainList.add(itineraryMapper.mapToDomain(res));
                        }
                    }

                    executorService.execute(() -> {
                        if (currentUserId != null) {
                            if (page == 1) {
                                itineraryDao.clearMyItineraries(currentUserId);
                            }
                            
                            if (!domainList.isEmpty()) {
                                itineraryDao.insertItineraries(domainList);
                            }
                        }
                    });

                    callback.onSuccess(domainList, paginatedData.getMeta());
                } else {
                    callback.onError("Lỗi máy chủ: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call, @NonNull Throwable t) {
                callback.onError("Không có kết nối mạng");
            }
        });
    }

    // 2. LẤY CHI TIẾT LỊCH TRÌNH
    public void getItineraryById(String id, ItineraryCallback callback) {
        executorService.execute(() -> {
            Itinerary cached = itineraryDao.getItineraryById(id);
            if (cached != null) {
                mainHandler.post(() -> callback.onSuccess(cached));
            }

            itineraryApi.getItineraryById(id).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call,
                                       @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Itinerary freshData = itineraryMapper.mapToDomain(response.body().getData());
                        executorService.execute(() -> {
                            itineraryDao.insertItinerary(freshData);
                            mainHandler.post(() -> callback.onSuccess(freshData));
                        });
                    } else if (cached == null) {
                        callback.onError("Error server (" + response.code() + ").");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) {
                    if (cached == null) callback.onError("Error network: " + t.getMessage());
                }
            });
        });
    }

    // 3. XÓA LỊCH TRÌNH (Xóa mây xong xóa luôn trong DB nội bộ)
    public void deleteItinerary(String id, DeleteCallback callback) {
        itineraryApi.deleteItinerary(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> itineraryDao.deleteItinerary(id));
                    callback.onSuccess();
                } else {
                    callback.onError("Error server (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void getPublicItineraries(int page, int limit, ItineraryListCallback callback) {
        if (page == 1) {
            executorService.execute(() -> {
                List<Itinerary> cachedList = itineraryDao.getCachedPublicItineraries(limit);
                if (cachedList != null && !cachedList.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(cachedList, null));
                }
            });
        }

        itineraryApi.getPublicItineraries(page, limit).enqueue(new Callback<ApiResponse<PaginatedData<ItineraryResponse>>>() {
           @Override
           public void onResponse(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call, 
                                  @NonNull Response<ApiResponse<PaginatedData<ItineraryResponse>>> response) {
               if (response.isSuccessful() && response.body() != null) {
                   PaginatedData<ItineraryResponse> paginatedData = response.body().getData();
                   if (paginatedData != null && paginatedData.getItems() != null) {
                       List<Itinerary> domainList = new ArrayList<>();
                       for (ItineraryResponse dto : paginatedData.getItems()) {
                           domainList.add(itineraryMapper.mapToDomain(dto));
                       }
                       
                       if (page == 1) {
                           executorService.execute(() -> itineraryDao.insertItineraries(domainList));
                       }

                       callback.onSuccess(domainList, paginatedData.getMeta());
                   }
               } else {
                   callback.onError("Error server");
               }
           }
           
           @Override 
           public void onFailure(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call, @NonNull Throwable t) { 
               if (page > 1) {
                   callback.onError("Network error: " + t.getMessage()); 
               }
           }
        });
    }

    public void createItinerary(CreateItineraryRequest request, ItineraryCallback callback) {
        itineraryApi.createItinerary(request).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error server");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void updateItinerary(String id, UpdateItineraryRequest request, ItineraryCallback callback) {
        itineraryApi.updateItinerary(id, request).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error server");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void cloneItinerary(String id, ItineraryCallback callback) {
        itineraryApi.cloneItinerary(id).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error server");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void addItineraryItem(String itineraryId, AddItineraryItemRequest request, ItineraryItemCallback callback) {
        itineraryApi.addItineraryItem(itineraryId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryItemMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error server");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void deleteItineraryItem(String itineraryId, String itemId, DeleteCallback callback) {
        itineraryApi.deleteItineraryItem(itineraryId, itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if(response.isSuccessful()) callback.onSuccess();
                else callback.onError("Error");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void scheduleItineraryItem(String itineraryId, String itemId, ScheduleItineraryItemRequest request, ItineraryItemCallback callback) {
        itineraryApi.scheduleItineraryItem(itineraryId, itemId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryItemMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void unscheduleItineraryItem(String itineraryId, String itemId, ItineraryItemCallback callback) {
        itineraryApi.unscheduleItineraryItem(itineraryId, itemId).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryItemMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }

    public void updateItineraryItemNote(String itineraryId, String itemId, UpdateItineraryItemNoteRequest request, ItineraryItemCallback callback) {
        itineraryApi.updateItineraryItemNote(itineraryId, itemId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                if(response.isSuccessful() && response.body() != null) callback.onSuccess(itineraryItemMapper.mapToDomain(response.body().getData()));
                else callback.onError("Error");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) { callback.onError("Error"); }
        });
    }
}