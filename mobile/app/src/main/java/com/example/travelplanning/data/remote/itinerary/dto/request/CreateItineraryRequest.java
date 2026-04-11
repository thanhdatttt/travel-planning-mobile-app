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
public class CreateItineraryRequest {
    private String title;
    private Date startDate;
    private Date endDate;
}
