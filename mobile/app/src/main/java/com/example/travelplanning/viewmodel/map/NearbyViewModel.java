package com.example.travelplanning.viewmodel.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.travelplanning.data.model.location.Location;
import java.util.ArrayList;
import java.util.List;

public class NearbyViewModel extends ViewModel {

    // Trạng thái cho việc Lọc Category
    private final MutableLiveData<String> selectedCategoryIcon = new MutableLiveData<>(null);
    private final MutableLiveData<List<Location>> allCurrentLocations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Location>> filteredLocations = new MutableLiveData<>(new ArrayList<>());

    // Trạng thái cho việc Gợi ý Tìm kiếm
    private final MutableLiveData<List<Location>> currentDbSuggestions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> displaySearchNames = new MutableLiveData<>(new ArrayList<>());

    // --- GETTERS ---
    public LiveData<String> getSelectedCategoryIcon() { return selectedCategoryIcon; }
    public LiveData<List<Location>> getFilteredLocations() { return filteredLocations; }
    public LiveData<List<Location>> getCurrentDbSuggestions() { return currentDbSuggestions; }
    public LiveData<List<String>> getDisplaySearchNames() { return displaySearchNames; }

    // --- LOGIC LƯU DỮ LIỆU & LỌC ---
    public void setLocations(List<Location> locations) {
        allCurrentLocations.setValue(locations != null ? locations : new ArrayList<>());
        applyFilter();
    }

    public void toggleCategory(String iconName) {
        String current = selectedCategoryIcon.getValue();
        if (current != null && current.equals(iconName)) {
            selectedCategoryIcon.setValue(null); // Bấm lần 2 để hủy lọc
        } else {
            selectedCategoryIcon.setValue(iconName); // Chọn category mới
        }
        applyFilter();
    }

    private void applyFilter() {
        List<Location> all = allCurrentLocations.getValue();
        String selectedIcon = selectedCategoryIcon.getValue();

        if (all == null) return;

        if (selectedIcon == null) {
            // Nếu không lọc, trả về toàn bộ
            filteredLocations.setValue(new ArrayList<>(all));
        } else {
            // Nếu có lọc, tìm các địa điểm khớp icon
            List<Location> filtered = new ArrayList<>();
            for (Location loc : all) {
                if (selectedIcon.equals(loc.getCategoryIcon())) {
                    filtered.add(loc);
                }
            }
            filteredLocations.setValue(filtered);
        }
    }

    // --- LOGIC XỬ LÝ DỮ LIỆU TÌM KIẾM ---
    public void setSearchSuggestions(List<Location> locations) {
        currentDbSuggestions.setValue(locations != null ? locations : new ArrayList<>());
        
        List<String> names = new ArrayList<>();
        if (locations != null) {
            for (Location loc : locations) {
                String address = loc.getAddress() != null ? " - " + loc.getAddress() : "";
                names.add(loc.getName() + address);
            }
        }
        displaySearchNames.setValue(names);
    }

    // --- LOGIC TÍNH TOÁN BÁN KÍNH ---
    public int calculateRadiusFromZoom(double zoomLevel) {
        if (zoomLevel >= 17) return 500;       // Zoom rất cận: 500m
        if (zoomLevel >= 15) return 2000;      // Zoom gần: 2km
        if (zoomLevel >= 13) return 5000;      // Zoom trung bình: 5km
        if (zoomLevel >= 11) return 15000;     // Zoom xa: 15km
        return 50000;                          // Zoom rất xa: 50km
    }
}