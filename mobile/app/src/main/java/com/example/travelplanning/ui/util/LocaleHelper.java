package com.example.travelplanning.ui.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper extends ContextWrapper {

    public LocaleHelper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = prefs.getString("lang", "en");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();
        configuration.setLocale(locale);

        // This is the non-deprecated way for API 24+
        context = context.createConfigurationContext(configuration);

        return new LocaleHelper(context);
    }
}