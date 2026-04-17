package com.example.travelplanning.data.remote.moderator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ItineraryReportResponse {
    private String reportId;
    private String itineraryId;
    private String reporterId;
    private String ownerId;

    String ownerUsername;
    private String ownerAvatarUrl;
    private String itineraryTitle;
    private String privacy;
    private String startDate;
    private String endDate;
    private String description;

    private String reporterUsername;
    private String reportReason;
}