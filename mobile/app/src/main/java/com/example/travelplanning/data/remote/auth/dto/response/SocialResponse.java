package com.example.travelplanning.data.remote.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialResponse {
    private String accessToken;
    private String refreshToken;
}
