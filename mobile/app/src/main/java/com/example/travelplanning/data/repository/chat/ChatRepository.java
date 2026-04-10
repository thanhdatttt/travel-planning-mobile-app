package com.example.travelplanning.data.repository.chat;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.travelplanning.data.remote.core.*;
import com.example.travelplanning.core.network.ApiServiceFactory;
import com.example.travelplanning.data.mapper.chat.ChatMapper;
import com.example.travelplanning.data.mapper.chat.ChatSessionMapper;
import com.example.travelplanning.data.model.chat.ChatMessage;
import com.example.travelplanning.data.model.chat.ChatSession;
import com.example.travelplanning.data.remote.core.ApiResponse;
import com.example.travelplanning.data.remote.chat.ChatApi;
import com.example.travelplanning.data.remote.chat.dto.ChatDTO;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private static final String TAG = "API_DEBUG_CHAT";
    private final ChatApi chatApi;
    private final ChatMapper chatMapper; 
    private final ChatSessionMapper chatSessionMapper;

    public ChatRepository(Context context) {
        this.chatMapper = new ChatMapper();
        this.chatSessionMapper = new ChatSessionMapper();
        this.chatApi = ApiServiceFactory.create(context, ChatApi.class);
    }

    public interface SendMessageCallback {
        void onSuccess(String sessionId, ChatMessage responseMessage);
        void onError(String errorMessage);
    }

    public void sendMessage(String content, String sessionId, SendMessageCallback callback) {
        ChatDTO.SendMessageRequest request = new ChatDTO.SendMessageRequest(content, sessionId);
        Log.d(TAG, "--> BẮT ĐẦU GỌI API: content=[" + content + "], sessionId=[" + sessionId + "]");
        chatApi.sendMessage(request).enqueue(new Callback<ApiResponse<ChatDTO.SendMessageDataResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ChatDTO.SendMessageDataResponse>> call,
                                   @NonNull Response<ApiResponse<ChatDTO.SendMessageDataResponse>> response) {
                Log.d(TAG, "<-- NHẬN PHẢN HỒI TỪ SERVER: HTTP Status Code = " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "    Thành công! Body: " + response.body().toString());
                    ChatDTO.SendMessageDataResponse data = response.body().getData();
                    if (data != null && data.getMessage() != null) {
                        ChatMessage domainMessage = chatMapper.mapToDomain(data.getMessage());
                        callback.onSuccess(data.getSessionId(), domainMessage);
                    } else {
                        callback.onError("Không nhận được dữ liệu phản hồi từ chatbot.");
                    }
                } else {

                    Log.e(TAG, "    Lỗi Server: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "    Chi tiết lỗi (Error Body): " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ChatDTO.SendMessageDataResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "<-- LỖI ON_FAILURE (Thường do mạng hoặc sai Base URL): " + t.getMessage(), t);
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public interface GetSessionMessagesCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String errorMessage);
    }

    public void getSessionMessages(String sessionId, GetSessionMessagesCallback callback) {
        chatApi.getSessionMessages(sessionId).enqueue(new Callback<ApiResponse<List<ChatDTO.ChatMessageResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ChatDTO.ChatMessageResponse>>> call,
                                   @NonNull Response<ApiResponse<List<ChatDTO.ChatMessageResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatDTO.ChatMessageResponse> dtoList = response.body().getData();
                    if (dtoList != null) {
                        List<ChatMessage> domainList = new ArrayList<>();
                        for (ChatDTO.ChatMessageResponse dto : dtoList) {
                            domainList.add(chatMapper.mapToDomain(dto));
                        }
                        callback.onSuccess(domainList);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ChatDTO.ChatMessageResponse>>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public interface DeleteSessionCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void deleteSession(String sessionId, DeleteSessionCallback callback) {
        chatApi.deleteSession(sessionId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call,
                                   @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Không thể xóa phiên chat (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }

    public interface GetAllSessionsCallback {
        void onSuccess(List<ChatSession> sessions, MetaResponse meta);
        void onError(String errorMessage);
    }

    public void getAllSessions(int page, int limit, GetAllSessionsCallback callback) {
        Log.d("API_DEBUG_SESSION", "--> Gọi API lấy danh sách Session (Page: " + page + ")");
        chatApi.getAllSessions(page, limit).enqueue(new Callback<ApiResponse<PaginatedData<ChatDTO.ChatSessionResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PaginatedData<ChatDTO.ChatSessionResponse>>> call,
                                   @NonNull Response<ApiResponse<PaginatedData<ChatDTO.ChatSessionResponse>>> response) {
                Log.d("API_DEBUG_SESSION", "<-- Nhận phản hồi Session: Code = " + response.code());                    
                if (response.isSuccessful() && response.body() != null) {
                    var paginatedData = response.body().getData();
                    if (paginatedData != null && paginatedData.getItems() != null) {
                        Log.d("API_DEBUG_SESSION", "    Data trả về có " + paginatedData.getItems().size() + " items");
                        List<ChatSession> domainList = new ArrayList<>();
                        for (ChatDTO.ChatSessionResponse dto : paginatedData.getItems()) {
                            domainList.add(chatSessionMapper.mapToDomain(dto));
                        }
                        callback.onSuccess(domainList, paginatedData.getMeta());
                    } else {
                        Log.w("API_DEBUG_SESSION", "    Data trả về null hoặc rỗng");
                        
                        callback.onSuccess(new ArrayList<>(), null);
                    }
                } else {
                    Log.e("API_DEBUG_SESSION", "    Lỗi Server: " + response.message());
                    callback.onError("Lỗi máy chủ (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PaginatedData<ChatDTO.ChatSessionResponse>>> call, @NonNull Throwable t) {
                Log.e("API_DEBUG_SESSION", "<-- LỖI MẠNG GỌI SESSION: " + t.getMessage(), t);
                callback.onError("Lỗi kết nối: " + t.getLocalizedMessage());
            }
        });
    }
}