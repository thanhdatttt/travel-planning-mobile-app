package com.example.travelplanning.data.remote.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserProfileResponse {
        String id;
        String email;
        String username;
        String fullName;
        String role;
        boolean isBanned;
        boolean isDeleted;
        String avatarUrl;
        String address;
        String phone;
}
