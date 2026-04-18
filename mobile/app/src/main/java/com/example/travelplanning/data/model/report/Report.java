package com.example.travelplanning.data.model.report;

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
public class Report {
    String id;
    String reporterId;
    String targetType;
    String targetId;
    String reason;
    String status;
    String createdAt;
    String handledAt;
    String handledBy;
}