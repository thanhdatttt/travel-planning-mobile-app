package com.example.travelplanning.data.model.bookmark;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "bookmarks")
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
@Builder
public class Bookmark {
    @PrimaryKey
    @NonNull
    private String id;
    
    private String userId;
    private String locationId;
    private String createdAt;
}