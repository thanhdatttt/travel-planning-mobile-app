package com.example.travelplanning.data.model.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingStat {
    private int rating; // 1, 2, 3, 4, 5
    private int count;  // Số lượng review cho mức sao này
}
