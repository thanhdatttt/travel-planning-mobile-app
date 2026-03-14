package com.example.travelplanning.data.remote.admin;

@Value
@Builder
public class UserCriteria {
    String name;
    String email;
    UserRole role;
    String sortBy;    // e.g., "username" or "createdAt"
    String sortOrder; // "asc" or "desc"

    public Map<String, String> toQueryMap() {
        Map<String, String> params = new HashMap<>();
        if (name != null) params.put("name", name);
        if (email != null) params.put("email", email);
        if (role != null) params.put("role", role.getValue());
        if (sortBy != null) params.put("sortBy", sortBy);
        if (sortOrder != null) params.put("sortOrder", sortOrder);
        return params;
    }
}