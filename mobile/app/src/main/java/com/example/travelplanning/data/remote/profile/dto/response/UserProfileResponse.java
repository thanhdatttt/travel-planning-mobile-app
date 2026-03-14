package com.example.travelplanning.data.remote.profile.dto.response;

import com.example.travelplanning.data.model.profile.UserRole;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String username;
    private String fullName;

    private String phone;
    private String address;
    private String avatarUrl;
    private String bio;
    private JsonNode preference;
    private String role;
}
