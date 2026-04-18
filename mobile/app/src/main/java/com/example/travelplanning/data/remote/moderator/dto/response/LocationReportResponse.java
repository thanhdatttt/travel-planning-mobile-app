package com.example.travelplanning.data.remote.moderator.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationReportResponse {
    String reportId;
    String reporterId;
    String locationId;

    String reporterName;
    String reason;
    String photoURL;
    String locationName;
    String locationAddress;
    String locationDescription;
    String handledBy;
    String createdAt;
}
