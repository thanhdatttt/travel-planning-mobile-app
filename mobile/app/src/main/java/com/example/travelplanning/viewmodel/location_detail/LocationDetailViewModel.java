package com.example.travelplanning.viewmodel.location_detail;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.repository.location.LocationRepository;

import org.jetbrains.annotations.NotNull;

public class LocationDetailViewModel extends AndroidViewModel {
    private final LocationRepository repository;
    private final MutableLiveData<Location> locationDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LocationDetailViewModel(@NotNull Application application) {
        super(application);
        this.repository = new LocationRepository(application);
    }

    public LiveData<Location> getLocationDetail() { return locationDetail; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void fetchDetail(String id) {
        isLoading.setValue(true);
        repository.getLocationById(id, new LocationRepository.LocationDetailCallback() {
            @Override
            public void onSuccess(Location data) {
                locationDetail.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }
}