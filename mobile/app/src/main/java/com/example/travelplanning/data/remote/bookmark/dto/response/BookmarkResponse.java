package com.example.travelplanning.data.remote.bookmark.dto.response;


import com.example.travelplanning.data.remote.location.dto.response.LocationResponse;
import lombok.Data;

@Data
public class BookmarkResponse {
    private String id;
    private String userId;
    private String locationId;
    private String createdAt;
    private LocationResponse location;
}