package com.example.travelplanning.viewmodel.moderator;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.moderator.LocationReport;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.repository.admin.AdminRepository;
import com.example.travelplanning.data.repository.moderator.ModeratorRepository;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ModeratorLocationViewModel extends AndroidViewModel {
    private final ModeratorRepository repository;

    private final MutableLiveData<List<LocationReport>> reports = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private int currentOffset = 0;
    private final int LIMIT = 20;
    private boolean isLastPage = false;

    public ModeratorLocationViewModel(Application app) {
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

        repository.getReportsLocation(
                currentOffset,
                LIMIT,
                new ModeratorRepository.ModeratorCallback<List<LocationReport>>() {
                    @Override
                    public void onSuccess(List<LocationReport> data) {
                        isLoading.setValue(false);

                        List<LocationReport> currentList = isLoadMore ? reports.getValue() : new ArrayList<>();
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