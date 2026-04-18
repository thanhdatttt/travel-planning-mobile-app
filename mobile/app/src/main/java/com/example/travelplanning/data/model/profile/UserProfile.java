package com.example.travelplanning.data.model.profile;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@Entity(tableName = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {
    @PrimaryKey
    @NonNull
    String id;
    String email;
    String username;
    LocalDate dob;

    String fullName;
    String phone;
    String address;
    String avatarUrl;
    String bio;

    Boolean isBanned;
    Boolean isDeleted;

    private Map<String, Object> preference;

    private UserRole role;

    private Date createdAt;
    private Date updatedAt;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

}
