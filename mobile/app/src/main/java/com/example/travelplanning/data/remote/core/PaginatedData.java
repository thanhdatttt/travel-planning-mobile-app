package com.example.travelplanning.data.remote.core;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedData<T> {
    private List<T> items;
    @SerializedName(value = "meta", alternate = {"metadata"})
    private MetaResponse meta;
}