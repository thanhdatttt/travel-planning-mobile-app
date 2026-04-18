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
public class ReviewReportResponse {
    String reportId;
    String reviewId;
    String reporterId;
    String reviewerId;

    String reviewerUsername;
    String reviewerAvatarUrl;

    String reviewDate;
    int rating;

    String reviewTitle;
    String reviewBody;

    String reporterUsername;
    String reportReason;
}