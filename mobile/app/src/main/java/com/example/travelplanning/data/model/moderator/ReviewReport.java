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
public class ReviewReport {
    String reportId;

    String reviewerId;
    String avatarUrl;
    String reviewerName;
    String subtitleText;
    String formattedRating;
    String title;
    String reviewText;
    String reporterName;
    String reportReason;
}