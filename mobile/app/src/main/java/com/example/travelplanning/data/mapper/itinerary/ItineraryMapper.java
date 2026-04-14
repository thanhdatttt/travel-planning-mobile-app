package com.example.travelplanning.data.mapper.itinerary;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;

import java.util.ArrayList;
import java.util.List;

public class ItineraryMapper implements BaseMapper<ItineraryResponse, Itinerary> {
    private final ItineraryItemMapper itemMapper;

    public ItineraryMapper(ItineraryItemMapper itineraryItemMapper){
        itemMapper = itineraryItemMapper;
    }

    @Override
    public Itinerary mapToDomain(ItineraryResponse dto) {
        if (dto == null) return null;

        List<ItineraryItem> items = new ArrayList<>();
        if (dto.getItineraryItems() != null) {
            for (ItineraryItemResponse itemDto : dto.getItineraryItems()) {
                items.add(itemMapper.mapToDomain(itemDto));
            }
        }

        Itinerary.User owner = null;
        if (dto.getUser() != null) {
            owner = Itinerary.User.builder()
                    .id(dto.getUser().getId())
                    .username(dto.getUser().getUsername())
                    .avatarUrl(dto.getUser().getAvatarUrl())
                    .build();
        }

        return Itinerary.builder()
                .id(dto.getId())
                .ownerId(dto.getOwnerId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .privacy(dto.getPrivacy())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .user(owner)
                .itineraryItems(items)
                .build();
    }
}
