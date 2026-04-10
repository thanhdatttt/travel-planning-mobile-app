package com.example.travelplanning.data.remote.admin.dto.request;

import com.example.travelplanning.data.model.profile.UserRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EditUserProfileRequest {
    final private String fullName;
    final private String email;
    final private String address;
    final private String phone;
    final private String dob;
    final private String role;
}
