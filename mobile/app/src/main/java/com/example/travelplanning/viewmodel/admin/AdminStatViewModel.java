package com.example.travelplanning.viewmodel.admin;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.remote.admin.dto.response.AdminStatResponse;
import com.example.travelplanning.data.repository.admin.AdminRepository;

import java.util.Calendar;

import lombok.Getter;

@Getter
public class AdminStatViewModel extends AndroidViewModel {
    private final AdminRepository repository;
    private final MutableLiveData<AdminStatResponse> stats = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedMonth = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>();

    public AdminStatViewModel(Application app) {
        super(app);
        this.repository = new AdminRepository(app);

        Calendar cal = Calendar.getInstance();
        selectedMonth.setValue(cal.get(Calendar.MONTH) + 1);
        selectedYear.setValue(cal.get(Calendar.YEAR));
    }

    public void fetchStats() {
        Integer m = selectedMonth.getValue();
        Integer y = selectedYear.getValue();
        if (m == null || y == null) return;
        repository.getStatistics(m, y, new AdminRepository.AdminCallback<AdminStatResponse>() {
            @Override
            public void onSuccess(AdminStatResponse data) { stats.setValue(data); }
            @Override
            public void onError(String err) { error.setValue(err); }
        });
    }

    public String[] getYearList() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int startYear = 2000;
        String[] years = new String[currentYear - startYear + 1];
        for (int i = 0; i <= currentYear - startYear; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        return years;
    }

    public String[] getMonthList() {
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) months[i] = String.valueOf(i + 1);
        return months;
    }

    public void setDate(int month, int year) {
        selectedMonth.setValue(month);
        selectedYear.setValue(year);
        fetchStats();
    }
}