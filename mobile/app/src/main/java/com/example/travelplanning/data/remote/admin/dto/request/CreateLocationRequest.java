package com.example.travelplanning.data.remote.admin.dto.request;

import java.util.List;

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
public class CreateLocationRequest {
    String name;
    String address;
    String description;
    String website;
    String phone;
    Integer priceLevel;
    Integer categoryId;
    List<String> imgUrls;
}