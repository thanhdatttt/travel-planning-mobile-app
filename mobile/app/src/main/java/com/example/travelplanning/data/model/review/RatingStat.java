package com.example.travelplanning.data.model.review;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "rating_stats", primaryKeys = {"locationId", "rating"})
@Data
@AllArgsConstructor(onConstructor_ = {@Ignore})
@NoArgsConstructor
public class RatingStat {
    @NonNull
    private String locationId = "";
    private int rating; // 1, 2, 3, 4, 5
    private int count;  // Số lượng review cho mức sao này
}
