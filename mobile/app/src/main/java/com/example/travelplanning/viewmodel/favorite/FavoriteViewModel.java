package com.example.travelplanning.viewmodel.favorite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.repository.favorite.FavoriteRepository;
import com.example.travelplanning.data.repository.profile.UserProfileRepository;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class FavoriteViewModel extends AndroidViewModel {
    private final FavoriteRepository repository;
    private final UserProfileRepository userRepo;
    private final MutableLiveData<List<Itinerary>> favoriteTrips = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> lastPage = new MutableLiveData<>(1);
    private int currentPage = 1;
    private String currentUserId;

    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FavoriteRepository(application);
        userRepo = new UserProfileRepository(application);
    }

    public void fetchFavorites(boolean isRefresh) {
        if (isRefresh) currentPage = 1;

        isLoading.setValue(true);

        repository.getAllFavorites(currentPage, 10, new FavoriteRepository.FavoriteCallback<List<Itinerary>>() {
            @Override
            public void onSuccess(List<Itinerary> data, int lp) {
                isLoading.setValue(false);
                lastPage.setValue(lp);
                if (isRefresh) favoriteTrips.setValue(data);
                else {
                    List<Itinerary> current = favoriteTrips.getValue();
                    current.addAll(data);
                    favoriteTrips.setValue(current);
                }
            }
            @Override public void onError(String err) { isLoading.setValue(false); }
        });
    }

    public void loadNextPage() {
        if (currentPage < lastPage.getValue()) {
            currentPage++;
            fetchFavorites(false);
        }
    }

    public void fetchCurrentUserId(){
        currentUserId = userRepo.getCurrentUserId();
    }

}
