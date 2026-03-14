package com.example.travelplanning.data.model.profile;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public enum UserRole {
    @SerializedName("user")
    USER("user"),

    @SerializedName("admin")
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
