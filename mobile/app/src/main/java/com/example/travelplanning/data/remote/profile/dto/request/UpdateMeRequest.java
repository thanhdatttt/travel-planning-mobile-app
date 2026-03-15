package com.example.travelplanning.data.remote.profile.dto.request;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMeRequest {
    String id;
    String email;
    String username;
    String fullName;

    String phone;
    String address;
    String avatarUrl;
    String bio;
    Map<String, Object> preference;
    String role;
}
