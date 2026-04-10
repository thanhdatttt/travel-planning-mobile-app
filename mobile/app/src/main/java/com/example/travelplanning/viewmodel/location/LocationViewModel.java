package com.example.travelplanning.viewmodel.location;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.repository.location.LocationRepository;

import java.util.List;

import lombok.Getter;

@Getter
public class LocationViewModel extends AndroidViewModel {

    private final LocationRepository locationRepository;

    private final MutableLiveData<List<Location>> nearbyLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> categoryPanelLocations = new MutableLiveData<>();
    
    private final MutableLiveData<List<Location>> searchResults = new MutableLiveData<>();   
    private final MutableLiveData<Boolean> hasMoreData = new MutableLiveData<>(false);  
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Location> locationDetail = new MutableLiveData<>();
    public LocationViewModel(@NonNull Application application) {
        super(application);
        this.locationRepository = new LocationRepository(application);
    }

    public void fetchNearbyLocations(double lat, double lng, Integer radius, Integer categoryId) {
        isLoading.setValue(true);
        locationRepository.getNearbyLocations(lat, lng, radius, categoryId, new LocationRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<Location> data) {
                isLoading.setValue(false);
                nearbyLocations.setValue(data);
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void fetchPanelLocationsByCategory(double lat, double lng, String categoryIcon) {
        isLoading.setValue(true);
        locationRepository.getNearbyLocations(lat, lng, null, null, new LocationRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<Location> data) {
                isLoading.setValue(false);
                categoryPanelLocations.setValue(data);
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void searchLocations(String query, Integer categoryId, Integer priceLevel, int page, int limit) {
        isLoading.setValue(true);
        locationRepository.searchLocations(query, categoryId, priceLevel, page, limit, new LocationRepository.LocationSearchCallback() {
            @Override
            public void onSuccess(List<Location> data, MetaResponse meta) {
                isLoading.setValue(false);
                List<Location> currentList = searchResults.getValue();
                if (page == 1 || currentList == null) searchResults.setValue(data);
                else {
                    currentList.addAll(data);
                    searchResults.setValue(currentList);
                }
                if (meta != null) hasMoreData.setValue(meta.getPage() < meta.getTotalPages());
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void fetchDetail(String id) {
        isLoading.setValue(true);
        locationRepository.getLocationById(id, new LocationRepository.LocationDetailCallback() {
            @Override
            public void onSuccess(Location data) {
                isLoading.setValue(false);
                locationDetail.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}