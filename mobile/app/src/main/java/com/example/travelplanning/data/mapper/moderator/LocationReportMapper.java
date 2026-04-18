package com.example.travelplanning.data.mapper.moderator;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.moderator.LocationReport;
import com.example.travelplanning.data.remote.moderator.dto.response.LocationReportResponse;

public class LocationReportMapper implements BaseMapper<LocationReportResponse, LocationReport> {

    @Override
    public LocationReport mapToDomain(LocationReportResponse dto) {
        if (dto == null) return null;

        return LocationReport.builder()
                .reportId(dto.getReportId())
                .reporterId(dto.getReporterId())
                .locationId(dto.getLocationId())
                .reporterName(dto.getReporterName())
                .reason(dto.getReason())
                .photoURL(dto.getPhotoURL())
                .locationName(dto.getLocationName())
                .locationAddress(dto.getLocationAddress())
                .locationDescription(dto.getLocationDescription())
                // .status("pending")
                .build();
    }
}