package com.example.travelplanning.data.model.bookmark;

import lombok.Data;

@Data
public class Bookmark {
    private String id;
    private String userId;
    private String locationId;
    private String createdAt;
}