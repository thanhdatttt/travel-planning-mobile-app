package com.example.travelplanning.data.model.moderator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItineraryReport {
    String reportId;
    String itineraryId;
    String ownerId;

    String ownerName;
    String avatarUrl;
    String title;
    String privacyStatus;
    String formattedStartDate;
    String formattedEndDate;
    String description;
    String reporterName;
    String reportReason;
}