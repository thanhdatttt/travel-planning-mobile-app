package com.example.travelplanning.data.mapper.location;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

public class LocationMapper implements BaseMapper<LocationResponse, Location> {

    @Override
    public Location mapToDomain(LocationResponse dto) {
        if (dto == null) return null;

        String firstImageUrl = null;
        if (dto.getLocationPhotos() != null && !dto.getLocationPhotos().isEmpty()) {
            firstImageUrl = dto.getLocationPhotos().get(0).getUrl();
        }

        return Location.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .avgRating(dto.getAvgRating())
                .priceLevel(dto.getPriceLevel())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .distance(dto.getDistance())
                .categoryName(dto.getCategory() != null ? dto.getCategory().getNameVi() : "Chưa phân loại")
                .categoryIcon(dto.getCategory() != null ? dto.getCategory().getIcon() : null)
                .imageUrl(firstImageUrl)
                .avgRating(dto.getAvgRating())
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .build();
    }
}