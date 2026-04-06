package com.example.travelplanning.data.remote.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInRequest {

    private String usernameOrEmail;
    private String password;

}
