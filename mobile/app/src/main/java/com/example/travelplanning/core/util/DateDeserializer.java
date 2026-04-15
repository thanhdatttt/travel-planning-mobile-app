package com.example.travelplanning.core.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateDeserializer implements JsonDeserializer<Date> {
    // ISO 8601 format from server
    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    // date only format
    private static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();
        try {
            // parse iso into date
            SimpleDateFormat isoParser = new SimpleDateFormat(ISO_8601_FORMAT, Locale.US);
            return isoParser.parse(dateString);
        } catch (ParseException e) {
            // Nếu format server khác đi một chút, thử parse kiểu chỉ có ngày
            try {
                return new SimpleDateFormat(DATE_ONLY_FORMAT, Locale.getDefault()).parse(dateString);
            } catch (ParseException e2) {
                throw new JsonParseException(e2);
            }
        }
    }
}