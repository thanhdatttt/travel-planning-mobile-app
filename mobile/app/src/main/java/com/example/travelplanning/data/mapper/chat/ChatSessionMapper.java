package com.example.travelplanning.data.mapper.chat;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.chat.ChatSession;
import com.example.travelplanning.data.remote.chat.dto.ChatDTO;

public class ChatSessionMapper implements BaseMapper<ChatDTO.ChatSessionResponse, ChatSession> {
    @Override
    public ChatSession mapToDomain(ChatDTO.ChatSessionResponse dto) {
        if (dto == null) return null;
        
        return ChatSession.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}