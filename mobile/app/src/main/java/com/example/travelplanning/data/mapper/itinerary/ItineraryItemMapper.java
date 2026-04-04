package com.example.travelplanning.data.mapper.itinerary;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.mapper.location.LocationMapper;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.itinerary.dto.response.ItineraryItemResponse;

public class ItineraryItemMapper  implements BaseMapper<ItineraryItemResponse, ItineraryItem> {
    private final LocationMapper locationMapper = new LocationMapper();
    @Override
    public ItineraryItem mapToDomain(ItineraryItemResponse dto) {
        if (dto == null) return null;

        return ItineraryItem.builder()
                .id(dto.getId())
                .itineraryId(dto.getItineraryId())
                .locationId(dto.getLocationId())
                .note(dto.getNote())
                .orderIdx(dto.getOrderIdx())
                .date(dto.getDate())
                .location(locationMapper.mapToDomain(dto.getLocation()))
                .build();
    }
}
