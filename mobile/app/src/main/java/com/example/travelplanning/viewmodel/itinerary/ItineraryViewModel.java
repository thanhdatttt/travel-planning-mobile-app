package com.example.travelplanning.viewmodel.itinerary;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.travelplanning.data.model.itinerary.HeaderItem;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryDisplayItem;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.model.itinerary.LocationItem;
import com.example.travelplanning.data.remote.core.MetaResponse;
import com.example.travelplanning.data.remote.itinerary.dto.request.AddItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.ScheduleItineraryItemRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryItemNoteRequest;
import com.example.travelplanning.data.remote.itinerary.dto.request.UpdateItineraryRequest;
import com.example.travelplanning.data.repository.itinerary.ItineraryRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItineraryViewModel extends AndroidViewModel {
    private final ItineraryRepository itineraryRepository;

    // format date
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    //  general states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // itinerary list states
    private final MutableLiveData<List<Itinerary>> userItineraries = new MutableLiveData<>();
    private final MutableLiveData<List<Itinerary>> publicItineraries = new MutableLiveData<>();
    private final MutableLiveData<MetaResponse> itineraryMeta = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cloneSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);

    // single itinerary state
    private final MutableLiveData<Itinerary> selectedItinerary = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addItemSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleteItemSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> scheduleItemSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> unscheduleItemSuccess = new MutableLiveData<>(false);

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
                .startDate(dateFormat.format(startDate))
                .endDate(dateFormat.format(endDate))
                .build();

        itineraryRepository.createItinerary(request,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        createSuccess.setValue(true);
                        prependToUserItineraries(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        createSuccess.setValue(false);
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
                .startDate(startDate != null ? dateFormat.format(startDate) : null)
                .endDate(endDate != null ? dateFormat.format(endDate) : null)
                .build();

        itineraryRepository.updateItinerary(id, request,
                new ItineraryRepository.ItineraryCallback() {
                    @Override
                    public void onSuccess(Itinerary data) {
                        isLoading.setValue(false);
                        updateSuccess.setValue(true);
                        replaceInUserItineraries(data);
                        syncSelectedItinerary(data);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        updateSuccess.setValue(false);
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
                        deleteSuccess.setValue(true);
                        removeFromUserItineraries(id);

                        Itinerary current = selectedItinerary.getValue();
                        if (current != null && current.getId().equals(id)) {
                            selectedItinerary.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        deleteSuccess.setValue(false);
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
                        cloneSuccess.setValue(true);
                        if (userItineraries.getValue() != null) {
                            prependToUserItineraries(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        cloneSuccess.setValue(false);
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
                        addItemSuccess.setValue(true);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        addItemSuccess.setValue(false);
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
                        deleteItemSuccess.setValue(true);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        deleteItemSuccess.setValue(false);
                        errorMessage.setValue(errorMsg);
                    }
                });
    }

    public void scheduleItineraryItem(String itineraryId, String itemId, Date targetDate) {
        isLoading.setValue(true);
        ScheduleItineraryItemRequest request = ScheduleItineraryItemRequest.builder()
                .targetDate(dateFormat.format(targetDate))
                .build();

        itineraryRepository.scheduleItineraryItem(itineraryId, itemId, request,
                new ItineraryRepository.ItineraryItemCallback() {
                    @Override
                    public void onSuccess(ItineraryItem data) {
                        isLoading.setValue(false);
                        replaceItemInItinerary(itineraryId, data);
                        scheduleItemSuccess.setValue(true);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        scheduleItemSuccess.setValue(false);
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
                        unscheduleItemSuccess.setValue(true);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isLoading.setValue(false);
                        unscheduleItemSuccess.setValue(false);
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

    // add new item to selected itinerary
    private void addItemToItinerary(String itineraryId, ItineraryItem newItem) {
        updateItemsInSelected(itineraryId, items -> {
            List<ItineraryItem> next = new ArrayList<>(items);
            next.add(newItem);
            return next;
        });
    }

    // remove item from selected itinerary
    private void removeItemFromItinerary(String itineraryId, String itemId) {
        updateItemsInSelected(itineraryId, items -> {
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
        updateItemsInSelected(itineraryId, items -> {
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

    // update items in itinerary
    private void updateItemsInSelected(String itineraryId, ItemListTransform transform) {
        Itinerary selected = selectedItinerary.getValue();

        if (selected == null || !selected.getId().equals(itineraryId)) return;

        // apply the transform to the full item list
        List<ItineraryItem> fullItems = selected.getItineraryItems() != null
                ? selected.getItineraryItems() : new ArrayList<>();
        List<ItineraryItem> updatedItems = transform.apply(fullItems);

        // rebuild and push to selectedItinerary
        Itinerary rebuilt = rebuildWithItems(selected, updatedItems);
        selectedItinerary.setValue(rebuilt);

        // patch the preview in userItineraries (keep only the first item)
        patchPreviewInUserItineraries(rebuilt);
    }

    // updates the matching entry in userItineraries to hold only the first item
    private void patchPreviewInUserItineraries(Itinerary fullItinerary) {
        List<Itinerary> current = userItineraries.getValue();
        if (current == null) return;

        List<Itinerary> next = new ArrayList<>(current);
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).getId().equals(fullItinerary.getId())) {
                List<ItineraryItem> previewItems =
                        fullItinerary.getItineraryItems() != null
                                && !fullItinerary.getItineraryItems().isEmpty()
                                ? Collections.singletonList(fullItinerary.getItineraryItems().get(0))
                                : new ArrayList<>();
                next.set(i, rebuildWithItems(next.get(i), previewItems));
                break;
            }
        }
        userItineraries.setValue(next);
    }

    // creates a copy of an Itinerary with a different items list
    private Itinerary rebuildWithItems(Itinerary source, List<ItineraryItem> items) {
        return Itinerary.builder()
                .id(source.getId())
                .ownerId(source.getOwnerId())
                .title(source.getTitle())
                .description(source.getDescription())
                .privacy(source.getPrivacy())
                .startDate(source.getStartDate())
                .endDate(source.getEndDate())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .user(source.getUser())
                .itineraryItems(items)
                .build();
    }

    /** Refreshes selectedItinerary only when it holds the same ID as updated. */
    private void syncSelectedItinerary(Itinerary updated) {
        Itinerary current = selectedItinerary.getValue();
        if (current == null || !current.getId().equals(updated.getId())) return;

        // only take metadata from updated
        selectedItinerary.setValue(rebuildWithItems(updated, current.getItineraryItems()));
    }

    // handle filter unschedule item in selected itinerary
    public LiveData<List<ItineraryItem>> getUnscheduleItems() {
        return Transformations.map(selectedItinerary, itinerary -> {
            if (itinerary == null) return new ArrayList<>();
            return itinerary.getItineraryItems().stream()
                    .filter(item -> item.getDate() == null)
                    .toList();
        });
    }

    // handle flatten item in selected itinerary
    public LiveData<List<ItineraryDisplayItem>> getFlattenedItinerary() {
        return Transformations.map(selectedItinerary, itinerary -> {
            if (itinerary == null) return new ArrayList<>();
            return flattenItinerary(itinerary);
        });
    }

    private List<ItineraryDisplayItem> flattenItinerary(Itinerary itinerary) {
        List<ItineraryDisplayItem> displayItems = new ArrayList<>();
        if (itinerary == null || itinerary.getStartDate() == null || itinerary.getEndDate() == null) {
            return displayItems;
        }

        Map<String, List<ItineraryItem>> groupedItems = new HashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (itinerary.getItineraryItems() != null) {
            for (ItineraryItem item : itinerary.getItineraryItems()) {
                // filter all unschedule items
                if (item.getDate() != null && item.getOrderIdx() != null) {
                    String dateKey = fmt.format(item.getDate());
                    groupedItems.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(item);
                }
            }
        }

        // calculate date range to get all dates even those without items
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(itinerary.getStartDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date endDate = itinerary.getEndDate();

        while (!calendar.getTime().after(endDate)) {
            Date currentDay = calendar.getTime();
            String dateKey = fmt.format(currentDay);

            // always add header item
            displayItems.add(new HeaderItem(currentDay));

            // if there are items in this day, add them
            List<ItineraryItem> itemsInDay = groupedItems.get(dateKey);
            if (itemsInDay != null) {
                itemsInDay.sort(Comparator.comparingInt(ItineraryItem::getOrderIdx));

                for (ItineraryItem item : itemsInDay) {
                    displayItems.add(new LocationItem(item));
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return displayItems;
    }

    /** Functional interface for transforming an item list inside updateItineraryItems. */
    private interface ItemListTransform {
        List<ItineraryItem> apply(List<ItineraryItem> items);
    }
}