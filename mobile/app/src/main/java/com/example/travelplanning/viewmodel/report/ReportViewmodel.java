package com.example.travelplanning.viewmodel.report;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.data.repository.report.ReportRepository;

import lombok.Getter;

@Getter
public class ReportViewmodel extends AndroidViewModel {
    private final ReportRepository repository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reportSuccess = new MutableLiveData<>();

    public ReportViewmodel(Application app) {
        super(app);
        this.repository = new ReportRepository(app);
    }

    public void reportReview(String reviewId, String reason) {
        isLoading.setValue(true);
        repository.reportReview(reviewId, reason, new ReportRepository.ReportCallback<Report>() {
            @Override
            public void onSuccess(Report data) {
                isLoading.setValue(false);
                reportSuccess.setValue(true);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void reportLocation(String locationId, String reason) {
        isLoading.setValue(true);
        repository.reportLocation(locationId, reason, new ReportRepository.ReportCallback<Report>() {
            @Override
            public void onSuccess(Report data) {
                isLoading.setValue(false);
                reportSuccess.setValue(true);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }

    public void reportItinerary(String itineraryId, String reason) {
        isLoading.setValue(true);
        repository.reportItinerary(itineraryId, reason, new ReportRepository.ReportCallback<Report>() {
            @Override
            public void onSuccess(Report data) {
                isLoading.setValue(false);
                reportSuccess.setValue(true);
            }

            @Override
            public void onError(String err) {
                isLoading.setValue(false);
                error.setValue(err);
            }
        });
    }
}