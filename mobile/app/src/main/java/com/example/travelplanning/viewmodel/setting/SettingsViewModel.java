package com.example.travelplanning.viewmodel.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _isDarkMode = new MutableLiveData<>();
    public LiveData<Boolean> isDarkMode = _isDarkMode;

    private final MutableLiveData<String> _currentLanguage = new MutableLiveData<>();
    public LiveData<String> currentLanguage = _currentLanguage;

    public void setDarkMode(boolean enabled) {
        _isDarkMode.setValue(enabled);
    }

    public void setLanguage(String langCode) {
        _currentLanguage.setValue(langCode);
    }
}