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
public class LocationReport {
    String reportId;
    String reporterId;
    String locationId;

    String reporterName;
    String reason;
    String status;
    String photoURL;
    String locationName;
    String locationAddress;
    String locationDescription;
}
