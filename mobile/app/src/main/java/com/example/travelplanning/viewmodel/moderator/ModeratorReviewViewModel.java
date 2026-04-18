package com.example.travelplanning.viewmodel.moderator;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.moderator.ReviewReport;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.repository.moderator.ModeratorRepository;



import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ModeratorReviewViewModel extends AndroidViewModel {
    private final ModeratorRepository repository;

    private final MutableLiveData<List<ReviewReport>> reports = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int currentOffset = 0;
    private final int LIMIT = 20;
    private boolean isLastPage = false;

    public ModeratorReviewViewModel(Application app) {
        super(app);
        this.repository = new ModeratorRepository(app);
    }
    public void fetchReports(boolean isLoadMore) {
        if (Boolean.TRUE.equals(isLoading.getValue()) || (isLoadMore && isLastPage)) return;

        if (!isLoadMore) {
            currentOffset = 0;
            isLastPage = false;
        }
        isLoading.setValue(true);

        repository.getReportsReview(
                currentOffset,
                LIMIT,
                new ModeratorRepository.ModeratorCallback<List<ReviewReport>>() {
                    @Override
                    public void onSuccess(List<ReviewReport> data) {
                        isLoading.setValue(false);

                        List<ReviewReport> currentList = isLoadMore ? reports.getValue() : new ArrayList<>();
                        if (currentList == null) currentList = new ArrayList<>();
                        if (data.size() < LIMIT) isLastPage = true;

                        currentList.addAll(data);
                        reports.setValue(currentList);
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

    public void banUserFromReport(String userId) {
        isLoading.setValue(true);
        repository.toggleUserBan(userId, true, new ModeratorRepository.ModeratorCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile data) {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void dismissReport(String reportId) {
        isLoading.setValue(true);
        repository.dismissReport(reportId, new ModeratorRepository.ModeratorCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                isLoading.setValue(false);
                fetchReports(false);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }
}