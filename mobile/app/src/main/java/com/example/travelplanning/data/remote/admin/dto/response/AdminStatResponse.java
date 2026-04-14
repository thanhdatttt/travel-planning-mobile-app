package com.example.travelplanning.data.remote.admin.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class AdminStatResponse {
    List<StatPointDTO> signInData;
    List<StatPointDTO> reviewData;
    UserCountDTO counts;

    @Data
    public static class StatPointDTO { String label; int value; }
    @Data
    public static class UserCountDTO { int user; int moderator; int admin; }
}
