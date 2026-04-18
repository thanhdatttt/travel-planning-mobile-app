package com.example.travelplanning.data.model.itinerary;

public interface ItineraryDisplayItem {
    int TYPE_HEADER = 0;
    int TYPE_LOCATION = 1;
    int getViewType();
}
