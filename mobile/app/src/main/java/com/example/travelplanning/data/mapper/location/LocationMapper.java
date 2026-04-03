package com.example.travelplanning.data.mapper.location;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.ArrayList;
import java.util.List;

public class LocationMapper implements BaseMapper<LocationResponse, Location> {

    @Override
    public Location mapToDomain(LocationResponse dto) {
        if (dto == null) return null;

        List<String> photoUrls = new ArrayList<>();
        String primaryImage = null;

        if (dto.getLocationPhotos() != null) {
            for (LocationResponse.LocationPhotoResponse p : dto.getLocationPhotos()) {
                photoUrls.add(p.getUrl());
                if (p.getIsFeature() != null && p.getIsFeature()) {
                    primaryImage = p.getUrl();
                }
            }
        }

        if (primaryImage == null && !photoUrls.isEmpty()) primaryImage = photoUrls.get(0);

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
                .imageUrl(primaryImage)
                .photoUrls(photoUrls)
                .avgRating(dto.getAvgRating())
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .build();
    }
}