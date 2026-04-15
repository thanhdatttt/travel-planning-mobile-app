package com.example.travelplanning.data.remote.core;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaResponse {
    @SerializedName(value = "total", alternate = {"totalItems"})
    private int total;
    @SerializedName(value = "page", alternate = {"currentPage"})
    private int page;
    private int limit;
    @SerializedName(value = "totalPages", alternate = {"lastPage"})
    private int totalPages;
}