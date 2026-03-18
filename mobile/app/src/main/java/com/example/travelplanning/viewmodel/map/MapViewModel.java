package com.example.travelplanning.viewmodel.map;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;
import com.example.travelplanning.data.repository.map.MapRepository;

import java.util.List;

public class MapViewModel extends AndroidViewModel {

    private final MapRepository repository;
    private final MutableLiveData<PhotonResponse> searchResults = new MutableLiveData<>();
    private final MutableLiveData<List<PhotonResponse.Feature>> autocompleteResults = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MapViewModel(@NonNull Application application) {
        super(application);
        repository = new MapRepository(application);
    }

    public LiveData<PhotonResponse> getSearchResults() { return searchResults; }
    public LiveData<List<PhotonResponse.Feature>> getAutocompleteResults() { return autocompleteResults; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void performSearch(String keyword, double lat, double lon, String bbox) {
        repository.searchPlaces(keyword, lat, lon, bbox, new MapRepository.PlaceCallback<PhotonResponse>() {
            @Override
            public void onSuccess(PhotonResponse data) { searchResults.postValue(data); }
            @Override
            public void onError(String error) { errorMessage.postValue(error); }
        });
    }

    public void fetchAutocomplete(String keyword, double lat, double lon, String bbox) {
        repository.searchPlaces(keyword, lat, lon, bbox, new MapRepository.PlaceCallback<PhotonResponse>() {
            @Override
            public void onSuccess(PhotonResponse data) {
                if (data != null && data.features != null) {
                    autocompleteResults.postValue(data.features);
                }
            }
            @Override
            public void onError(String error) {}
        });
    }
}