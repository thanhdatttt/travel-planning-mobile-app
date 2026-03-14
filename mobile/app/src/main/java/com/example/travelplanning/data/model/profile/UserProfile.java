package com.example.travelplanning.data.model.profile;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
    String id;
    String email;
    String username;
    String fullName;
    String phone;
    String address;
    String avatarUrl;
    String bio;

    private Map<String, Object> preference;

    private UserRole role;

    private Date createdAt;
    private Date updatedAt;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

}
