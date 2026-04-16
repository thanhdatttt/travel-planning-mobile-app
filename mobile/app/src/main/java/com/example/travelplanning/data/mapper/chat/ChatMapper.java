package com.example.travelplanning.data.mapper.chat;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.chat.ChatMessage;
import com.example.travelplanning.data.remote.chat.dto.ChatDTO;

public class ChatMapper implements BaseMapper<ChatDTO.ChatMessageResponse, ChatMessage> {
    @Override
    public ChatMessage mapToDomain(ChatDTO.ChatMessageResponse dto) {
        if (dto == null) return null;
        return ChatMessage.builder()
                .id(dto.getId())
                .sessionId(dto.getSessionId())
                .role(dto.getRole())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}