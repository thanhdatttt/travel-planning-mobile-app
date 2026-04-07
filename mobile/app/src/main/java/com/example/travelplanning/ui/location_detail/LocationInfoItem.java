package com.example.travelplanning.ui.location;

import lombok.Getter;

@Getter
public class LocationInfoItem {
    private int iconRes;
    private String title;
    private String content;
    private int contentColor; // Để đổi màu cho "Đang mở cửa" hoặc "Link Web"

    public LocationInfoItem(int iconRes, String title, String content, int contentColor) {
        this.iconRes = iconRes;
        this.title = title;
        this.content = content;
        this.contentColor = contentColor;
    }
}