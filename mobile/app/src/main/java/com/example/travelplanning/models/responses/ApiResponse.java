package com.example.travelplanning.models.responses;

public class ApiResponse<T> {
    private String message;
    private T data;
    private String error;

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
