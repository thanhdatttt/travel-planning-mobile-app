package com.example.travelplanning.core.util;

import android.content.Context;

import com.example.travelplanning.R;

import java.util.HashMap;
import java.util.Map;

public class AndroidStringProvider implements StringProvider {
    private final Context context;
    private static final Map<String, Integer> categoryMap = new HashMap<>();

    static {
        categoryMap.put("food_and_drink", R.string.cat_food_and_drink);
        categoryMap.put("accommodation", R.string.cat_accommodation);
        categoryMap.put("attractions", R.string.cat_attractions);
        categoryMap.put("shopping", R.string.cat_shopping);
        categoryMap.put("services", R.string.cat_services);
    }

    public AndroidStringProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getString(String slug) {
        Integer resId = categoryMap.get(slug);
        if (resId != null) {
            return context.getString(resId);
        }
        return "Chưa phân loại";
    }
}