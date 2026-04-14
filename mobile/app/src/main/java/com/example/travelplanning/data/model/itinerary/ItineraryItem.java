package com.example.travelplanning.data.model.itinerary;

import com.example.travelplanning.data.model.location.Location;
import java.util.Date;
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
public class ItineraryItem {
    String id;
    String itineraryId;
    String locationId;
    String note;
    Integer orderIdx;
    Date date;
    Location location;
}
