package com.example.travelplanning.viewmodel.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.repository.auth.AuthRepository;
public class MainViewModel extends AndroidViewModel {
    private final AuthRepository authRepo;
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application){
        super(application);
        authRepo = new AuthRepository(application);
    }

    public void checkIsLoggedIn(){
        if (authRepo.isLoggedIn()){

        }
    }

}
