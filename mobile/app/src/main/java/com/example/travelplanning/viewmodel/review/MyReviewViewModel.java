package com.example.travelplanning.viewmodel.review;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.ReviewPagination;
import com.example.travelplanning.data.model.review.UserReview;
import com.example.travelplanning.data.model.review.UserReviewPagination;
import com.example.travelplanning.data.repository.profile.UserProfileRepository;
import com.example.travelplanning.data.repository.review.ReviewRepository;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class MyReviewViewModel extends AndroidViewModel {
    private final ReviewRepository repo;
    private final UserProfileRepository userRepo;
    private final MutableLiveData<List<UserReview>> reviews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private int currentPage = 1;
    private boolean isLastPage = false;

    public MyReviewViewModel(@NotNull Application application){
        super(application);
        repo = new ReviewRepository(application);
        userRepo = new UserProfileRepository(application);
    }

    public void fetchMyReviews(boolean isLoadMore) {
        if (isLoadMore) {
            if (isLastPage) return;
            currentPage++;
        } else {
            currentPage = 1;
            isLastPage = false;
        }

        isLoading.setValue(true);
        repo.getMyReviews(currentPage, new ReviewRepository.ReviewCallback<UserReviewPagination>() {
            @Override
            public void onSuccess(UserReviewPagination data) {
                List<UserReview> currentList = isLoadMore ? reviews.getValue() : new ArrayList<>();
                if (data.getUserReviews() != null) {
                    currentList.addAll(data.getUserReviews());
                }
                reviews.setValue(currentList);
                isLastPage = (currentPage >= data.getLastPage());
                isLoading.setValue(false);
            }
            @Override
            public void onError(String msg) {
                isLoading.setValue(false);
            }
        });
    }
}
