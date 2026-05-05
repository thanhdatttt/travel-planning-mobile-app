package com.example.travelplanning.data.remote.admin.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditLocationRequest {
    private String name;
    private String address;
    private String phone;
    private int priceLevel;
    private double avgRating;
    private String imageUrl;
    private int categoryId;
}
