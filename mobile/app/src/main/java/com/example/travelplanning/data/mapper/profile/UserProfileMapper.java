package com.example.travelplanning.data.mapper.profile;

import com.example.travelplanning.data.enum_converter.EnumMapper;
import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.remote.profile.dto.request.UpdateMeRequest;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;

public class UserProfileMapper implements BaseMapper<UserProfileResponse, UserProfile> {

    @Override
    public UserProfile mapToDomain(UserProfileResponse dto) {
        if (dto == null)
            return null;
        UserRole domainRole = EnumMapper.fromString(UserRole.class, dto.getRole(), UserRole.UNKNOWN);
        return UserProfile.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .bio(dto.getBio())
                .phone(dto.getPhone())
                .role(domainRole)
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .fullName(dto.getFullName())
                .dob(dto.getDob())
                .build();
    }

    public UpdateMeRequest mapToRequest(UserProfile domain) {
        if (domain == null) return null;

        return UpdateMeRequest.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .fullName(domain.getFullName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .address(domain.getAddress())
                .bio(domain.getBio())
                .avatarUrl(domain.getAvatarUrl())
                .preference(domain.getPreference())
                .role(domain.getRole() != null ? domain.getRole().getStringValue() : null)
                .build();
    }
}
