package com.example.travelplanning.data.model.favorite;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
@Builder
public class Favorite {
    @PrimaryKey
    @NonNull
    @Builder.Default
    private String id = "";
    private String userId;
    private String itineraryId;
    private String createdAt;
}