package com.example.travelplanning.data.mapper.report;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;

public class ReportMapper implements BaseMapper<ReportResponse, Report> {
    @Override
    public Report mapToDomain(ReportResponse dto) {
        if (dto == null) return null;

        return Report.builder()
                .id(dto.getId())
                .reporterId(dto.getReporterId())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reason(dto.getReason())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .handledAt(dto.getHandledAt())
                .handledBy(dto.getHandledBy())
                .build();
    }
}