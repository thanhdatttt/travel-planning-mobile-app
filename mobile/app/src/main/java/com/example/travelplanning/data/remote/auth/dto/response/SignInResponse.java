package com.example.travelplanning.data.remote.auth.dto.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("user_id")
    private int userId;

    public String getAccessToken() {
        return accessToken;
    }

    public int getUserId() {
        return userId;
    }
}
