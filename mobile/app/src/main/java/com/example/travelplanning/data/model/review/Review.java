package com.example.travelplanning.data.model.review;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity(tableName = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@androidx.room.Ignore})
public class Review {
    @PrimaryKey
    @NonNull
    String id;

    String locationId;
    String userId;
    String title;
    String body;
    int rating;
    String createdAt;
    String userName;
}