package com.example.travelplanning.data.mapper.profile;

import android.util.Log;

import com.example.travelplanning.data.enum_converter.EnumMapper;
import com.example.travelplanning.data.mapper.BaseMapper;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.data.remote.profile.dto.request.UpdateMeRequest;
import com.example.travelplanning.data.remote.profile.dto.response.UserProfileResponse;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UserProfileMapper implements BaseMapper<UserProfileResponse, UserProfile> {

    @Override
    public UserProfile mapToDomain(UserProfileResponse dto) {
        if (dto == null)
            return null;
        UserRole domainRole = EnumMapper.fromString(UserRole.class, dto.getRole(), UserRole.UNKNOWN);

        LocalDate parsedDob = null;
        if (dto.getDob() != null && !dto.getDob().isEmpty()) {
            try {
                //chỉ lấy 10 ký tự đầu
                String dateOnly = dto.getDob().length() > 10
                        ? dto.getDob().substring(0, 10)
                        : dto.getDob();
                parsedDob = LocalDate.parse(dateOnly);
            } catch (DateTimeParseException e) {
                Log.e("MAPPER_ERROR", "Không thể parse DOB: " + dto.getDob());
            }
        }

        return UserProfile.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .address(dto.getAddress())
                .bio(dto.getBio())
                .phone(dto.getPhone())
                .role(domainRole)
                .email(dto.getEmail())
                .avatarUrl(dto.getAvatarUrl())
                .fullName(dto.getFullName())
                .dob(parsedDob)
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
                .dob(domain.getDob() != null ? domain.getDob().toString() : null)
                .bio(domain.getBio())
                .avatarUrl(domain.getAvatarUrl())
                .preference(domain.getPreference())
                .role(domain.getRole() != null ? domain.getRole().getStringValue() : null)
                .build();
    }
}
