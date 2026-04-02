package com.example.travelplanning.data.remote.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaResponse {
    private int total;
    private int page;
    private int limit;
    private int totalPages;
}