package com.example.travelplanning.viewmodel.moderator;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.repository.moderator.ModeratorRepository;
import com.example.travelplanning.data.remote.report.dto.response.ReportResponse;



import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ModeratorReviewViewModel extends AndroidViewModel {
    private final ModeratorRepository repository;

    private final MutableLiveData<List<Report>> reports = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String currentTargetType = null;
    private int currentOffset = 0;
    private final int LIMIT = 20;
    private boolean isLastPage = false;

    public ModeratorReviewViewModel(Application app) {
        super(app);
        this.repository = new ModeratorRepository(app);
    }

    public void setFilterType(String targetType) {
        this.currentTargetType = targetType;
        fetchReports(false);
    }

    public void fetchReports(boolean isLoadMore) {
        if (Boolean.TRUE.equals(isLoading.getValue()) || (isLoadMore && isLastPage)) return;

        if (!isLoadMore) {
            currentOffset = 0;
            isLastPage = false;
        }
        isLoading.setValue(true);

        repository.getReports(
                currentTargetType,
                currentOffset,
                LIMIT,
                new ModeratorRepository.ModeratorCallback<List<Report>>() {
                    @Override
                    public void onSuccess(List<Report> data) {
                        isLoading.setValue(false);

                        List<Report> currentList = isLoadMore ? reports.getValue() : new ArrayList<>();
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
        repository.dismissReport(reportId, new ModeratorRepository.ModeratorCallback<Report>() {
            @Override
            public void onSuccess(Report data) {
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