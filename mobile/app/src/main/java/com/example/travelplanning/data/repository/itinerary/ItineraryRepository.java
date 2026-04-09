package com.example.travelplanning.data.repository.itinerary;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.core.util.AndroidStringProvider;
import com.example.travelplanning.core.util.StringProvider;
import com.example.travelplanning.data.mapper.itinerary.ItineraryItemMapper;
import com.example.travelplanning.data.mapper.itinerary.ItineraryMapper;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.itinerary.ItineraryApi;
import com.example.travelplanning.data.remote.itinerary.dto.request.AddItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.ScheduleItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryItemNoteRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItineraryRepository {
    private final ItineraryApi itineraryApi;
    private final ItineraryMapper itineraryMapper;
    private final ItineraryItemMapper itineraryItemMapper;

    public ItineraryRepository(Context context) {
        this.itineraryApi = ApiServiceFactory.create(context, ItineraryApi.class);

        StringProvider stringProvider = new AndroidStringProvider(context);
        LocationMapper locationMapper = new LocationMapper(stringProvider);

        // Gán vào biến instance của Repository
        this.itineraryItemMapper = new ItineraryItemMapper(locationMapper);
        this.itineraryMapper = new ItineraryMapper(this.itineraryItemMapper);
    }

    // callbacks
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

    // response handlers
    private void handlePaginatedResponse(
            Response<ApiResponse<PaginatedData<ItineraryResponse>>> response,
            ItineraryListCallback callback) {
        if (response.isSuccessful() && response.body() != null) {
            PaginatedData<ItineraryResponse> paginatedData = response.body().getData();

            if (paginatedData != null && paginatedData.getItems() != null) {
                List<Itinerary> domainList = new ArrayList<>();
                for (ItineraryResponse dto : paginatedData.getItems()) {
                    domainList.add(itineraryMapper.mapToDomain(dto));
                }
                callback.onSuccess(domainList, paginatedData.getMeta());
            } else {
                callback.onError("Itinerary data not found.");
            }
        } else {
            callback.onError("Error server (" + response.code() + ").");
        }
    }

    private void handleItineraryResponse(
            Response<ApiResponse<ItineraryResponse>> response,
            ItineraryCallback callback) {
        if (response.isSuccessful() && response.body() != null) {
            callback.onSuccess(itineraryMapper.mapToDomain(response.body().getData()));
        } else {
            callback.onError("Error server (" + response.code() + ").");
        }
    }

    private void handleItemResponse(
            Response<ApiResponse<ItineraryItemResponse>> response,
            ItineraryItemCallback callback) {
        if (response.isSuccessful() && response.body() != null) {
            callback.onSuccess(itineraryItemMapper.mapToDomain(response.body().getData()));
        } else {
            callback.onError("Error server (" + response.code() + ").");
        }
    }

    // itinerary apis
    public void getUserItineraries(int page, int limit, ItineraryListCallback callback) {
        itineraryApi.getUserItineraries(page, limit).enqueue(new Callback<ApiResponse<PaginatedData<ItineraryResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call,
                                   @NonNull Response<ApiResponse<PaginatedData<ItineraryResponse>>> response) {
                handlePaginatedResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void getPublicItineraries(int page, int limit, ItineraryListCallback callback) {
        itineraryApi.getPublicItineraries(page, limit).enqueue(new Callback<ApiResponse<PaginatedData<ItineraryResponse>>>() {
           @Override
           public void onResponse(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call,
                                  @NonNull Response<ApiResponse<PaginatedData<ItineraryResponse>>> response) {
               handlePaginatedResponse(response, callback);
           }

           @Override
           public void onFailure(@NonNull Call<ApiResponse<PaginatedData<ItineraryResponse>>> call, @NonNull Throwable t) {
               callback.onError("Error network: " + t.getMessage());
           }
        });
    }

    public void getItineraryById(String id, ItineraryCallback callback) {
        itineraryApi.getItineraryById(id).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                handleItineraryResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void createItinerary(CreateItineraryRequest request, ItineraryCallback callback) {
        itineraryApi.createItinerary(request).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override
            public void onResponse(@NonNull  Call<ApiResponse<ItineraryResponse>> call,
                                   @NonNull  Response<ApiResponse<ItineraryResponse>> response) {
                handleItineraryResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull  Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void updateItinerary(String id, UpdateItineraryRequest request, ItineraryCallback callback) {
        itineraryApi.updateItinerary(id, request).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                handleItineraryResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void deleteItinerary(String id, DeleteCallback callback) {
        itineraryApi.deleteItinerary(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
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

    public void cloneItinerary(String id, ItineraryCallback callback) {
        itineraryApi.cloneItinerary(id).enqueue(new Callback<ApiResponse<ItineraryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryResponse>> response) {
                handleItineraryResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryResponse>> call,@NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    // itinerary item apis
    public void addItineraryItem(String itineraryId, AddItineraryItemRequest request, ItineraryItemCallback callback) {
        itineraryApi.addItineraryItem(itineraryId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                handleItemResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void deleteItineraryItem(String itineraryId, String itemId, DeleteCallback callback) {
        itineraryApi.deleteItineraryItem(itineraryId, itemId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
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

    public void scheduleItineraryItem(String itineraryId, String itemId,
                                      ScheduleItineraryItemRequest request, ItineraryItemCallback callback) {
        itineraryApi.scheduleItineraryItem(itineraryId, itemId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                handleItemResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void unscheduleItineraryItem(String itineraryId, String itemId, ItineraryItemCallback callback) {
        itineraryApi.unscheduleItineraryItem(itineraryId, itemId).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                handleItemResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call, @NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }

    public void updateItineraryItemNote(String itineraryId, String itemId,
                                        UpdateItineraryItemNoteRequest request, ItineraryItemCallback callback) {
        itineraryApi.updateItineraryItemNote(itineraryId, itemId, request).enqueue(new Callback<ApiResponse<ItineraryItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ItineraryItemResponse>> call,
                                   @NonNull Response<ApiResponse<ItineraryItemResponse>> response) {
                handleItemResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ItineraryItemResponse>> call,@NonNull Throwable t) {
                callback.onError("Error network: " + t.getMessage());
            }
        });
    }
}
