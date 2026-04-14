package com.example.travelplanning.data.remote.auth.dto.response;

import com.example.travelplanning.data.model.profile.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private UserProfile userProfile;
}
