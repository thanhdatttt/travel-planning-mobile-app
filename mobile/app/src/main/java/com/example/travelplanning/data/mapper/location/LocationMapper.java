package com.example.travelplanning.data.mapper.location;

import android.util.Log;

import com.example.travelplanning.core.util.StringProvider;
import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.LocationHour;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;

import java.util.ArrayList;
import java.util.List;

public class LocationMapper implements BaseMapper<LocationResponse, Location> {
    private final StringProvider stringProvider;

    public LocationMapper(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }
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

        List<LocationHour> domainHours = new ArrayList<>();
        if (dto.getOpeningHours() != null) {
            for (LocationResponse.LocationHourResponse h : dto.getOpeningHours()) {
                domainHours.add(LocationHour.builder()
                        .id(h.getId())
                        .dayOfWeek(h.getDayOfWeek())
                        .openTime(h.getOpenTime())
                        .closeTime(h.getCloseTime())
                        .build());
            }
        }

//        Log.d("DEBUG_MAPPER", "Image_url: " + domainPhotos.get(0).getUrl());
//        Log.d("DEBUG_MAPPER", "Hours: " + domainHours.get(0).getOpenTime());
        String categoryName = "Chưa phân loại";
        if (dto.getCategory() != null)
            categoryName = stringProvider.getString(dto.getCategory().getSlug());
//            categoryName = dto.getCategory().getNameVi();


        return Location.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .email(dto.getEmail())
                .avgRating(dto.getAvgRating())
                .priceLevel(dto.getPriceLevel())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .distance(dto.getDistance())
                .categoryName(categoryName)
                .categoryIcon(dto.getCategory() != null ? dto.getCategory().getIcon() : null)
                .imageUrl(primaryImage)
                .photos(domainPhotos)
                .avgRating(dto.getAvgRating())
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .openingHours(domainHours)
                .build();
    }
}