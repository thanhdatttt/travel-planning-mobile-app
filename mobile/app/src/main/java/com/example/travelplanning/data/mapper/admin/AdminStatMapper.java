package com.example.travelplanning.data.mapper.admin;

import com.example.travelplanning.data.remote.admin.dto.response.AdminStatResponse;
import java.util.ArrayList;
import java.util.List;

public class AdminStatMapper {
    public static List<Float> mapToFloatList(List<AdminStatResponse.StatPointDTO> dtos) {
        List<Float> values = new ArrayList<>();
        if (dtos == null) return values;
        for (AdminStatResponse.StatPointDTO dto : dtos) {
            values.add((float) dto.getValue());
        }
        return values;
    }
}