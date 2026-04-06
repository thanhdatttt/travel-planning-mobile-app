package com.example.travelplanning.data.remote.itinerary.dto.request;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItineraryRequest {
    private String title;
    private String description;
    private String privacy;
    private Date startDate;
    private Date endDate;
}
