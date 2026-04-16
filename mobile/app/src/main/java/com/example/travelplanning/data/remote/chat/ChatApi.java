package com.example.travelplanning.data.remote.chat;

import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.chat.dto.ChatDTO;

import java.util.List;
import com.example.travelplanning.data.remote.core.PaginatedData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query; 
public interface ChatApi {

    @POST("api/chatbot/message")
    Call<ApiResponse<ChatDTO.SendMessageDataResponse>> sendMessage(@Body ChatDTO.SendMessageRequest request);

    @GET("api/chatbot/sessions/{sessionId}/messages")
    Call<ApiResponse<List<ChatDTO.ChatMessageResponse>>> getSessionMessages(@Path("sessionId") String sessionId);

    @GET("api/chatbot/sessions")
    Call<ApiResponse<PaginatedData<ChatDTO.ChatSessionResponse>>> getAllSessions(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @DELETE("api/chatbot/sessions/{sessionId}")
    Call<ApiResponse<Object>> deleteSession(@Path("sessionId") String sessionId);
}