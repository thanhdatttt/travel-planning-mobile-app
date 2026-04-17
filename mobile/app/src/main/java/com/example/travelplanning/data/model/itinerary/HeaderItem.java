package com.example.travelplanning.data.model.itinerary;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HeaderItem implements ItineraryDisplayItem {
    private Date date;

    @Override
    public int getViewType() {
        return TYPE_HEADER;
    }
}
