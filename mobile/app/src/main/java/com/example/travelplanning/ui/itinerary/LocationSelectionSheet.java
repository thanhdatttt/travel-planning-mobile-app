package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;

public class LocationSelectionSheet extends BottomSheetDialogFragment {
    private final Date targetDate; // date when the user clicks "Add"
    private final String currentTripId;
    private LocationSelectionAdapter adapter;
    private ItineraryViewModel viewModel;

    public LocationSelectionSheet(String currentTripId, Date targetDate) {
        this.targetDate = targetDate;
        this.currentTripId = currentTripId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_location_selection, container, false);
        RecyclerView rv = view.findViewById(R.id.rvLocationSelection);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupAdapter(rv);
        observeUnscheduledItems();

        return view;
    }

    private void setupAdapter(RecyclerView rv) {
        adapter = new LocationSelectionAdapter(item -> {
            viewModel.scheduleItineraryItem(currentTripId, item.getId(), targetDate);
            dismiss();
        });
        rv.setAdapter(adapter);
    }

    private void observeUnscheduledItems() {
        viewModel.getUnscheduleItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setData(items);
        });
    }
}
