package com.example.travelplanning.data.remote.core;

import lombok.Getter;
import lombok.Value;

@Value
public class ApiResponse<T> {
    String message;
    T data;
    String error;
}
