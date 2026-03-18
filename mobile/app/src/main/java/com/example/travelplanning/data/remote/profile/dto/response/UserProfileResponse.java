package com.example.travelplanning.data.remote.profile.dto.response;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.Date;

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

    private LocalDate dob;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
    private String bio;
    private Gson preference;
    private String role;
}
