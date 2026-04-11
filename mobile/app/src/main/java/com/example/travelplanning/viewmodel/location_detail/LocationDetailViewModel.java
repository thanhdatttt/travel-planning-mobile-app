package com.example.travelplanning.viewmodel.location_detail;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.ReviewPagination;
import com.example.travelplanning.data.repository.location.LocationRepository;
import com.example.travelplanning.data.repository.review.ReviewRepository;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class LocationDetailViewModel extends AndroidViewModel {
    private final LocationRepository repository;
    private final ReviewRepository reviewRepo;
    private final MutableLiveData<Location> locationDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    //location photo
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    private final int PAGE_SIZE = 2; // Hiển thị 4 ảnh mỗi trang

    //review
    private final MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private int currentReviewPage = 1;
    private boolean isLastPage = false;
    private final MutableLiveData<List<RatingStat>> reviewStats = new MutableLiveData<>();
    public LiveData<List<Review>> getReviews() { return reviews; }
    public LiveData<List<RatingStat>> getReviewStats() { return reviewStats; }

    public LocationDetailViewModel(@NotNull Application application) {
        super(application);
        this.repository = new LocationRepository(application);
        this.reviewRepo = new ReviewRepository(application);
    }

    public LiveData<Location> getLocationDetail() { return locationDetail; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Integer> getCurrentPage() { return currentPage; }

    private final MutableLiveData<List<Location>> nearbyLocations = new MutableLiveData<>();
    public LiveData<List<Location>> getNearbyLocations() { return nearbyLocations; }

    public void fetchDetail(String id) {
        isLoading.setValue(true);
        repository.getLocationById(id, new LocationRepository.LocationDetailCallback() {
            @Override
            public void onSuccess(Location data) {
                locationDetail.setValue(data);
                fetchReviews(id, false);
                fetchReviewStats(id);

                if (data.getLatitude() != null && data.getLongitude() != null) {
                    fetchNearbyLocations(data.getLatitude(), data.getLongitude(), 5000, null);
                }

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

    //REVIEW----------
    public void fetchReviews(String id, boolean isLoadMore) {
        if (isLoadMore) {
            if (isLastPage) return;
            currentReviewPage++;
        } else {
            currentReviewPage = 1;
            isLastPage = false;
        }

        isLoading.setValue(true);
        reviewRepo.getReviewsByLocation(id, currentReviewPage, 5, new ReviewRepository.ReviewCallback<ReviewPagination>() {
            @Override
            public void onSuccess(ReviewPagination data) {
                List<Review> newList = data.getReviews(); // Không còn lỗi getReviews()
                List<Review> currentList = isLoadMore ? reviews.getValue() : new ArrayList<>();

                if (newList != null) {
                    currentList.addAll(newList);
                }

                reviews.setValue(currentList);
                isLastPage = (currentReviewPage >= data.getLastPage());
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void fetchReviewStats(String locationId) {
        reviewRepo.getReviewStats(locationId, new ReviewRepository.ReviewCallback<List<RatingStat>>() {
            @Override
            public void onSuccess(List<RatingStat> data) {
                reviewStats.setValue(data);
            }
            @Override
            public void onError(String msg) {
                error.setValue(msg);
            }
        });
    }

    public void fetchNearbyLocations(double lat, double lng, Integer radius, Integer categoryId) {
        isLoading.setValue(true);
        repository.getNearbyLocations(lat, lng, radius, categoryId, new LocationRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<Location> data) {
                isLoading.setValue(false);
                if (data != null) {
                    //lấy 8 địa điểm đầu tiên
                    List<Location> limitedData = data.size() > 8
                            ? new ArrayList<>(data.subList(0, 8))
                            : data;

                    nearbyLocations.setValue(limitedData);
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