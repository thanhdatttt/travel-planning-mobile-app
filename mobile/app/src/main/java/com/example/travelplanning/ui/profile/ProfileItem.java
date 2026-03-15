package com.example.travelplanning.ui.profile;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ProfileItem {
    private String label;
    private String value;
    private String fieldKey;
    private boolean isEditable;
}