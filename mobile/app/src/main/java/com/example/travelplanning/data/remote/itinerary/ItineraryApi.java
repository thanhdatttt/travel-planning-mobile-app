package com.example.travelplanning.data.remote.itinerary;

import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.core.PaginatedData;
import com.example.travelplanning.data.remote.itinerary.dto.request.AddItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.ScheduleItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryItemNoteRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ItineraryApi {
    // itinerary api
    @GET("/api/itinerary/by-id/{id}")
    Call<ApiResponse<ItineraryResponse>> getItineraryById(
            @Path("id") String id
    );

    @GET("/api/itinerary/public")
    Call<ApiResponse<PaginatedData<ItineraryResponse>>> getPublicItineraries(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("/api/itinerary/me")
    Call<ApiResponse<PaginatedData<ItineraryResponse>>> getUserItineraries(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @POST("/api/itinerary")
    Call<ApiResponse<ItineraryResponse>> createItinerary(
            @Body CreateItineraryRequest request
    );

    @PATCH("/api/itinerary/{id}")
    Call<ApiResponse<ItineraryResponse>> updateItinerary(
            @Path("id") String id,
            @Body UpdateItineraryRequest request
    );

    @DELETE("/api/itinerary/{id}")
    Call<ApiResponse<Void>> deleteItinerary(
            @Path("id") String id
    );

    @POST("/api/itinerary/{id}/clone")
    Call<ApiResponse<ItineraryResponse>> cloneItinerary(
            @Path("id") String id
    );

    // itinerary item api
    @POST("/api/itinerary/{id}/item")
    Call<ApiResponse<ItineraryItemResponse>> addItineraryItem(
            @Path("id") String itineraryId,
            @Body AddItineraryItemRequest request
    );

    @DELETE("/api/itinerary/{id}/item/{itemId}")
    Call<ApiResponse<Void>> deleteItineraryItem(
            @Path("id") String itineraryId,
            @Path("itemId") String itemId
    );
    @PATCH("/api/itinerary/{id}/item/{itemId}/schedule")
    Call<ApiResponse<ItineraryItemResponse>> scheduleItineraryItem(
            @Path("id") String itineraryId,
            @Path("itemId") String itemId,
            @Body ScheduleItineraryItemRequest request
    );

    @PATCH("/api/itinerary/{id}/item/{itemId}/unschedule")
    Call<ApiResponse<ItineraryItemResponse>> unscheduleItineraryItem(
            @Path("id") String itineraryId,
            @Path("itemId") String itemId
    );

    @PATCH("/api/itinerary/{id}/item/{itemId}/note")
    Call<ApiResponse<ItineraryItemResponse>> updateItineraryItemNote(
            @Path("id") String itineraryId,
            @Path("itemId") String itemId,
            @Body UpdateItineraryItemNoteRequest request
    );
}
