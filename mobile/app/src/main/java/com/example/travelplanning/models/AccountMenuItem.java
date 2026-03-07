package com.example.travelplanning.models;

public class AccountMenuItem {
    private String title;
    private int iconRes; // ID của ảnh trong drawable

    public AccountMenuItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }
    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
}
