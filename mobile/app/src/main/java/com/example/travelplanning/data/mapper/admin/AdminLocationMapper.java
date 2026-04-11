package com.example.travelplanning.data.mapper.admin;

import android.util.Log;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.remote.admin.dto.response.AdminLocationResponse;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationMapper implements BaseMapper<AdminLocationResponse, Location> {

    @Override
    public Location mapToDomain(AdminLocationResponse dto) {
        if (dto == null) return null;

        List<Photo> domainPhotos = new ArrayList<>();
        String primaryImage = null;

        if (dto.getPhotos() != null) {
            for (AdminLocationResponse.LocationPhotoResponse p : dto.getPhotos()) {
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
                .categoryId(dto.getCategoryId())
                .imageUrl(primaryImage)
                .photos(domainPhotos)
                .avgRating(dto.getAvgRating())
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .build();
    }
}