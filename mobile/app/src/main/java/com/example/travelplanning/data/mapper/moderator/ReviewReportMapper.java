package com.example.travelplanning.data.mapper.moderator;

import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.moderator.ReviewReport;
import com.example.travelplanning.data.remote.moderator.dto.response.ReviewReportResponse;

public class ReviewReportMapper implements BaseMapper<ReviewReportResponse, ReviewReport> {

    @Override
    public ReviewReport mapToDomain(ReviewReportResponse dto) {
        if (dto == null) return null;

        return ReviewReport.builder()
                .reportId(dto.getReportId())
                .avatarUrl(dto.getReviewerAvatarUrl())
                .reviewerName(dto.getReviewerUsername())
                .subtitleText(dto.getReviewDate())
                .formattedRating(dto.getRating() + "/5")
                .title(dto.getReviewTitle())
                .reviewText(dto.getReviewBody())
                .reporterName(dto.getReporterUsername())
                .reportReason(dto.getReportReason())
                .reviewerId(dto.getReviewerId())
                .build();
    }
}