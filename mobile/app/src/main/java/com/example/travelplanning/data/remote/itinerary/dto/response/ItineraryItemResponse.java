package com.example.travelplanning.data.remote.itinerary.dto.response;

import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemResponse {
    private String id;
    private String itineraryId;
    private String locationId;
    private String note;
    private int orderIdx;
    private Date date;
    private LocationResponse location;
}
