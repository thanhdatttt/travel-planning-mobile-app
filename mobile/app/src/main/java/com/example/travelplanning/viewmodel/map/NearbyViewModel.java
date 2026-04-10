package com.example.travelplanning.viewmodel.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelplanning.data.model.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NearbyViewModel extends ViewModel {

    private final MutableLiveData<String> selectedCategoryIcon = new MutableLiveData<>(null);
    
    // Nguồn dữ liệu thô
    private List<Location> rawMapData = new ArrayList<>();
    private List<Location> rawCategoryData = new ArrayList<>();
    private GeoPoint currentUserLocation = null;

    // Dữ liệu xuất ra UI
    private final MutableLiveData<List<Location>> mapMarkers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Location>> panelList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> displaySearchNames = new MutableLiveData<>(new ArrayList<>());

    public LiveData<String> getSelectedCategoryIcon() { return selectedCategoryIcon; }
    public LiveData<List<Location>> getMapMarkers() { return mapMarkers; }
    public LiveData<List<Location>> getPanelList() { return panelList; }
    public LiveData<List<String>> getDisplaySearchNames() { return displaySearchNames; }

    // 1. Nhận data từ thao tác kéo Map
    public void setMapData(List<Location> locations, GeoPoint userLocation) {
        this.rawMapData = locations != null ? locations : new ArrayList<>();
        this.currentUserLocation = userLocation;
        updateUI();
    }

    // 2. Nhận data từ thao tác bấm Category
    public void setCategoryPanelData(List<Location> locations, GeoPoint userLocation) {
        this.rawCategoryData = locations != null ? locations : new ArrayList<>();
        this.currentUserLocation = userLocation;
        updateUI();
    }

    public void toggleCategory(String iconName) {
        String current = selectedCategoryIcon.getValue();
        if (current != null && current.equals(iconName)) {
            selectedCategoryIcon.setValue(null);
        } else {
            selectedCategoryIcon.setValue(iconName);
        }
        updateUI();
    }

    private void updateUI() {
        updateMapMarkers();
        updatePanelList();
    }

    // Map chỉ quan tâm radius (rawMapData) và lọc theo Category
    private void updateMapMarkers() {
        String selectedIcon = selectedCategoryIcon.getValue();
        List<Location> filteredMap = new ArrayList<>();
        
        if (selectedIcon == null || selectedIcon.equals("CLEAR_ALL")) {
            filteredMap.addAll(rawMapData);
        } else {
            for (Location loc : rawMapData) {
                if (selectedIcon.equals(loc.getCategoryIcon())) filteredMap.add(loc);
            }
        }
        mapMarkers.setValue(filteredMap);
    }

    // Panel đảm bảo ưu tiên số lượng và sắp xếp khoảng cách
    private void updatePanelList() {
        String selectedIcon = selectedCategoryIcon.getValue();
        List<Location> sourceList;

        if (selectedIcon == null || selectedIcon.equals("CLEAR_ALL")) {
            // Không chọn Category -> Lấy top 10 từ Map Data
            sourceList = new ArrayList<>(rawMapData);
        } else {
            // Đã chọn Category -> Lấy từ Category Data (không bị giới hạn radius) để có đủ 10 cái
            sourceList = new ArrayList<>();
            for (Location loc : rawCategoryData) {
                if (selectedIcon.equals(loc.getCategoryIcon())) sourceList.add(loc);
            }
        }

        // Luôn sắp xếp ưu tiên gần nhất
        if (currentUserLocation != null && !sourceList.isEmpty()) {
            Collections.sort(sourceList, (l1, l2) -> {
                double d1 = currentUserLocation.distanceToAsDouble(new GeoPoint(l1.getLatitude(), l1.getLongitude()));
                double d2 = currentUserLocation.distanceToAsDouble(new GeoPoint(l2.getLatitude(), l2.getLongitude()));
                return Double.compare(d1, d2);
            });
        }

        int limit = Math.min(sourceList.size(), 10);
        panelList.setValue(new ArrayList<>(sourceList.subList(0, limit)));
    }

    public int calculateRadiusFromZoom(double zoomLevel) {
        if (zoomLevel >= 17) return 500;
        if (zoomLevel >= 15) return 2000;
        if (zoomLevel >= 13) return 5000;
        if (zoomLevel >= 11) return 15000;
        return 50000;
    }
}