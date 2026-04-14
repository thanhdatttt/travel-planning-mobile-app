package com.example.travelplanning.data.remote.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private String id;
    private String reporterId;
    private String targetType;
    private String targetId;
    private String reason;
    private String status;
    private String createdAt;
    private String handledAt;
    private String handledBy;
}