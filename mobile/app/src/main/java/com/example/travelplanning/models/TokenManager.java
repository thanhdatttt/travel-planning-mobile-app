package com.example.travelplanning.models;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    public static void saveTokens(Context context, String accessToken, String refreshToken) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(ACCESS_TOKEN, accessToken)
                .putString(REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN, null);
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(REFRESH_TOKEN, null);
    }
}
