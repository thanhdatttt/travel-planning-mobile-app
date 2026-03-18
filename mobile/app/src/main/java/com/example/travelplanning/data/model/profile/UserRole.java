package com.example.travelplanning.data.model.profile;

import com.example.travelplanning.data.enum_converter.MappableEnum;

public enum UserRole implements MappableEnum {
    USER("user"), ADMIN("admin"), MODERATOR("moderator"), UNKNOWN("unknown");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return this.value;
    }

}
