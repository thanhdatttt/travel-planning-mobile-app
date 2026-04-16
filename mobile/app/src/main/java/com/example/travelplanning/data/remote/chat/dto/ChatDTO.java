package com.example.travelplanning.data.remote.chat.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatDTO {
    @Data
    @AllArgsConstructor
    public static class SendMessageRequest {
        private String content;
        private String sessionId;
    }

    @Data
    public static class ChatMessageResponse {
        private String id;
        private String sessionId;
        private String role;
        private String content;
        private String createdAt;
    }

    @Data
    public static class SendMessageDataResponse {
        private String sessionId;
        private ChatMessageResponse message;
    }
    @Data
    public static class ChatSessionResponse {
        private String id;
        private String userId;
        private String title;
        private String createdAt;
    }
}