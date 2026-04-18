package com.example.travelplanning.data.model.review;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "my_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
public class UserReview {
    
    @PrimaryKey
    @NonNull
    @Builder.Default 
    private String reviewId = ""; 

    @Embedded(prefix = "nested_")
    private Review review;
    
    private String locationName;
    private String locationImage;
    private String locationId;
}