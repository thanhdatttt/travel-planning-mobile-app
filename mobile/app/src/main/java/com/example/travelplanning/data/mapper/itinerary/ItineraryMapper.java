package com.example.travelplanning.data.mapper.itinerary;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryResponse;

import java.util.ArrayList;
import java.util.List;

public class ItineraryMapper implements BaseMapper<ItineraryResponse, Itinerary> {
    private final ItineraryItemMapper itemMapper = new ItineraryItemMapper();
    @Override
    public Itinerary mapToDomain(ItineraryResponse dto) {
        if (dto == null) return null;

        List<ItineraryItem> items = new ArrayList<>();
        String photoFromLocation = null;
        if (dto.getItineraryItems() != null) {
            for (int i = 0; i < dto.getItineraryItems().size(); i++) {
                ItineraryItemResponse itemDto = dto.getItineraryItems().get(i);
                ItineraryItem domainItem = itemMapper.mapToDomain(itemDto);
                items.add(domainItem);

                if (i == 0 && itemDto.getLocation() != null &&
                        itemDto.getLocation().getPhotos() != null &&
                        !itemDto.getLocation().getPhotos().isEmpty()) {
                    photoFromLocation = itemDto.getLocation().getPhotos().get(0).getUrl();
                }
            }
        }
        String finalThumbnail = (dto.getImageUrl() != null) ? dto.getImageUrl() : photoFromLocation;

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
                .image(finalThumbnail)
                .build();
    }
}
