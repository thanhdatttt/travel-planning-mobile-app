package com.example.travelplanning.models;

enum UserRole {
    user,
    admin,
    moderator
}

public class User {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String address;
    private String phone;
    private String bio;
    private UserRole role;
}
