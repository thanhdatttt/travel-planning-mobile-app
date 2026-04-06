package com.example.travelplanning.viewmodel.itinerary;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.remote.itinerary.dto.request.AddItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.ScheduleItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryItemNoteRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryRequest;
import com.example.travelplanning.data.repository.itinerary.ItineraryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItineraryViewModel extends AndroidViewModel {

    private final ItineraryRepository itineraryRepository;

    //  general states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // itinerary list states
    private final MutableLiveData<List<Itinerary>> userItineraries = new MutableLiveData<>();
    private final MutableLiveData<List<Itinerary>> publicItineraries = new MutableLiveData<>();
    private final MutableLiveData<MetaResponse> itineraryMeta = new MutableLiveData<>();

    // single itinerary state
    private final MutableLiveData<Itinerary> selectedItinerary = new MutableLiveData<>();

    public ItineraryViewModel(@NonNull Application application) {
        super(application);
        this.itineraryRepository = new ItineraryRepository(application);
    }

    // itinerary api calls
    public void fetchUserItineraries(int page, int limit) {
        isLoading.setValue(true);
        itineraryRepository.getUserItineraries(page, limit,
                new ItineraryRepository.ItineraryListCallback() {
                    @Override
                    public void onSuccess(List<Itinerary> data, MetaResponse meta) {
                        isLoading.setValue(false);
                        List<Itinerary> current = userItineraries.getValue();
                        if (page == 1 || current == null) {
                            userItineraries.setValue(data);
                        } else {
                            List<Itinerary> next = new ArrayList<>(current);
                            next.addAll(data);
                            userItineraries.setValue(next);
                        }
                        itineraryMeta.setValue(meta);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void fetchPublicItineraries(int page, int limit) {
        isLoading.setValue(true);
        itineraryRepository.getPublicItineraries(page, limit,
                new ItineraryRepository.ItineraryListCallback() {
                    @Override
                    public void onSuccess(List<Itinerary> data, MetaResponse meta) {
                        isLoading.setValue(false);
                        List<Itinerary> current = publicItineraries.getValue();
                        if (page == 1 || current == null) {
                            publicItineraries.setValue(data);
                        } else {
                            List<Itinerary> next = new ArrayList<>(current);
                            next.addAll(data);
                            publicItineraries.setValue(next);
                        }
                        itineraryMeta.setValue(meta);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void fetchItineraryById(String id) {
        isLoading.setValue(true);
        itineraryRepository.getItineraryById(id,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        selectedItinerary.setValue(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void createItinerary(String title, Date startDate, Date endDate) {
        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Please enter a trip title.");
            return;
        }

        isLoading.setValue(true);
        CreateItineraryRequest request = CreateItineraryRequest.builder()
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        itineraryRepository.createItinerary(request,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        prependToUserItineraries(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void updateItinerary(String id, String title, String description,
                                String privacy, Date startDate, Date endDate) {
        isLoading.setValue(true);
        UpdateItineraryRequest request = UpdateItineraryRequest.builder()
                .title(title)
                .description(description)
                .privacy(privacy)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        itineraryRepository.updateItinerary(id, request,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        replaceInUserItineraries(data);
                        syncSelectedItinerary(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void deleteItinerary(String id) {
        isLoading.setValue(true);
        itineraryRepository.deleteItinerary(id,
                new ItineraryRepository.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        isLoading.setValue(false);
                        removeFromUserItineraries(id);

                        Itinerary current = selectedItinerary.getValue();
                        if (current != null && current.getId().equals(id)) {
                            selectedItinerary.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void cloneItinerary(String id) {
        isLoading.setValue(true);
        itineraryRepository.cloneItinerary(id,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        prependToUserItineraries(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    // itinerary item api calls
    public void addItineraryItem(String itineraryId, String locationId, String note) {
        isLoading.setValue(true);
        AddItineraryItemRequest request = AddItineraryItemRequest.builder()
                .locationId(locationId)
                .note(note)
                .build();

        itineraryRepository.addItineraryItem(itineraryId, request,
                new ItineraryRepository.ItineraryItemCallback() {
                    @Override
                    public void onSuccess(ItineraryItem data) {
                        isLoading.setValue(false);
                        addItemToItinerary(itineraryId, data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void deleteItineraryItem(String itineraryId, String itemId) {
        isLoading.setValue(true);
        itineraryRepository.deleteItineraryItem(itineraryId, itemId,
                new ItineraryRepository.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        isLoading.setValue(false);
                        removeItemFromItinerary(itineraryId, itemId);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void scheduleItineraryItem(String itineraryId, String itemId, Date targetDate) {
        isLoading.setValue(true);
        ScheduleItineraryItemRequest request = ScheduleItineraryItemRequest.builder()
                .targetDate(targetDate)
                .build();

        itineraryRepository.scheduleItineraryItem(itineraryId, itemId, request,
                new ItineraryRepository.ItineraryItemCallback() {
                    @Override
                    public void onSuccess(ItineraryItem data) {
                        isLoading.setValue(false);
                        replaceItemInItinerary(itineraryId, data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void unscheduleItineraryItem(String itineraryId, String itemId) {
        isLoading.setValue(true);
        itineraryRepository.unscheduleItineraryItem(itineraryId, itemId,
                new ItineraryRepository.ItineraryItemCallback() {
                    @Override
                    public void onSuccess(ItineraryItem data) {
                        isLoading.setValue(false);
                        replaceItemInItinerary(itineraryId, data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void updateItineraryItemNote(String itineraryId, String itemId, String note) {
        isLoading.setValue(true);
        UpdateItineraryItemNoteRequest request = UpdateItineraryItemNoteRequest.builder()
                .note(note)
                .build();

        itineraryRepository.updateItineraryItemNote(itineraryId, itemId, request,
                new ItineraryRepository.ItineraryItemCallback() {
                    @Override
                    public void onSuccess(ItineraryItem data) {
                        isLoading.setValue(false);
                        replaceItemInItinerary(itineraryId, data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    // handler
    // insert created itinerary to top of list
    private void prependToUserItineraries(Itinerary created) {
        List<Itinerary> current = userItineraries.getValue();
        List<Itinerary> next = current != null ? new ArrayList<>(current) : new ArrayList<>();
        next.add(0, created);
        userItineraries.setValue(next);
    }

    // replace updated itinerary in list
    private void replaceInUserItineraries(Itinerary updated) {
        List<Itinerary> current = userItineraries.getValue();
        if (current == null) {
            userItineraries.setValue(new ArrayList<>(Collections.singletonList(updated)));
            return;
        }
        List<Itinerary> next = new ArrayList<>(current);
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).getId().equals(updated.getId())) {
                next.set(i, updated);
                break;
            }
        }
        userItineraries.setValue(next);
    }

    // remove deleted itinerary from list
    private void removeFromUserItineraries(String id) {
        List<Itinerary> current = userItineraries.getValue();
        if (current == null) return;
        List<Itinerary> next = new ArrayList<>(current);
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).getId().equals(id)) {
                next.remove(i);
                break;
            }
        }
        userItineraries.setValue(next);
    }

    // add new item to itinerary in list
    private void addItemToItinerary(String itineraryId, ItineraryItem newItem) {
        updateItineraryItems(itineraryId, items -> {
            List<ItineraryItem> next = new ArrayList<>(items);
            next.add(newItem);
            return next;
        });
    }

    // remove item from itinerary in list
    private void removeItemFromItinerary(String itineraryId, String itemId) {
        updateItineraryItems(itineraryId, items -> {
            List<ItineraryItem> next = new ArrayList<>(items);
            for (int i = 0; i < next.size(); i++) {
                if (next.get(i).getId().equals(itemId)) {
                    next.remove(i);
                    break;
                }
            }
            return next;
        });
    }

    // replace item in itinerary in list
    private void replaceItemInItinerary(String itineraryId, ItineraryItem updatedItem) {
        updateItineraryItems(itineraryId, items -> {
            List<ItineraryItem> next = new ArrayList<>(items);
            for (int i = 0; i < next.size(); i++) {
                if (next.get(i).getId().equals(updatedItem.getId())) {
                    next.set(i, updatedItem);
                    break;
                }
            }
            return next;
        });
    }

    //
    private void updateItineraryItems(String itineraryId, ItemListTransform transform) {
        List<Itinerary> current = userItineraries.getValue();
        if (current == null) return;

        List<Itinerary> next = new ArrayList<>(current);
        for (int i = 0; i < next.size(); i++) {
            Itinerary itinerary = next.get(i);
            if (itinerary.getId().equals(itineraryId)) {
                List<ItineraryItem> updatedItems = transform.apply(
                        itinerary.getItineraryItems() != null
                                ? itinerary.getItineraryItems()
                                : new ArrayList<>()
                );
                Itinerary rebuilt = Itinerary.builder()
                        .id(itinerary.getId())
                        .ownerId(itinerary.getOwnerId())
                        .title(itinerary.getTitle())
                        .description(itinerary.getDescription())
                        .privacy(itinerary.getPrivacy())
                        .startDate(itinerary.getStartDate())
                        .endDate(itinerary.getEndDate())
                        .createdAt(itinerary.getCreatedAt())
                        .updatedAt(itinerary.getUpdatedAt())
                        .itineraryItems(updatedItems)
                        .build();
                next.set(i, rebuilt);
                syncSelectedItinerary(rebuilt);
                break;
            }
        }
        userItineraries.setValue(next);
    }

    /** Refreshes selectedItinerary only when it holds the same ID as updated. */
    private void syncSelectedItinerary(Itinerary updated) {
        Itinerary current = selectedItinerary.getValue();
        if (current != null && current.getId().equals(updated.getId())) {
            selectedItinerary.setValue(updated);
        }
    }

    /** Functional interface for transforming an item list inside updateItineraryItems. */
    private interface ItemListTransform {
        List<ItineraryItem> apply(List<ItineraryItem> items);
    }
}