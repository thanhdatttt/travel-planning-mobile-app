package com.example.travelplanning.ui.util;

import android.content.Context;
import com.example.travelplanning.R;

public class CategoryHelper {

    public static String getCategoryName(Context context, String slug) {
        if (slug == null) return context.getString(R.string.cat_unknown);
        
        switch (slug.toLowerCase()) {
            case "food_and_drink": 
                return context.getString(R.string.cat_food_and_drink);
            case "accommodation": 
                return context.getString(R.string.cat_accommodation);
            case "attractions": 
                return context.getString(R.string.cat_attractions);
            case "shopping": 
                return context.getString(R.string.cat_shopping);
            case "services": 
                return context.getString(R.string.cat_services);
            default: 
                return context.getString(R.string.cat_unknown);
        }
    }
}