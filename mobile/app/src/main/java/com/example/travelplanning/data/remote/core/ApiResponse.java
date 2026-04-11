package com.example.travelplanning.data.remote.core;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Value;

@Value
public class ApiResponse<T> {
    String message;
    T data;
    String error;
    @SerializedName(value = "metadata", alternate = {"meta"})
    MetaResponse metadata;
}
