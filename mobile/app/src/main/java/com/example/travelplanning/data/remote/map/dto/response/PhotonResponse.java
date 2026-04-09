package com.example.travelplanning.data.remote.map.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PhotonResponse {
    @SerializedName("features")
    public List<Feature> features;

    public static class Feature {
        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("properties")
        public Properties properties;
    }

    public static class Geometry {
        @SerializedName("coordinates")
        public List<Double> coordinates;
    }

    public static class Properties {
        @SerializedName("name")
        public String name;

        @SerializedName("city")
        public String city;

        @SerializedName("street")
        public String street;
    }
}
