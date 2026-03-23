package com.example.travelplanning.data.remote.admin.dto.request;

public class SoftDeleteUserRequest {
    final private boolean delete;

    public SoftDeleteUserRequest(boolean delete) {this.delete = delete;}
}
