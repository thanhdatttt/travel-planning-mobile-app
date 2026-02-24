package com.example.travelplanning;

public class LoginRequest {
    private String usernameOrEmail;
    private String password;

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
