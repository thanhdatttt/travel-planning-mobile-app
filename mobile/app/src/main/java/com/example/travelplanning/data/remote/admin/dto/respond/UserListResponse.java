package com.example.travelplanning.data.remote.admin.dto.respond;

import com.example.travelplanning.data.model.User;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<User> list;
}
