package com.example.travelplanning.data.remote.auth.dto.response;

import com.example.travelplanning.data.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private User user;
}
