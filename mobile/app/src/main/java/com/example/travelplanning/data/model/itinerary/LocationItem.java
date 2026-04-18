package com.example.travelplanning.data.model.itinerary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationItem implements ItineraryDisplayItem {
    private final ItineraryItem item;

    @Override
    public int getViewType() {
        return TYPE_LOCATION;
    }
}