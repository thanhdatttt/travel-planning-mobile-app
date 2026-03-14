package com.example.travelplanning.data.remote.admin.dto.param;

import com.example.travelplanning.data.model.UserRole;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListQuery {
    private String name = "";
    private String email = "";
    private UserRole role;
    private String order = "asc";
    private String sort = "";
    private Boolean isBanned = false;
    private Boolean isActive = false;
    private Integer page;

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<>();
        if (name != null && !name.isEmpty()) params.put("name", name);
        if (email != null && !email.isEmpty()) params.put("email", email);
        if (role != null) params.put("role", role.name().toLowerCase());
        if (order != null) params.put("order", order);
        if (sort != null) params.put("sort", sort);
        if (isBanned != null) params.put("isBanned", String.valueOf(isBanned));
        if (isActive != null) params.put("isActive", String.valueOf(isActive));
        if (page != null) params.put("page", String.valueOf(page));
        return params;
    }
}
