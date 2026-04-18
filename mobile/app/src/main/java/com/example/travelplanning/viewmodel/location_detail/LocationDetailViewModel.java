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
import com.example.travelplanning.data.repository.bookmark.BookmarkRepository;
import com.example.travelplanning.data.repository.location.LocationRepository;
import com.example.travelplanning.data.repository.profile.UserProfileRepository;
import com.example.travelplanning.data.repository.review.ReviewRepository;
import com.example.travelplanning.data.remote.review.dto.request.ReviewRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class LocationDetailViewModel extends AndroidViewModel {
    private final LocationRepository repository;
    private final ReviewRepository reviewRepo;
    private final BookmarkRepository bookmarkRepo;
    private final UserProfileRepository userRepo;
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

    //bookmark
    private final MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>(false);

    //review
    private final MutableLiveData<Boolean> reviewSubmissionSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canUserReview = new MutableLiveData<>(true);

    public LocationDetailViewModel(@NotNull Application application) {
        super(application);
        this.repository = new LocationRepository(application);
        this.reviewRepo = new ReviewRepository(application);
        this.bookmarkRepo = new BookmarkRepository(application);
        this.userRepo = new UserProfileRepository(application);
    }

    public LiveData<Location> getLocationDetail() { return locationDetail; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Integer> getCurrentPage() { return currentPage; }

    private final MutableLiveData<List<Location>> nearbyLocations = new MutableLiveData<>();
    public LiveData<List<Location>> getNearbyLocations() { return nearbyLocations; }

    public LiveData<List<Review>> getReviews() { return reviews; }
    public LiveData<List<RatingStat>> getReviewStats() { return reviewStats; }

    public LiveData<Boolean> getCanUserReview() { return canUserReview; }
    public LiveData<Boolean> getReviewSubmissionSuccess() { return reviewSubmissionSuccess; }

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
                List<Review> newList = data.getReviews();
                List<Review> currentList = isLoadMore ? reviews.getValue() : new ArrayList<>();

                if (newList != null) {
                    currentList.addAll(newList);
                }

                reviews.setValue(currentList);
                isLastPage = (currentReviewPage >= data.getLastPage());
                isLoading.setValue(false);

                String currentUserId = userRepo.getCurrentUserId();
                if (!isLoadMore && data.getReviews() != null) {
                    for (Review r : data.getReviews()) {
                        if (r.getUserId().equals(currentUserId)) {
                            canUserReview.setValue(false);
                            break;
                        }
                    }
                }
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

    public void toggleBookmark(String locationId) {
        bookmarkRepo.toggleBookmark(locationId, new BookmarkRepository.BookmarkCallback<String>() {
            @Override
            public void onSuccess(String message, int lp) {
                isBookmarked.setValue(message.contains("successfully"));
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
            }
        });
    }

    public void postReview(String title, String body, int rating, String locationId) {
        if (title.isEmpty() || body.isEmpty() || rating == 0) {
            error.setValue("Vui lòng nhập đầy đủ thông tin và đánh giá sao");
            return;
        }

        isLoading.setValue(true);
        ReviewRequest request = ReviewRequest.builder()
                .title(title)
                .body(body)
                .rating(rating)
                .locationId(locationId)
                .build();

        reviewRepo.createReview(request, new ReviewRepository.ReviewCallback<Review>() {
            @Override
            public void onSuccess(Review newReview) {
                isLoading.setValue(false);
                reviewSubmissionSuccess.setValue(true);

                // Cập nhật danh sách review hiện tại
                List<Review> currentReviews = reviews.getValue();
                if (currentReviews == null) currentReviews = new ArrayList<>();
                currentReviews.add(0, newReview); // Thêm vào đầu danh sách
                reviews.setValue(currentReviews);

                // Refresh lại stats (sao trung bình)
                fetchReviewStats(locationId);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.setValue(false);
                error.setValue(errorMessage);
            }
        });
    }

    // Thêm vào LocationDetailViewModel.java

    public void deleteReview(String reviewId, String locationId) {
        isLoading.setValue(true);
        reviewRepo.deleteReview(reviewId, new ReviewRepository.ReviewCallback<String>() {
            @Override
            public void onSuccess(String data) {
                List<Review> currentList = reviews.getValue();
                if (currentList != null) {
                    currentList.removeIf(review -> review.getId().equals(reviewId));
                    reviews.setValue(currentList);
                }

                canUserReview.setValue(true);

                fetchReviewStats(locationId);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
                isLoading.setValue(false);
            }
        });
    }

    public void checkBookmarkStatus(String locationId) {
        bookmarkRepo.checkBookmarkStatus(locationId, new BookmarkRepository.BookmarkCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isBookmarkedFromDb, int lp) {
                isBookmarked.setValue(isBookmarkedFromDb);
            }
            @Override
            public void onError(String msg) {
                isBookmarked.setValue(false);
            }
        });
    }
}