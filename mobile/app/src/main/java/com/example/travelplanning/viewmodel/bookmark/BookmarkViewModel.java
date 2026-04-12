package com.example.travelplanning.viewmodel.bookmark;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.repository.bookmark.BookmarkRepository;

import java.util.List;
import lombok.Getter;

@Getter
public class BookmarkViewModel extends AndroidViewModel {
    private final BookmarkRepository bookmarkRepository;

    private final MutableLiveData<List<Location>> bookmarkedLocations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBookmarkedStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> lastPage = new MutableLiveData<>(1);

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
        this.bookmarkRepository = new BookmarkRepository(application);
    }

    public void toggleBookmark(String locationId) {
        bookmarkRepository.toggleBookmark(locationId, new BookmarkRepository.BookmarkCallback<String>() {
            @Override
            public void onSuccess(String message, int lp) {
                isBookmarkedStatus.setValue(message.contains("successfully"));
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public void fetchAllBookmarks(int page) {
        isLoading.setValue(true);
        bookmarkRepository.getAllBookmarks(page, 10, new BookmarkRepository.BookmarkCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> data, int lp) {
                isLoading.setValue(false);
                bookmarkedLocations.setValue(data);
                lastPage.setValue(lp);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}