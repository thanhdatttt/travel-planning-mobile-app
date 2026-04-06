package com.example.travelplanning.data.mapper.admin;

import com.example.travelplanning.data.enum_converter.EnumMapper;
import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.remote.admin.dto.response.UserProfileResponse;

public class AdminUserProfileMapper implements BaseMapper<UserProfileResponse, UserProfile> {

    @Override
    public UserProfile mapToDomain(UserProfileResponse dto) {
        if (dto == null)
            return null;
        UserRole domainRole = EnumMapper.fromString(UserRole.class, dto.getRole(), UserRole.UNKNOWN);
        return UserProfile.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .role(domainRole)
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .isBanned(dto.isBanned())
                .isDeleted(dto.isDeleted())
                .build();
    }
}
