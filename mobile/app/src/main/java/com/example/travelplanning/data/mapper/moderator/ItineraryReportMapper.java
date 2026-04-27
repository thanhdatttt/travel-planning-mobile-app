package com.example.travelplanning.data.mapper.moderator;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.moderator.ItineraryReport;
import com.example.travelplanning.data.remote.moderator.dto.response.ItineraryReportResponse;

public class ItineraryReportMapper implements BaseMapper<ItineraryReportResponse, ItineraryReport> {

    @Override
    public ItineraryReport mapToDomain(ItineraryReportResponse dto) {
        if (dto == null) return null;

        return ItineraryReport.builder()
                .reportId(dto.getReportId())
                .itineraryId(dto.getItineraryId())
                .ownerId(dto.getOwnerId())
                .avatarUrl(dto.getOwnerAvatarUrl())
                .title(dto.getItineraryTitle())
                .ownerName(dto.getOwnerUsername())
                .privacyStatus(dto.getPrivacy())
                // Depending on your date parsing needs, you might want to format these strings here
                .formattedStartDate(dto.getStartDate().split("T")[0])
                .formattedEndDate(dto.getEndDate().split("T")[0])
                .description(dto.getDescription())
                .reporterName(dto.getReporterUsername())
                .reportReason(dto.getReportReason())
                .build();
    }
}