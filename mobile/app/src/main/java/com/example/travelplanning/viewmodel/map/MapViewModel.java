package com.example.travelplanning.viewmodel.map;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    private final MutableLiveData<List<Pair<String, GeoPoint>>> mapData = new MutableLiveData<>();

    public LiveData<List<Pair<String, GeoPoint>>> getMapData() {
        return mapData;
    }

    public void loadSampleData() {
        List<Pair<String, GeoPoint>> sampleData = new ArrayList<>();


        sampleData.add(new Pair<>("Đại học KHTN (Cơ sở 1)", new GeoPoint(10.762622, 106.681305)));
        sampleData.add(new Pair<>("Chợ Bến Thành", new GeoPoint(10.7725, 106.6980)));
        sampleData.add(new Pair<>("Bưu điện Thành phố", new GeoPoint(10.7798, 106.6990)));

        mapData.setValue(sampleData);
    }
}