package com.example.travelplanning.data.mapper.location;

import android.util.Log;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.ArrayList;
import java.util.List;

public class LocationMapper implements BaseMapper<LocationResponse, Location> {

    @Override
    public Location mapToDomain(LocationResponse dto) {
        if (dto == null) return null;

        List<Photo> domainPhotos = new ArrayList<>();
        String primaryImage = null;

        if (dto.getPhotos() != null) {
            for (LocationResponse.LocationPhotoResponse p : dto.getPhotos()) {
                // 1. Chuyển đổi từ DTO Photo sang Domain Photo
                Photo domainPhoto = Photo.builder()
                        .id(p.getId())
                        .url(p.getUrl())
                        .isFeature(p.getIsFeature())
                        .build();

                domainPhotos.add(domainPhoto);

                // 2. Xác định ảnh chính (Feature Image) cho thumbnail
                if (p.getIsFeature() != null && p.getIsFeature()) {
                    primaryImage = p.getUrl();
                }
            }
        }

        // Nếu không có ảnh nào được đánh dấu là Feature, lấy ảnh đầu tiên làm primary
        if (primaryImage == null && !domainPhotos.isEmpty()) {
            primaryImage = domainPhotos.get(0).getUrl();
        }

//        Log.d("DEBUG_MAPPER", "Image_url: " + domainPhotos.get(0).getUrl());

        return Location.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .avgRating(dto.getAvgRating())
                .priceLevel(dto.getPriceLevel())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .distance(dto.getDistance())
                .categoryName(dto.getCategory() != null ? dto.getCategory().getNameVi() : "Chưa phân loại")
                .categoryIcon(dto.getCategory() != null ? dto.getCategory().getIcon() : null)
                .imageUrl(primaryImage)
                .photos(domainPhotos)
                .avgRating(dto.getAvgRating())
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .build();
    }
}