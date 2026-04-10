package com.example.travelplanning.viewmodel.location_detail;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.repository.location.LocationRepository;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocationDetailViewModel extends AndroidViewModel {
    private final LocationRepository repository;
    private final MutableLiveData<Location> locationDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    //location photo
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    private final int PAGE_SIZE = 2; // Hiển thị 4 ảnh mỗi trang

    public LocationDetailViewModel(@NotNull Application application) {
        super(application);
        this.repository = new LocationRepository(application);
    }

    public LiveData<Location> getLocationDetail() { return locationDetail; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Integer> getCurrentPage() { return currentPage; }

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

    public void nextPage() {
        Location loc = locationDetail.getValue();
        if (loc != null && loc.getPhotos() != null) {
            int totalPages = (int) Math.ceil((double) loc.getPhotos().size() / PAGE_SIZE);
            if (currentPage.getValue() < totalPages - 1) {
                currentPage.setValue(currentPage.getValue() + 1);
            }
        }
    }

    public void prevPage() {
        if (currentPage.getValue() > 0) {
            currentPage.setValue(currentPage.getValue() - 1);
        }
    }

    public void uploadPhoto(String locationId, Uri uri) {
        isLoading.setValue(true);
        repository.uploadLocationPhoto(locationId, uri, new LocationRepository.PhotoUploadCallback() {
            @Override
            public void onSuccess(Photo newPhoto) {
                isLoading.setValue(false);
                Location currentLoc = locationDetail.getValue();

                if (currentLoc != null && newPhoto != null) {
                    // Lấy danh sách ảnh cũ
                    List<Photo> photoList = currentLoc.getPhotos();
                    if (photoList == null) photoList = new ArrayList<>();

                    // Thêm ảnh mới vào danh sách
                    photoList.add(newPhoto);
                    currentLoc.setPhotos(photoList);

                    // Cập nhật lại LiveData.
                    locationDetail.setValue(currentLoc);
                }
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }
}