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

    private final MutableLiveData<List<Location>> nearbyLocations = new MutableLiveData<>(); // Cho Nearby
    private final MutableLiveData<List<Location>> searchResults = new MutableLiveData<>();   // Cho Search

    private final MutableLiveData<Boolean> hasMoreData = new MutableLiveData<>(false);       // Check Load More
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

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
    public void searchLocations(String query, int page) {
        isLoading.setValue(true);
        locationRepository.searchLocations(query, null, null, page, 10, new LocationRepository.LocationSearchCallback() {
            @Override
            public void onSuccess(List<Location> data, MetaResponse meta) {
                isLoading.setValue(false);

                searchResults.setValue(data);

                 hasMoreData.setValue(meta.getPage() < meta.getTotalPages());
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}