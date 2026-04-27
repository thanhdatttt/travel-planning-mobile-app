package com.example.travelplanning.viewmodel.admin;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.repository.admin.AdminRepository;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class AdminLocationViewModel extends AndroidViewModel {
    private final AdminRepository adminRepository;
    private final MutableLiveData<List<Location>> locations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String searchQuery = "";
    private String sortBy = "name";
    private String sortOrder = "asc";
    private int minPrice = 1;
    private int maxPrice = 4;
    private int minRating = 0;
    private int maxRating = 5;
    private List<String> categoryId;
    private boolean isDeleted;
    private int currentOffset = 0;
    private final int LIMIT = 20;
    private boolean isLastPage = false;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public AdminLocationViewModel(Application app) {
        super(app);
        this.adminRepository = new AdminRepository(app);
        categoryId = new ArrayList<>();
    }

    private void debounceFetch() {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        searchRunnable =  () -> fetchLocations(false);
        searchHandler.postDelayed(searchRunnable, 300);
    }

    public void onSearchQueryChanged(String query) {
        this.searchQuery = query;
        debounceFetch();
    }

    public void fetchLocations(boolean isLoadMore) {
        if (Boolean.TRUE.equals(isLoading.getValue()) || (isLoadMore && isLastPage)) return;

        if (!isLoadMore) {
            currentOffset = 0;
            isLastPage = false;
        }
        isLoading.setValue(true);

        adminRepository.getAllLocations(
                searchQuery,
                sortBy,
                sortOrder,
                minPrice,
                maxPrice,
                minRating,
                maxRating,
                categoryId,
                isDeleted,
                currentOffset,
                LIMIT,
                new AdminRepository.AdminCallback<List<Location>>() {
                    @Override
                    public void onSuccess(List<Location> data) {
                        isLoading.setValue(false);

                        List<Location> currentList = isLoadMore ? locations.getValue() : new ArrayList<>();
                        if (currentList == null) currentList = new ArrayList<>();
                        if (data.size() < LIMIT) isLastPage = true;
                        currentList.addAll(data);
                        locations.setValue(currentList);
                        currentOffset += data.size();
                    }

                    @Override
                    public void onError(String err) {
                        isLoading.setValue(false);
                        error.setValue(err);
                    }
                }
        );
    }

    public void applyFilters(int minP, int maxP, int minR, int maxR, List<String> catId, String sort, String order, boolean isDeleted) {
        this.minPrice = Math.max(1, minP);
        this.maxPrice = Math.min(4, maxP);
        this.minRating = minR;
        this.maxRating = maxR;
        this.categoryId = catId;
        this.sortBy = sort;
        this.sortOrder = order;
        this.isDeleted = isDeleted;
        fetchLocations(false);
    }

    public void resetFilters() {
        this.searchQuery = "";
        this.minPrice = 1;
        this.maxPrice = 4;
        this.minRating = 0;
        this.maxRating = 5;
        this.categoryId = new ArrayList<>();
        this.sortBy = "name";
        this.sortOrder = "asc";
        fetchLocations(false);
    }

    public void updateLocation(Location location) {
        isLoading.setValue(true);
        adminRepository.updateLocation(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getPhone(),
                location.getPriceLevel(),
                location.getAvgRating(),
                location.getImageUrl(),
                location.getCategoryName(),
                new AdminRepository.AdminCallback<Location>() {
                    @Override
                    public void onSuccess(Location updatedLocation) {
                        isLoading.setValue(false);
                        // Cập nhật item trong list hiện tại
                        List<Location> currentList = locations.getValue();
                        if (currentList != null) {
                            for (int i = 0; i < currentList.size(); i++) {
                                if (currentList.get(i).getId().equals(updatedLocation.getId())) {
                                    currentList.set(i, updatedLocation);
                                    break;
                                }
                            }
                            locations.setValue(currentList); // Kích hoạt Observer cập nhật UI
                        }
                    }

                    @Override
                    public void onError(String err) {
                        isLoading.setValue(false);
                        error.setValue(err);
                    }
                }
        );
    }

    public void deleteLocation(Location location) {
        isLoading.setValue(true);
        adminRepository.softDeleteLocation(location.getId(), true, new AdminRepository.AdminCallback<Location>() {
            @Override
            public void onSuccess(Location data) {
                isLoading.setValue(false);
                List<Location> currentList = locations.getValue();
                if (currentList != null) {
                    currentList.remove(location);
                    locations.setValue(currentList);
                }
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void createLocation(Location location) {
        isLoading.setValue(true);
        adminRepository.createLocation(location, new AdminRepository.AdminCallback<Location>() {
            @Override
            public void onSuccess(Location newLocation) {
                isLoading.setValue(false);
                List<Location> currentList = locations.getValue();
                if (currentList != null) {
                    currentList.add(0, newLocation);
                    locations.setValue(currentList);
                }
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void createLocationWithPhotos(Location location, List<Uri> photoUris) {
        if (photoUris == null || photoUris.isEmpty()) {
            createLocation(location);
            return;
        }

        isLoading.setValue(true);

        adminRepository.uploadLocationPhotos(getApplication().getApplicationContext(), photoUris, new AdminRepository.AdminCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> uploadedUrls) {

                List<Photo> photos = new ArrayList<>();
                for (int i = 0; i < uploadedUrls.size(); i++) {
                    photos.add(new Photo(null, uploadedUrls.get(i), i == 0));
                }

                location.setPhotos(photos);

                createLocation(location);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue("Failed to upload photos: " + err);
            }
        });
    }
}