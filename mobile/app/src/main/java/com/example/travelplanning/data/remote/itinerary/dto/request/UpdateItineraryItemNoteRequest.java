package com.example.travelplanning.data.remote.itinerary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItineraryItemNoteRequest {
    private String note;
}
